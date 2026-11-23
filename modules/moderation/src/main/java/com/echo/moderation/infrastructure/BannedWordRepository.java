package com.echo.moderation.infrastructure;

import com.echo.moderation.domain.BannedWord;
import org.springframework.data.jpa.repository.JpaRepository;

/** 금칙어 JPA 리포지토리. 목록 관리는 DB에서 직접 한다(관리 API는 범위 외). */
public interface BannedWordRepository extends JpaRepository<BannedWord, Long> {
}
