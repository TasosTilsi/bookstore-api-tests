# ADR-0008: Response wrapper plus a custom assertion type

- Status: Accepted
- Date: 2026-10-27

## Context

Raw RestAssured assertions in tests scatter HTTP details across every method: status codes, header names, JSON paths and parser calls. A failure then points into the HTTP library rather than at the test line that made the assertion. Two response shapes need explicit handling: an empty body, which must not reach a parser, and an absent `Content-Type`, which must be read from the header map without touching the body.

Several tests also share the same problem+json envelope check. Repeating five assertions in every error test would be noise.

## Decision

Wrap the RestAssured response in `ApiResponse` and expose assertions through `ApiResponseAssert` (`core/src/main/java/io/github/tasostilsi/bookstore/http/ApiResponse.java`, `core/src/main/java/io/github/tasostilsi/bookstore/http/ApiResponseAssert.java`).

- `ApiResponse` exposes `status()`, `header(name)`, `contentType()`, `body()`, `hasBody()`, `json()`, `bodyAs(Class)`, `bodyAsList(Class)` and `assertThat()`.
- `contentType()` reads the raw `Content-Type` header. It does not use `getContentType()`, which normalises an absent header to an empty string.
- `json()`, `bodyAs()` and `bodyAsList()` call `requireBody()` and throw `IllegalStateException` naming the status and status line when the body is empty.
- `ApiResponseAssert` returns itself from every method and uses hard Hamcrest assertions. It includes `isProblemJson()`, which checks the problem content type, the charset, the presence of `type`, `title` and `traceId`, and that the body's `status` equals the response status.
- `hasNullField` and `hasNoField` check the top-level key set through `getMap("")`. They do not claim to distinguish a present null from an absent key at nested paths.

## Consequences

- A failed assertion throws from `ApiResponseAssert`, and the stack includes the test method that called the chain, so the failing test is identifiable.
- Empty-body accessors produce a readable `IllegalStateException` with the status and status line, not an opaque Jackson or JsonPath error. `ApiResponseEdgeTest.emptyBodyAccessorsThrowIllegalStateException` pins this against a locally built empty `200`, so the test states the wrapper's rule without depending on an endpoint; `BooksDeleteTest` asserts the live `DELETE /Books/1` shape.
- `hasNoContentType` reads only the header. `ApiResponseEdgeTest.absentContentTypeDoesNotParseBody` pins this against a locally built `405` with no content type, and `BooksDeleteTest.deleteCollectionReturns405` pins the live `405`.
- `isProblemJson` centralises the problem+json envelope check and makes it status-aware, so a body with the wrong `status` field fails.
- The wrapper does not hide every RestAssured type. `json()` returns a `JsonPath`, and a test that needs a key set or a typed list uses it directly. That is deliberate: the wrapper covers the common assertions, not every expression.
- The constructor is private and the only way in is the public static factory `ApiResponse.of(Response)` (`core/src/main/java/io/github/tasostilsi/bookstore/http/ApiResponse.java`). The factory keeps construction in one place and still lets `Api`, in the `client` package, create a wrapper.
- The tests use only hard assertions, through the chain or Hamcrest and TestNG directly. A failure stops at the first mismatch, which is the signal a contract test wants. `SoftAssert` is not used anywhere.

## Alternatives considered and why rejected

- **Raw `.then().statusCode(200).body("id", is(1))` in every test.** It duplicates header names and JSON paths, and a failure points into RestAssured. The problem+json shape would be repeated in every error test.
- **AssertJ.** It adds a dependency and still needs a wrapper to handle the empty-body and absent-content-type cases. Hamcrest already arrives with RestAssured.
- **Checked exceptions on accessors.** Every test would carry `throws`, and an empty body is a response condition, not an exceptional control flow at the test level.
- **Return the raw `Response` from the clients.** It leaks RestAssured into the service modules and removes the single place where the empty-body guard lives.
