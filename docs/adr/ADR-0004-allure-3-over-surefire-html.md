# ADR-0004: Allure 3 instead of the Surefire HTML report

- Status: Accepted
- Date: 2026-10-27

## Context

The suite must produce a report a reader can browse without downloading the repository. The report should show each test, the client calls it made, and the request and response attached to each call. It also needs to be publishable from CI as a static site.

Surefire already writes JUnit XML per module. That XML is the machine-readable artifact a CI job can turn into a failure table. The question is the human-readable report, not the machine-readable one.

## Decision

Use Allure 3.0.1 for the human-readable report:

- `allure-testng` at test scope in the parent POM.
- `allure-rest-assured` at compile scope in `core`.
- `allure-maven` 3.1.0, with `allure.reportVersion` pinned to 3.17.0 (`pom.xml`).
- `@Step` on every resource operation (`BooksApi`, `AuthorsApi`), with AspectJ weaving configured once in the parent POM. The one-argument `create(String)` delegates to the annotated two-argument overload.
- `./mvnw -B allure:aggregate` writes one static report to `target/site/allure-maven-plugin`.
- `./mvnw -B allure:serve` serves the same suite from the repository root. The serve mojo reads a single results path, so every module writes into the root `target/allure-results` (`pom.xml`), the directory the root project's serve goal reads. `./.allure/bin/allure open target/site/allure-maven-plugin` reads the aggregated static report instead.

Do not enable the Surefire HTML report. Keep the Surefire JUnit XML.

## Consequences

- Each test expands into its client-call steps. A delete-then-read test records `DELETE /Books/1` and `GET /Books/1` as separate steps. The `AllureRestAssured` filter adds an `HTTP exchange` step with the request and response attachments.
- Steps depend on the AspectJ weaver being on the test JVM. The parent POM declares `aspectjweaver` as a Surefire plugin dependency and passes it as a quoted `-javaagent` on `argLine` (`pom.xml`). A missing agent produces no steps and no error, so the planned CI workflow checks for the named step `GET /Books` rather than a non-empty steps array.
- The generated result JSON contains the Allure labels and links from `@Epic`, `@Feature`, `@Severity` and `@Link`. Method-level `@Feature` overrides move a test into its own Behaviors section while leaving it in its class in the Suites view.
- `target/allure-results` at the root accumulates across runs when the target directory is not cleaned. Local runs use `clean verify` before aggregating, or the report shows tests that no longer exist.
- The report must be served, not opened from disk, because Allure loads its data over XHR from relative paths.
- Multi-call tests rely on the per-call `@Step` and do not wrap the sequence in `Allure.step`; `BooksDeleteTest.deleteDoesNotRemoveResource` is the current example. The two client terminals already produce the two named steps, so a wrapper would nest a third.

## Alternatives considered and why rejected

- **Surefire HTML report.** It lists pass and fail per module but has no steps, no request or response attachments and no Behaviors view. It also produces one report per module, not one combined report.
- **ExtentReports.** An extra dependency, no automatic TestNG listener registration comparable to `allure-testng`, and no RestAssured filter. The suite would write its own reporting layer.
- **ReportPortal.** It needs a server and infrastructure the repository does not have.
- **JUnit XML plus a custom HTML page.** It would work for pass and fail counts, but the steps and attachments would have to be built from scratch, and the result would duplicate a report format the project can use directly.
