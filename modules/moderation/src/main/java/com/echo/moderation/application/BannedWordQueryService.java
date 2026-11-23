package com.echo.moderation.application;

import com.echo.moderation.domain.BannedWord;
import com.echo.moderation.infrastructure.BannedWordRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 금칙어 필터(읽기 측). 금칙어 목록은 DB에서 매번 읽으므로 DB 수정이 즉시 반영된다.
 * 400 매핑은 호출자(컨트롤러)의 몫이다.
 */
@Service
@Transactional(readOnly = true)
public class BannedWordQueryService {

    private final BannedWordRepository bannedWordRepository;

    public BannedWordQueryService(BannedWordRepository bannedWordRepository) {
        this.bannedWordRepository = bannedWordRepository;
    }

    /** 본문에 포함된 첫 금칙어를 반환한다. 없으면 empty. */
    public Optional<String> findContainedWord(String content) {
        return bannedWordRepository.findAll().stream()
                .filter(bannedWord -> bannedWord.isContainedIn(content))
                .map(BannedWord::getWord)
                .findFirst();
    }
}
