# ADR-0015: Lifecycle isolation

- Status: Accepted
- Date: 2026-10-28

## Context

The suite runs against one shared host whose Books collection holds the same 200 seeded books for every caller, and whose Authors collection is regenerated per request. Several tests need a resource they may then change or delete. A test that deletes Book 1 to prove removal would corrupt shared state on a service that really stores data, and a test that writes to a seeded id would read back another test's write on any service that keeps it.

There is a second trap. The demo stores nothing, so a read after a write answers `404` whatever the write did. A removal test written as "`DELETE /Books/1`, then `GET /Books/1` is `404`" passes on this service only if it is satisfied with a `404` that came from nothing being stored at all, and passes for the wrong reason. The test must therefore create the resource it removes, and assert that creation succeeded before asserting removal.

## Decision

Every test owns the resources it writes to, and no test writes to a resource it does not own.

- No test sends a write to a seeded id, meaning Books 1 to 200 or Authors 1.
- A fixture is created with `POST`, and the id the test uses is the last segment of the `Location` header on the `201` that a correct service returns. The test never assumes the id it sent was stored under.
- Ids in request payloads are chosen above the seeded range: books 201 to 8999, authors 7000 to 8999, and 9001/9002 in the cross-service test. A write addressed to such an id cannot touch stored data even if the service stores it.
- Absent-id cases do not use `201`: a corrected service assigns the next sequential id, which can be 201. They use `0`, `-1`, `99999`, `100000` and `9000`.
- The one write addressed to a seeded id is `BooksUpdateTest.updateWithMismatchedBodyIdReturns409`, which sends a body id of 7 to `/Books/1`. A correct service rejects the inconsistent representation with `409` before mutating anything, so the test is safe on any service that meets the contract.
- No test depends on the execution order of another. Reads of seeded data are fine and are the only cross-test dependency left: `BooksReadTest`, `AuthorsReadTest` and `AuthorsByBookTest` read the seeded collections, and `BooksAuthorsConsistencyTest.everyBookHasAuthors` compares the two.

## Consequences

- A removal test cannot pass vacuously. `deleteRemovesResourceAndLaterGetReturns404` creates a book, asserts `201` with a `Location`, asserts the read at that location, and only then asserts that the `DELETE` made the resource unreadable. On this service it fails at the `201`, which is the honest first violation.
- The defect tests state the full lifecycle of a resource, so a service that stores data passes them without the suite needing any cleanup beyond the delete each test performs.
- Against a corrected service every write test leaves one book or author behind per run, and nothing removes it. The ranges hold about 8,800 book ids and 2,000 author ids; on a demo that is acceptable, and on a service with persistence the fixture would need a teardown instead.
- A test can be run on its own, in any order, and repeatedly against the live demo. `-Dtest=BooksDeleteTest` behaves the same as the full reactor.
- Two failure modes remain and are accepted: an id collision across runs on a service that persists data, and the cross-service read, which fails if the Books collection changes shape for reasons outside the suite.
