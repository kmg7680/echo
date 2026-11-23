package com.echo.moderation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 금칙어 엔티티. 금칙어 목록은 코드가 아니라 DB({@code banned_words})로 관리한다.
 *
 * <p>대소문자를 구분하지 않고 비교하기 위해 소문자로 정규화해 저장한다.
 */
@Entity
@Table(name = "banned_words")
public class BannedWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String word;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected BannedWord() {
    }

    public BannedWord(String word) {
        if (word == null || word.isBlank()) {
            throw new IllegalArgumentException("금칙어는 비어 있을 수 없습니다.");
        }
        this.word = word.strip().toLowerCase(Locale.ROOT);
        this.createdAt = LocalDateTime.now();
    }

    /** 본문에 이 금칙어가 포함되어 있는지(대소문자 무시). */
    public boolean isContainedIn(String content) {
        return content != null && content.toLowerCase(Locale.ROOT).contains(word);
    }

    public Long getId() { return id; }
    public String getWord() { return word; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
