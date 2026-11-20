# 등록 설정 템플릿

## settings.gradle.kts
`include("apps:customer-api")` 앞에 추가:
```kotlin
include("modules:{name}")
```

## apps/customer-api/build.gradle.kts
기존 `implementation(project(":modules:..."))` 줄들 옆에:
```kotlin
implementation(project(":modules:{name}"))
```

## apps/customer-api/src/main/java/com/echo/api/EchoApplication.java
세 곳 모두에 패키지를 추가한다(하나라도 빠지면 빈/엔티티/리포지토리가 안 잡힌다).
```java
@SpringBootApplication(scanBasePackages = {
    ...,
    "com.echo.{name}.application"
})
@EntityScan(basePackages = {
    ...,
    "com.echo.{name}.domain"
})
@EnableJpaRepositories(basePackages = {
    ...,
    "com.echo.{name}.infrastructure"
})
```
