# ADR-0009: Measure the live service before writing assertions

- Status: Amended by ADR-0014
- Date: 2026-10-27

## Context

The OpenAPI document is not a usable oracle for behaviour. Every operation declares only `200`, including `POST` and `DELETE`. No schema declares `required` fields, and although every schema sets `additionalProperties: false`, the service accepts an unknown field and drops it. It cannot say which status code a validation failure returns, which fields are mandatory, or what an error body looks like.

The service also deviates from common REST behaviour in ways a reader would not guess: `POST` returns the request body with `200` and no `Location`, `DELETE` returns an empty `200`, and `PUT` takes the resource id from the body. Writing assertions from the textbook would produce a suite that is red for reasons no test author can fix.

## Decision

Measure the live service before writing an assertion, and write the assertion from the measurement.

- Record the probe output in the assertion itself. The raw logs sit in the gitignored `tmp/` directory.
- Set every expected status, header and body value from the probe, not from the OpenAPI document.
- When a test fails, re-probe once and report the result before editing the assertion. A red test is a signal that the recorded behaviour changed, not an invitation to relax the check.

The current record is the test itself: each measured value appears as a literal in the assertion that reads it, and the `@Description` on a defect method says what was measured. `docs/design.md` summarises the results.

## Amendment (ADR-0014)

The part of this decision that made a measurement the source of an expected value is superseded. An expectation now comes from RFC 9110 and the committed OpenAPI schema, and a measurement is the evidence recorded in the defect report. A method that observes a violation fails rather than passing with the observed value, unless no contract decides the answer, in which case it is a `regression` method and records what the service does. The probing practice still applies when a red test needs classifying: the response body and the RFC decide whether the service changed or the assertion was wrong. See ADR-0014.

## Consequences

- The suite asserts what the service does: `200` on create, no `Location`, `404` with `v=1.0` for a missing resource, `400` without `v=1.0` for a validation error, an empty `200` on delete, the body-id override on update, and the zero date default.
- Under the original rule the suite could record a deviation without failing: a `deviation` group and a `@Feature("Contract deviations")` label marked it, and the assertion matched reality, so the test was green. ADR-0014 replaced that with a requirement the test can violate.
- The raw probe logs are not committed. They reference absolute paths on the machine that produced them and are not part of the suite; the assertion literals are the durable record.
- A service change produces a red test. The correct response is to re-probe and report, then decide whether the assertion or the documented behaviour changes.
- The suite never mocks or stubs the service. A mock would measure the mock, not the contract.

## Alternatives considered and why rejected

- **Trust the OpenAPI document.** It would produce assertions for `201`, required fields and a schema-valid error model that the service does not implement. Most of the suite would be red.
- **Trust REST conventions.** It was rejected because the service is a demo with known deviations and the suite was meant to record them. ADR-0014 reverses this: a convention the service misses is now a failing test, not a recorded observation.
- **Mock or stub the service.** It removes the network dependency but also removes the evidence. The suite would no longer test the published contract.
- **Record only status codes.** It would miss the header asymmetry (`v=1.0` on `404`, absent on `400` and `415`), the empty body on delete, and the body-id override. Those are the findings.
