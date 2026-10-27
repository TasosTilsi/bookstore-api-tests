# Architecture Decision Records

Each record covers one decision. The format is Title, Status, Context, Decision, Consequences, and Alternatives considered and why rejected.

| ADR | Title | Status |
|---|---|---|
| [ADR-0001](ADR-0001-four-module-maven-reactor.md) | Four-module Maven reactor with one module per service area | Accepted |
| [ADR-0002](ADR-0002-testng-over-junit-5.md) | TestNG over JUnit 5 | Accepted |
| [ADR-0003](ADR-0003-restassured-for-http.md) | RestAssured for HTTP | Accepted |
| [ADR-0004](ADR-0004-allure-3-over-surefire-html.md) | Allure 3 instead of the Surefire HTML report | Accepted |
| [ADR-0005](ADR-0005-surefire-pinned-3-5-6.md) | Surefire held at 3.5.6 | Accepted |
| [ADR-0006](ADR-0006-models-from-committed-openapi-snapshot.md) | Models generated from a committed OpenAPI snapshot | Accepted |
| [ADR-0007](ADR-0007-stateless-fluent-client-per-resource.md) | Stateless fluent client with one class per resource | Accepted |
| [ADR-0008](ADR-0008-response-wrapper-and-custom-assertions.md) | Response wrapper plus a custom assertion type | Accepted |
| [ADR-0009](ADR-0009-measure-live-service-before-assertions.md) | Measure the live service before writing assertions | Accepted |
| [ADR-0010](ADR-0010-group-based-test-taxonomy.md) | Group-based test taxonomy and Allure label mapping | Accepted |
| [ADR-0011](ADR-0011-no-retries-and-no-workarounds.md) | No retries and no workarounds for service behaviour | Accepted |
| [ADR-0012](ADR-0012-invariants-for-regenerated-authors-data.md) | Invariants instead of values for regenerated Authors data | Accepted |

## Reading order

For a first pass, read ADR-0001 (module layout), ADR-0007 (client), ADR-0008 (assertions) and ADR-0010 (groups). The remaining records explain the toolchain, the reporting stack, the model generation, and the evidence rules that the tests follow.

Each ADR records what it costs as well as what it buys, in its Consequences section.
