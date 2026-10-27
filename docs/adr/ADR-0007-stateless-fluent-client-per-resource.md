# ADR-0007: Stateless fluent client with one class per resource

- Status: Accepted
- Date: 2026-10-27

## Context

Tests need to read like the calls they make. Each call should carry a useful Allure step name, and the step name should include the arguments, for example `GET /Books/1` rather than `GET /Books/{id}`. The service is stateless, so no client state has to survive between tests.

The suite must also send bodies that a typed object cannot express: an empty body, malformed JSON, a JSON array, a wrong field type, a partial update and an unknown field. A typed `create(Book)` would default fields and silently change the payload under test.

## Decision

Use a stateless fluent client with one class per resource:

- `Api` is a final class with a private constructor. It exposes `request(String method, String path)`, `request(String method, String path, Map<String, String> headers)`, the five-argument `request(...)`, `books()` and `authors()` (`core/src/main/java/io/github/tasostilsi/bookstore/client/Api.java`).
- `BooksApi` and `AuthorsApi` are final classes. Each terminal takes the values it needs: `get(int id)`, `create(String body)`, `create(String body, String contentType)`, `update(int id, String body)`, `delete(int id)`, `byBook(int bookId)` (`core/src/main/java/io/github/tasostilsi/bookstore/client/BooksApi.java`, `core/src/main/java/io/github/tasostilsi/bookstore/client/AuthorsApi.java`).
- `ApiSpec` is package-private and stateless. It builds one `RequestSpecification` per call with the base URI, timeouts and the Allure filter.
- Every resource operation carries an Allure `@Step` naming the verb and path. The one-argument `create(String)` delegates to the annotated two-argument overload. The parameter names fill placeholders because the compiler emits parameter metadata (`<parameters>true</parameters>` in `pom.xml`).
- `Api.request` is the escape hatch for requests that do not fit a resource terminal: unsupported methods, path-binding probes and custom headers.

Do not add a generic request builder or a stateful fluent builder.

## Consequences

- No client object holds state between tests. A test cannot leak a header, a body or a base path into the next test through a shared builder.
- The fluent call reads as the operation it performs, and the report step carries the argument values.
- Adding a service area means adding a client class, a block in `ApiPaths`, a module, a test package, and one factory method on `Api`. The factory is a two-line delegate; the cost the design avoids is a shared request builder, not a two-method list.
- The raw escape hatch keeps host-level probes possible. Those probes do not get a named `@Step`, because `Api.request` itself is not annotated. Their report entry contains only the `HTTP exchange` step from `AllureRestAssured`.
- The `create(String, String)` overload cannot express a request with no `Content-Type` header. RestAssured always emits one. ADR-0011 records that limit; the 415 contract is pinned with `text/plain`.
- The `books` and `authors` modules do not import RestAssured types. They see only `Api`, the two clients and `ApiResponse`.
- There is no builder anywhere, not even for the malformed-request cases: the body is a `String` on the terminal.

## Alternatives considered and why rejected

- **A generic request builder such as `RequestBuilder.get(path).header(...).call()`.** It is one abstraction with one implementation. It loses the per-resource step names, and it invites a test to forget the base URI or the content type. The raw five-argument `Api.request` already covers the unusual cases.
- **A stateful builder that accumulates headers and body.** State can survive between tests, and the suite runs serially. A leak would be hard to see. The stateless client has no state to leak.
- **A declarative service registry.** It is an abstraction with one implementation, and it hides the per-resource step names.
- **Typed payload objects for requests.** They cannot express empty, malformed, array, wrong-type or partial bodies without defaulting fields, which changes the payload under test.
