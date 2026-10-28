# ADR-0014: Contract-first assertions with a red CI gate

- Status: Accepted
- Date: 2026-10-28
- Amends: ADR-0009, ADR-0010

## Context

The first version of the suite measured the live service and asserted what it does. A test that observed `POST` answering `200` with no `Location` asserted exactly that and passed. The report was green, and a reader had to know REST already to see that the service was wrong.

The demo is stateless. `POST` and `PUT` echo the request payload and store nothing, `DELETE` answers `200` with zero bytes and removes nothing, and Authors data is regenerated per request. Under the measurement-first rule every one of those behaviours was recorded as correct for this service, which is the opposite of what a test suite is for. The requester's instruction was to validate the correct real behaviour, not how this fake API works, for every API under test.

## Decision

Assert the contract, and let the suite be red.

- An expected status, header or body comes from RFC 9110's method semantics, the problem-details shape, or the types and constraints the committed OpenAPI schema declares. A measurement of the live service is evidence for the defect report, never the source of an expected value.
- A `contract` method states a requirement any correct REST API must meet. A `defect` method is a `contract` method whose requirement this service violates, and is expected to fail until the service is fixed.
- A requirement no contract decides, such as the seed range or case-insensitive path matching, is a `regression` method: it records this implementation's choice and passes.
- The defect entry is the failing test, not a paragraph in a report. Each defect test carries a method-level `@Feature("Contract defects")` label and, where the gap is not obvious from the assertion, an `@Description` naming the rule.
- The build is red by design. The local command uses `-Dmaven.test.failure.ignore=true` so Maven does not stop at the first failing module; CI then runs a step that reads the Allure results and exits nonzero when any result is failed. The deploy job runs with `if: always()`, because the failing report is the deliverable.

At the time of writing, 25 of 91 cases fail against the demo, and every one of them is intended. The list is in the `README.md` under Defects.

## Consequences

- A green run now means every requirement in the suite is met. A red run names the requirements that are not. The old green run meant only that the recording was still accurate.
- The run cannot be used as a pass/fail signal for a release of this demo. It is a defect report, and the workflow is designed to publish it rather than block on it silently.
- 14 of the 25 failures surface at a fixture create, because the fixture is a `POST` that a correct service answers with `201` and this one answers with `200`. The root cause is the same defect; the method name, the `@Description` and the precondition reason on the fixture assertion carry the specific rule, so the count reads higher than the number of distinct requirements.
- A test that fails is now ambiguous between "the service is wrong" and "my reading of the contract is wrong". The response body and the RFC settle it; when the RFC allows two answers, the assertion uses `hasStatusOneOf`, as on `PUT`.
- `-Dgroups=contract` selects the failures as well as the passing contract tests. `-Dgroups=defect` selects the defect report alone and is the filter to use when the question is "what is broken".
- The `README.md` must stay in step with the failing set, or the report and the document disagree. Both are checked against the same Surefire run.

## Alternatives considered and why rejected

- **Record the measured behaviour and stay green (the previous rule).** It documents the defect but cannot detect it: a service that behaved the same way forever would stay green forever, and a reader who does not already know RFC 9110 is told the service is correct.
- **Quarantine the defect tests behind a Maven profile so the default build is green.** This was the first proposal. It hides the defect from the default build and makes the quarantine list a second thing to maintain; a test nobody runs stops being true. The requester chose the red gate instead.
- **Let Maven fail normally, without `-Dmaven.test.failure.ignore=true`.** The reactor stops at the first failing module, and `books` and `authors` depend on `core`, so a failure in `core` would leave the other two untested. `-fae` is no better: it skips dependents, so the run would report `core` alone.
- **Put the defect tests in their own module.** It moves them away from the class whose resource they exercise and duplicates fixtures to do it. Groups already select them.
- **Keep measurement as the oracle and add a separate "expected defects" list.** The list would be a second copy of the assertions, and the two copies would drift.
