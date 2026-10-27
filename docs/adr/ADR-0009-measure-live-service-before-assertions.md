# ADR-0009: Measure the live service before writing assertions

- Status: Accepted
- Date: 2026-10-27

## Context

The OpenAPI document is not a usable oracle for behaviour. Every operation declares only `200`, including `POST` and `DELETE`. No schema declares `required` fields, and although every schema sets `additionalProperties: false`, the service accepts an unknown field and drops it. It cannot say which status code a validation failure returns, which fields are mandatory, or what an error body looks like.

The service also deviates from common REST behaviour in ways a reader would not guess: `POST` returns the request body with `200` and no `Location`, `DELETE` returns an empty `200`, and `PUT` takes the resource id from the body. Writing assertions from the textbook would produce a suite that is red for reasons no test author can fix.

## Decision

Measure the live service before writing an assertion, and write the assertion from the measurement.

- Record the probe output in the assertion itself. The raw logs sit in the gitignored `tmp/` directory.
- Set every expected status, header and body value from the probe, not from the OpenAPI document.
- When a test fails, re-probe once and report the result before editing the assertion. A red test is a signal that the recorded behaviour changed, not an invitation to relax the check.

The current record is the test itself: each measured value appears as a literal in the assertion that reads it, and the `@Description` on a deviation says what was measured. `docs/design.md` summarises the results.

## Consequences

- The suite asserts what the service does: `200` on create, no `Location`, `404` with `v=1.0` for a missing resource, `400` without `v=1.0` for a validation error, an empty `200` on delete, the body-id override on update, and the zero date default.
- The suite can record a deviation without failing. `groups = "deviation"` and `@Feature("Contract deviations")` mark it, and the test is green because the assertion matches reality.
- The raw probe logs are not committed. They reference absolute paths on the machine that produced them and are not part of the suite; the assertion literals are the durable record.
- A service change produces a red test. The correct response is to re-probe and report, then decide whether the assertion or the documented behaviour changes.
- The suite never mocks or stubs the service. A mock would measure the mock, not the contract.

## Alternatives considered and why rejected

- **Trust the OpenAPI document.** It would produce assertions for `201`, required fields and a schema-valid error model that the service does not implement. Most of the suite would be red.
- **Trust REST conventions.** The same problem in a different form. The service is a demo with known deviations; the suite exists partly to record them.
- **Mock or stub the service.** It removes the network dependency but also removes the evidence. The suite would no longer test the published contract.
- **Record only status codes.** It would miss the header asymmetry (`v=1.0` on `404`, absent on `400` and `415`), the empty body on delete, and the body-id override. Those are the findings.
