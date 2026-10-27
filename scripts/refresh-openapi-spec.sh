#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
spec="$root/core/src/main/resources/spec/openapi.json"
tmp="$(mktemp)"
trap 'rm -f "$tmp"' EXIT

curl -fsSL https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json -o "$tmp"
# Reject a truncated body or an HTML maintenance page before it replaces a tracked file.
jq -e '.openapi and (.paths | length == 12) and (.components.schemas | length == 5)' "$tmp" >/dev/null
mv "$tmp" "$spec"
chmod 644 "$spec"

"$root/mvnw" -B -f "$root/pom.xml" -pl core generate-sources
git -C "$root" diff --stat core/src/main/resources/spec/openapi.json
