# 짝 테스트 템플릿

JUnit 5 + `@DisplayName`. 골격은 실패하는 TODO 테스트가 아니라 컴파일되는 최소 테스트로 둔다.

## modules/{name}/src/test/java/com/echo/{name}/domain/{Name}Test.java
```java
package com.echo.{name}.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class {Name}Test {

    @Test
    @DisplayName("생성 시 createdAt이 설정된다")
    void createdAtIsSet() {
        {Name} {name} = new {Name}(/* TODO */);
        assertThat({name}.getCreatedAt()).isNotNull();
    }

    // TODO: 불변식 위반 -> IllegalArgumentException 케이스
}
```
(assertj가 모듈 테스트 클래스패스에 없으면 JUnit `assertNotNull`/`assertThrows`를 쓴다. follow의 `FollowTest`를 따른다.)

## modules/{name}/src/test/java/com/echo/{name}/application/{Name}ServiceTest.java
```java
package com.echo.{name}.application;

import static org.mockito.Mockito.verify;

import com.echo.{name}.infrastructure.{Name}Repository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class {Name}ServiceTest {

    @Mock
    private {Name}Repository {name}Repository;

    @InjectMocks
    private {Name}Service {name}Service;

    @Test
    @DisplayName("create는 저장소에 save를 호출한다")
    void createSaves() {
        // TODO: given/when 후 verify({name}Repository).save(any())
    }
}
```

## apps/customer-api/src/test/java/com/echo/api/{Name}IntegrationTest.java
```java
package com.echo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** {Name} API 통합 테스트. 각 테스트는 @Transactional로 롤백한다. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class {Name}IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // TODO: HTTP 상태(201/400/404/409)와 ApiResponse 형식(jsonPath) 검증
}
```
