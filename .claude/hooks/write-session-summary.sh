#!/usr/bin/env bash
# Stop 훅 — Claude가 응답을 마칠 때마다 transcript에서 "이번 세션에서 바뀐 파일"과
# "사용자 요청 목록"을 뽑아 .claude/session-summary.md 에 덮어쓴다. 다음 세션의 인수인계용.
#  - 세션을 막지 않는다: 어떤 경우에도 exit 0, stdout/stderr 출력 없음 (조용히 통과)
#  - 이번 세션에서 적용된 파일 변경이 없으면 파일을 건드리지 않는다
#    (질문만 한 세션이 이전 세션의 인수인계 내용을 덮어쓰지 않도록)
#  - 요약은 LLM이 쓴 문장이 아니라 요청 원문 첫 줄을 모은 기계적 요약이다
#  - 파일 목록은 Edit/Write/MultiEdit 기준. Bash(rm, mv 등)로 바뀐 파일은 아래 git status로 보완한다
set -uo pipefail
trap 'exit 0' ERR   # 훅 자체의 오류가 세션에 영향을 주지 않게 한다

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT="${SESSION_SUMMARY_FILE:-$PROJECT_ROOT/.claude/session-summary.md}"   # 환경변수는 테스트용

command -v jq >/dev/null 2>&1 || exit 0

# 1) 훅 입력(JSON, stdin)에서 transcript 경로와 세션 ID 추출
input="$(cat)"
transcript="$(printf '%s' "$input" | jq -r '.transcript_path // empty')"
session_id="$(printf '%s' "$input" | jq -r '.session_id // empty')"
[ -n "$transcript" ] && [ -f "$transcript" ] || exit 0

# 2) 실제로 적용된 파일 변경 수집 — 훅/권한에 막혀 is_error 로 끝난 도구 호출은 제외
#    프로젝트 밖 파일(scratchpad 등)과 지금은 존재하지 않는 파일(임시 파일 등)도 제외한다
changed=""
while IFS=$'\t' read -r rel w e; do
  [ -z "$rel" ] && continue
  case "$rel" in /*) continue ;; esac            # ltrimstr 후에도 절대경로 = 프로젝트 밖
  [ -e "$PROJECT_ROOT/$rel" ] || continue
  changed+="- \`$rel\` (Write $w, Edit $e)"$'\n'
done < <(jq -rs --arg root "$PROJECT_ROOT/" '
  ([.[] | select(.type=="user") | .message.content | arrays | .[]
     | select(.type=="tool_result" and .is_error==true) | .tool_use_id]) as $err
  | [.[] | select(.type=="assistant") | .message.content[]?
     | select(.type=="tool_use" and (.name | IN("Write","Edit","MultiEdit"))
              and (.id as $i | $err | index($i) | not))
     | {id, name, path: (.input.file_path | ltrimstr($root))}]
  | unique_by(.id)
  | group_by(.path)
  | map({path: .[0].path,
         w: (map(select(.name=="Write")) | length),
         e: (map(select(.name!="Write")) | length)})
  | .[] | "\(.path)\t\(.w)\t\(.e)"' "$transcript")

# 적용된 변경이 없으면 이전 인수인계 내용을 보존하고 종료
[ -z "$changed" ] && exit 0

# 3) 사용자 요청 목록 — 시스템 주입(<로 시작)과 API 오류 문구는 제외, 첫 줄만 100자까지
requests="$(jq -r '
  select(.type=="user") | .message.content | strings
  | select((startswith("<") or startswith(" API Error")) | not)
  | split("\n")[0] | .[0:100] | "- " + .' "$transcript")"

branch="$(git -C "$PROJECT_ROOT" rev-parse --abbrev-ref HEAD 2>/dev/null || echo '-')"
git_status="$(git -C "$PROJECT_ROOT" status --short 2>/dev/null || true)"

# 4) 임시 파일에 쓰고 교체 (쓰는 도중 읽히는 반쪽짜리 파일 방지)
tmp="$(mktemp "$OUT.XXXXXX")"
{
  printf '# 세션 요약 (자동 생성 — Stop 훅)\n\n'
  printf '다음 세션은 이 파일을 먼저 읽고 이어서 작업한다. 직접 편집하지 말 것 (세션 종료 시 덮어써진다).\n\n'
  printf -- '- 갱신: %s\n- 세션: %s\n- 브랜치: %s\n\n' "$(date '+%Y-%m-%d %H:%M:%S')" "${session_id:--}" "$branch"
  printf '## 사용자 요청 (시간순)\n\n%s\n\n' "$requests"
  printf '## 이번 세션에서 바뀐 파일 (Edit/Write 기준)\n\n%s\n\n' "$changed"
  printf '## 현재 워킹트리 상태 (git status --short)\n\n'
  if [ -n "$git_status" ]; then
    printf '```\n%s\n```\n' "$git_status"
  else
    printf '(변경 없음 또는 git 저장소 아님)\n'
  fi
} > "$tmp"
mv "$tmp" "$OUT"
exit 0
