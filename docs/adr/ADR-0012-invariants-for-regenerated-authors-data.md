# ADR-0012: Invariants instead of values for regenerated Authors data

- Status: Accepted
- Date: 2026-10-27

## Context

The Authors collection is regenerated on every request. Measured over eleven calls, the total number of authors ranged from 586 to 629. The `idBook` groups are stable in shape: 200 distinct groups spanning 1 to 200, with 2 to 4 authors each, and author ids contiguous from 1. An id near the current total is unstable: `/Authors/596` returned `404` and `200` on alternating calls.

Books do not behave this way. `GET /Books` always returns 200 books with ids 1 to 200.

A test that asserts an Authors total, captures an author id and re-reads it, or compares two Authors responses will flake. A flake hides real regressions and makes the report untrustworthy.

## Decision

Assert invariants, not values, for Authors data:

- Author ids are contiguous ascending from 1, with no gaps.
- The set of `idBook` values is exactly 1 to 200, with 200 distinct groups.
- A stable low id such as 1 is readable and its `idBook` is within 1 to 200.
- An absent id is tested only with values far outside the observed population: `0` and `100000`.
- `Authors/authors/books/{id}` returns only rows whose `idBook` equals the requested id, and the result is non-empty for a valid id.
- Do not assert the total number of authors, a per-group count, or a specific author id captured from an earlier response.
- The cross-service test compares the set of book ids with the set of `idBook` groups. Both are invariant sets, not values that vary per response.

The rule behind all of this: every assertion must hold for every response the service can return, not just the one that was probed.

## Consequences

- The Authors tests remain stable while the population is regenerated. The invariants held on every probe.
- The suite cannot detect a change that keeps the invariants but changes the content, such as a different author name in an existing group. That is a deliberate limit; the alternative is a flaky test.
- `AuthorsReadTest`, `AuthorsWriteTest` and `AuthorsByBookTest` apply this rule. The cross-service test applies it to the book-id versus group-id set.
- A write test still uses a high id such as 7000 or 9001 for the read-back `404`. The id is far above any possible generated id, so the `404` is unambiguous even though the population is random.
- There is no cleanup step. Nothing is stored, so there is nothing to clean.

## Alternatives considered and why rejected

- **Assert the total number of authors.** It was measured between 586 and 629 across 11 calls. The test would flake on a run that lands outside the sampled range, and it would red for a change that is not a defect.
- **Capture an author id and re-read it.** Ids near the current total flip between `200` and `404` across calls. The test would flake.
- **Freeze or seed the randomness.** The randomness is server-side and not controllable from the client.
- **Snapshot one Authors response and compare against it.** The service regenerates the response, so the snapshot is stale on the next call.
- **Mock the Authors service.** It would remove the regeneration problem by removing the live contract, which defeats the purpose.
