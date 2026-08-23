#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

echo "Compiling..."
javac PageModuleCopier.java ServiceManagerCopier.java I18nCopier.java MigrationEntrance.java

echo "Running..."
java MigrationEntrance
