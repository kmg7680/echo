# domain / infrastructure / application 템플릿

패키지: `com.echo.{name}.{domain|infrastructure|application}`. 필드·불변식은 TODO로 두고 사용자 요구로 채운다.

## domain/{Name}.java
```java
package com.echo.{name}.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * TODO: {Name} 엔티티 설명.
 * domain 계층이므로 Spring 의존 없이 POJO + JPA만 사용한다.
 */
@Entity
@Table(name = "{name}s")
public class {Name} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: 도메인 필드

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected {Name}() {
    }

    public {Name}(/* TODO */) {
        // TODO: 불변식은 생성자에서 강제 (null, 범위 등) -> IllegalArgumentException
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```
중복 방지가 필요하면 `@Table(uniqueConstraints = @UniqueConstraint(name = "uk_...", columnNames = {...}))` + 서비스에서 존재 검사.

## domain/Already{Name}Exception.java (필요할 때만)
```java
package com.echo.{name}.domain;

public class Already{Name}Exception extends RuntimeException {
    public Already{Name}Exception(String message) {
        super(message);
    }
}
```

## infrastructure/{Name}Repository.java
```java
package com.echo.{name}.infrastructure;

import com.echo.{name}.domain.{Name};
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {Name} JPA 리포지토리. infrastructure 계층.
 */
public interface {Name}Repository extends JpaRepository<{Name}, Long> {
    // TODO: 파생 쿼리 메서드
}
```

## application/{Name}Service.java
```java
package com.echo.{name}.application;

import com.echo.{name}.domain.{Name};
import com.echo.{name}.infrastructure.{Name}Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class {Name}Service {

    private final {Name}Repository {name}Repository;

    public {Name}Service({Name}Repository {name}Repository) {
        this.{name}Repository = {name}Repository;
    }

    @Transactional
    public {Name} create(/* TODO */) {
        // 도메인 예외는 던지기만 한다. HTTP 상태 변환은 Controller 몫.
        return {name}Repository.save(new {Name}(/* TODO */));
    }
}
```
조회가 많으면 `{Name}QueryService`(클래스 `@Transactional(readOnly = true)`)로 분리한다.
