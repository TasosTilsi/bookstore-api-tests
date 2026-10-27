# ADR-0011: No retries and no workarounds for service behaviour

- Status: Accepted
- Date: 2026-10-27

## Context

The service is public and external. Its behaviour is not ours to change, and some of that behaviour is inconvenient for a test client. Two examples matter here:

- RestAssured cannot send a request with no `Content-Type` header. A `String` body becomes an Apache `StringEntity` with `text/plain; charset=ISO-8859-1`, and `RequestEntityProxy` re-adds the header at execution time even after a filter removes it. `header(..., (Object) null)` throws, and a `byte[]` body becomes `application/octet-stream`.
- `HEAD /Books/1` blocks for about 25 seconds before returning `405`.

A suite that retries a red test, normalises a response value, or relaxes an assertion to get green hides the very behaviour it is meant to record.

## Decision

Do not retry, normalise, fall back or relax an assertion. Record the behaviour and its limits.

- No Surefire `rerunFailingTestsCount`, no retry plugin, no retry loop in the client. The parent POM contains no retry configuration (`pom.xml`).
- No fallback payload and no response normalisation. `ApiResponse` exposes what the service returned.
- No assertion is weakened to make a red test green. A red test is re-probed and reported before any edit.
- The absent request `Content-Type` case is not tested. The `415` contract is pinned with `text/plain`, which the client can send.
- `HEAD` is not tested. The 25 second wait would encode the client's timeout, not the contract.
- The empty-body accessors throw instead of parsing. The zero-date default is read through JsonPath instead of the generated `OffsetDateTime`.
- `dynamicData` is an exclusion group for CI. It is not a retry mechanism.

## Consequences

- An origin outage or a network failure reads as a test failure. The planned CI workflow adds a health gate before the suite so a dead host is distinguishable from a contract failure.
- A test that depends on a regenerated value can be excluded from a CI run with `dynamicData`. It is still part of the full local suite.
- The suite sends malformed, empty, array and partial bodies as raw strings. A typed payload object would have defaulted fields and changed the payload under test.
- The known limits are documented rather than hidden: absent request `Content-Type`, `HEAD`, Authors totals, the public host dependency, and the absence of a mock.
- If the service changes, the suite goes red. That is the intended signal, and the response is a re-probe and a decision, not a retry.

## Alternatives considered and why rejected

- **Surefire `rerunFailingTestsCount`.** It would turn a real service change into a delayed green or a confusing retry count. The suite has no known flaky test that needs it.
- **A custom retry filter in the client.** It hides the same signal, and it would also retry requests that are not idempotent. The service is stateless, but the assertion would still be wrong.
- **Normalise or coerce response values.** It asserts a fiction. The zero date is a real response; the suite records it as it is.
- **Mock the service.** It removes the network dependency but also removes the contract evidence. The suite is a live contract suite.
- **Skip the awkward cases silently.** It would make the suite look complete while leaving the most interesting behaviour unrecorded. The limits are stated instead.
