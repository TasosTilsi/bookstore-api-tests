# ADR-0013: Random payloads built from the generated models, with a logged seed

- Status: Accepted
- Date: 2026-10-28

## Context

Request payloads were hand-written JSON strings in `BooksData` and `AuthorsData`. That has two costs. The data was identical on every run, so a field-length, character-class or integer-range bug in the service was only caught if the literal happened to hit it. And every payload repeated the model's field list, so a change to the committed snapshot would not fail the build: the literal would keep compiling and keep sending yesterday's shape.

The suite also has payloads that no typed constructor can express. The service accepts an empty body, malformed JSON, an array, a wrong field type, an unknown field and `{}`, and answering what it does with them is a large part of what the suite is for.

## Decision

Build valid payloads from the generated `Book` and `Author` types and fill them with datafaker.

Use `net.datafaker:datafaker` 2.4.3 at compile scope in `core`, version-pinned in the parent POM. Not `com.github.javafaker:javafaker`: last released in 2020 and it carries SnakeYAML 1.23, which is CVE-2022-1471. datafaker 2.4.3 uses SnakeYAML 2.4.

Seed one `Faker` per JVM from `-Ddata.seed`, or from a fresh `Random().nextLong()` when the property is absent (`core/src/main/java/io/github/tasostilsi/bookstore/data/RandomData.java`). Print the seed once at startup with the command that reproduces it, and attach it to every test that generates a payload, so a failure on generated data is visible in the report rather than in a console log.

Serialise payloads through a copy of the generated Jackson mapper with `JsonInclude.Include.NON_NULL` (`ApiSpec.body`). The generated models annotate every field with `USE_DEFAULTS`, which means the mapper's default applies, so one line changes what an unset field sends.

Keep the raw-string terminal, `create(String)` / `create(String, String)` / `update(int, String)`, for the payloads the model cannot express, and delete the JSON-string constants that held valid payloads.

## Consequences

- A failing run can be replayed. The printed and attached seed plus `-Ddata.seed=<seed>` reproduces the data, provided the same test classes run in the same order: one faker instance serves the whole JVM, so its sequence is execution-order dependent. Each module forks its own JVM, so each prints its own seed; `-Ddata.seed` applies to all three.
- Every payload assertion is written against the instance that was sent, not against a literal: `hasField("title", book.getTitle())`. A generated value is therefore never copied by hand. The tests that assert an echo compare every field of the sent model, not just the field under test, so a service that echoed a payload only partly would fail instead of passing on the id alone. `publishDate` is compared by instant through `hasInstantField`.
- Ids come from ranges the service does not serve, so a create-then-GET pair cannot accidentally read stored data: books 201..8999, authors 7000..8999, cross-service ids 9001 and 9002. `MAX_BOOK_ID` (200) is the measured upper bound.
- Generated `publishDate` values are truncated to microseconds. The service keeps 7 fractional digits, so a nanosecond timestamp would come back changed and the echo assertion would compare two different instants. Truncation keeps the round trip exact.
- Nulls are omitted from every request. A measured `POST` with `"id":null` returns 400 (`The JSON value could not be converted to System.Int32`), while an omitted field binds to the default. `NON_NULL` is what lets a mostly-filled model express "field absent".
- datafaker brings SnakeYAML 2.4, `rgxgen` and `libphonenumber` transitively. That is three jars for a suite that uses four providers (`book`, `name`, `lorem`, `number`). The alternative was maintaining name and title lists by hand, which is worse data and more code.
- `Book.publishDate` and the fluent setters are the generated API; the factories use them directly, so a snapshot change that renames a field breaks the build.

## Alternatives considered and why rejected

- **Keep fixed JSON-string payloads.** No variation between runs, and the payload silently keeps its old shape when the snapshot changes.
- **Random values from `java.util.Random` by hand.** No realistic strings. A hand-rolled name and title list is either tiny or a copy of what a library already carries.
- **`com.github.javafaker:javafaker` 1.0.2.** The name the request used, but it is abandoned and drags in a SnakeYAML with a published deserialisation CVE.
- **One seed per test class.** Reproduces finer, but the same seed would then be a different data set depending on which class runs first, which is more confusing than one seed per JVM.
- **Fixed seed committed in the POM.** Runs would be identical forever, which removes the point of random data. A committed seed is still reachable on demand with `-Ddata.seed`.
