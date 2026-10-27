#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
mkdir -p "$root/core/src/main/resources/spec"
curl -fsS https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json \
    -o "$root/core/src/main/resources/spec/openapi.json"
"$root/mvnw" -B -pl core generate-sources
git -C "$root" diff --stat core/src/main/resources/spec/openapi.json
