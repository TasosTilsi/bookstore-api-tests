# ADR-0006: Models generated from a committed OpenAPI snapshot

- Status: Accepted
- Date: 2026-10-27

## Context

The service publishes an OpenAPI 3.0.1 document at `/swagger/v1/swagger.json`. The document declares five schemas: `Activity`, `Author`, `Book`, `CoverPhoto` and `User`. It is useful for field names and types, but it is a weak oracle for behaviour: every operation declares only `200`, no schema declares `required`, and although every schema sets `additionalProperties: false`, the service accepts a body with an unknown field and silently drops it. The document describes the response shape, not the input rules.

The suite needs response types that match the wire format. Hand-written POJOs would drift from the published contract. Generating from the live URL at build time would make a build depend on the demo service and would let two builds on different days disagree.

## Decision

Commit a snapshot of the document at `core/src/main/resources/spec/openapi.json`. Generate the models from that snapshot during `generate-sources` in `core`, using `openapi-generator-maven-plugin` 7.26.0 (`core/pom.xml`).

Configuration that matters:

- `modelPackage` is `io.github.tasostilsi.bookstore.model`.
- `invokerPackage` is `io.github.tasostilsi.bookstore.generated`, so the generated `ApiClient`, `JSON` and generated `ApiResponse` do not land in the project root package next to the hand-written `http.ApiResponse` and `client.Api`.
- `generateApis=false`, `generateModelTests=false`, `generateModelDocumentation=false`.
- `generateSupportingFiles=true`. Generated models call `ApiClient.urlEncode` from their `toUrlQueryString` helper, so the support classes must exist.
- `openApiNullable=false`, `dateLibrary=java8`, `serializationLibrary=jackson`, `useJakartaEe=true`, `hideGenerationTimestamp=true`.

Refresh the snapshot with `scripts/refresh-openapi-spec.sh`.

## Consequences

- A build does not need the demo service to be reachable. It compiles against the committed snapshot.
- Two builds from the same commit generate the same models. `hideGenerationTimestamp=true` removes the remaining source of variation.
- The generated sources land in `core/target/generated-sources/openapi` and are not committed. Only the snapshot is tracked.
- The models serve the response direction. `ApiResponse.bodyAs` and `bodyAsList` deserialise into them. Request payloads stay as JSON strings in the fixtures, because the service accepts empty, partial, malformed, array and wrong-type bodies that a typed constructor cannot express.
- `publishDate` is generated as `OffsetDateTime` (`core/target/generated-sources/openapi/src/main/java/io/github/tasostilsi/bookstore/model/Book.java`). A `PUT` with `{}` or a partial body returns `"0001-01-01T00:00:00"`, which has no offset and cannot bind to that type. The update test asserts that value through JsonPath instead.
- A spec refresh changes a tracked file, so the diff is reviewable before it changes a test. The script also runs `./mvnw -B -pl core generate-sources` after replacing the snapshot, so the next compile uses the new models. It still touches only the snapshot and the build output under `target`.

## Alternatives considered and why rejected

- **Hand-written POJOs.** They duplicate the published schema, drift when the service changes, and add files a reader has to maintain. The generator produces the same types from the contract.
- **Generate from the live URL at build time.** It makes every build depend on the demo host and makes the generated output non-reproducible across days. A snapshot gives the same types with a reviewable change.
- **JSON Schema validation instead of models.** It was considered earlier and dropped. It adds a validator dependency without producing response types, and the generated models already provide a shape oracle through deserialisation.
- **A separate model module.** It adds a POM and a dependency direction for five generated classes that only `core` uses.
