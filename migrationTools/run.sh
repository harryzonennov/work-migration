#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

bash compile.sh

echo "Running..."
java MigrationEntrance
