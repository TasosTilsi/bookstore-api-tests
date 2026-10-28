# ADR-0003: RestAssured for HTTP

- Status: Accepted
- Date: 2026-10-27

## Context

The suite needs an HTTP client, JSON path extraction, Hamcrest matchers and a way to attach every request and response to the Allure report. The service returns JSON with nested objects and arrays, and some responses have an empty body or no `Content-Type`. The build targets Java 17.

The client layer must also hide the HTTP library from the `books` and `authors` modules. Those modules should call a fluent client, not construct a request specification.

## Decision

Use RestAssured 6.3.0 at compile scope in `core` (`core/pom.xml`). Build the shared request specification in `ApiSpec` with `given()`, the base URI, timeouts, redirect following off, the `AllureRestAssured` filter and optional logging (`core/src/main/java/io/github/tasostilsi/bookstore/client/ApiSpec.java`). Use RestAssured `JsonPath` for path-based assertions in `ApiResponse`. Redirect following is RestAssured's default and would resolve a 3xx before any assertion could see it, which contradicts ADR-0009.

Declare `jackson-databind` and `jackson-datatype-jsr310` explicitly at compile scope. RestAssured marks Jackson optional, and the generated models and `ApiClient` import it from the `core` main source root.

## Consequences

- The service modules do not import `io.restassured.*`. They call `Api`, `BooksApi`, `AuthorsApi` and `ApiResponse`. `Api.request(String, String)` exists so a service module can probe a raw path without seeing a `RequestSpecification`.
- RestAssured 6.x requires Java 17, which matches the compiler `release` setting.
- Hamcrest and JsonPath arrive transitively. No AssertJ dependency is declared.
- `allure-rest-assured` 3.0.1 is declared at compile scope in `core` because `ApiSpec` uses `AllureRestAssured` and the client terminals use `@Step`. A test-scoped jar would not be on the main compile classpath.
- `ApiSpec` sets no default `Content-Type`. The body-bearing client calls set the one `ApiSpec.JSON` constant, `application/json; charset=utf-8`, and the `create(String, String)` overload sets the caller's value. A generated model is serialised by `ApiSpec.body`, a copy of the generated Jackson mapper with nulls omitted, before it reaches RestAssured (ADR-0013). The consequence, that RestAssured cannot send a request with no `Content-Type` at all, is recorded in ADR-0011.
- RestAssured normalises an absent `Content-Type` to an empty string through `getContentType()`. `ApiResponse.contentType()` therefore reads the raw header so the wrapper can distinguish absent from empty (`core/src/main/java/io/github/tasostilsi/bookstore/http/ApiResponse.java`).

## Alternatives considered and why rejected

- **JDK `HttpClient`.** It has no JsonPath, no Hamcrest integration and no Allure filter. The suite would have to build all three, which is more code than the wrapper it already has.
- **Apache HttpClient directly.** Lower level than RestAssured and still requires a JSON path layer and matchers. It adds code without removing a dependency.
- **OkHttp.** A new dependency with no built-in JsonPath or matcher integration. It would also need a custom Allure interceptor.
- **Spring `WebTestClient` or `RestTemplate`.** Pulls in Spring and a larger dependency tree for a suite that needs one HTTP client and JSON assertions. The service modules would still need a wrapper.
