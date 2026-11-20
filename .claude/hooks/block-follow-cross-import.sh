#!/usr/bin/env bash
# PreToolUse 훅 — modules/follow 의 Java 파일에 user/post 패키지 import가 들어가려 하면
# 파일이 써지기 전에 차단한다. exit 2 + stderr 는 Claude에게 피드백으로 전달된다.
# (규칙: 모듈 간 직접 의존 금지 — modules/* → common 만 허용)
set -uo pipefail

input="$(cat)"
file_path="$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty')"

[ -z "$file_path" ] && exit 0

# follow 모듈의 Java 파일만 검사
case "$file_path" in
  */modules/follow/*.java) ;;
  *) exit 0 ;;
esac

# Write(content) / Edit(new_string) / MultiEdit(edits[].new_string) 에서 새로 써질 내용 수집
new_content="$(printf '%s' "$input" | jq -r '
  [.tool_input.content, .tool_input.new_string, (.tool_input.edits[]?.new_string)]
  | map(select(. != null)) | join("\n")')"

violations="$(printf '%s\n' "$new_content" \
  | grep -nE '^[[:space:]]*import[[:space:]]+(static[[:space:]]+)?com\.echo\.(user|post)(\.|;)' || true)"

[ -z "$violations" ] && exit 0

{
  printf '🚫 차단: follow 모듈은 user/post 모듈을 직접 import할 수 없습니다.\n\n'
  printf '대상 파일: %s\n' "$file_path"
  printf '문제가 된 import:\n%s\n\n' "$violations"
  printf '규칙: 모듈 간 직접 의존 금지 (modules/* → common 만 허용).\n\n'
  printf '이렇게 고쳐줘:\n'
  printf '  1. follow가 user/post에서 필요로 하는 것(예: UserId 같은 값 타입, 조회용 포트 인터페이스)을\n'
  printf '     common 모듈(com.echo.common)에 두고, follow는 common 것만 import한다.\n'
  printf '  2. 실제 user/post 구현과의 연결(어댑터)은 apps/customer-api 에서 조립한다.\n'
  printf '  3. 단순히 ID만 필요하면 import 없이 Long 등 기본 타입으로 받는다.\n'
} >&2
exit 2
