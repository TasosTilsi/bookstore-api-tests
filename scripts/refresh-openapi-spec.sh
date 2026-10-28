#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
spec="$root/core/src/main/resources/spec/openapi.json"
tmp="$(mktemp)"
trap 'rm -f "$tmp"' EXIT

curl -fsSL https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json -o "$tmp"
# Reject a truncated body or an HTML maintenance page before it replaces a tracked file. Checks the
# paths and schemas the suite depends on, not the document's size: an added endpoint is legitimate.
jq -e '.openapi
  and (.paths | has("/api/v1/Books") and has("/api/v1/Books/{id}")
       and has("/api/v1/Authors") and has("/api/v1/Authors/{id}")
       and has("/api/v1/Authors/authors/books/{idBook}"))
  and (.components.schemas | has("Book") and has("Author"))' "$tmp" >/dev/null
mv "$tmp" "$spec"
chmod 644 "$spec"

"$root/mvnw" -B -f "$root/pom.xml" -pl core generate-sources
git -C "$root" diff --stat core/src/main/resources/spec/openapi.json
