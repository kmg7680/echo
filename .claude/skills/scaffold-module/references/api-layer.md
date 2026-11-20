# customer-api 템플릿 (Controller + DTO)

위치: `apps/customer-api/src/main/java/com/echo/api/{name}/`. 모듈 안에 두지 않는다.

## {Name}Controller.java
```java
package com.echo.api.{name};

import com.echo.common.ApiResponse;
import com.echo.{name}.application.{Name}Service;
import com.echo.{name}.domain.{Name};
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/{name}s")
@Tag(name = "{Name}", description = "TODO {Name} API")
public class {Name}Controller {

    private final {Name}Service {name}Service;

    public {Name}Controller({Name}Service {name}Service) {
        this.{name}Service = {name}Service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "TODO", description = "TODO")
    public ApiResponse<{Name}Response> create(@RequestBody Create{Name}Request request) {
        try {
            {Name} created = {name}Service.create(/* TODO */);
            return ApiResponse.ok({Name}Response.from(created), "생성했습니다.");
        } catch (IllegalArgumentException e) {
            // 도메인 불변식 위반 -> 400. 중복/없음은 409/404로 매핑
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
```
다른 모듈(예: user)이 필요하면 여기서 해당 모듈의 서비스를 함께 주입해 조립한다(`FollowController`의 `requireUserExists` 참고).

## {Name}Response.java / Create{Name}Request.java (record)
```java
package com.echo.api.{name};

import com.echo.{name}.domain.{Name};
import java.time.LocalDateTime;

public record {Name}Response(Long id, LocalDateTime createdAt /* TODO */) {
    public static {Name}Response from({Name} {name}) {
        return new {Name}Response({name}.getId(), {name}.getCreatedAt());
    }
}
```
```java
package com.echo.api.{name};

public record Create{Name}Request(/* TODO */) {
}
```
