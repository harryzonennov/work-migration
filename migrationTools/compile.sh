#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

echo "Compiling..."
javac CopierParams.java ListControllerMigrator.java EditorControllerMigrator.java PageModuleCopier.java ServiceManagerCopier.java I18nCopier.java ContentTypeMigrator.java MigrationEntrance.java
echo "Compile OK."
