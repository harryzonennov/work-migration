-- V1: Create all application schemas
-- Flyway runs this in the context of the connection's default database.
-- All subsequent migrations use schema-qualified table names.

CREATE SCHEMA IF NOT EXISTS `platform`  DEFAULT CHARACTER SET utf8mb4;
CREATE SCHEMA IF NOT EXISTS `finance`   DEFAULT CHARACTER SET utf8mb4;
CREATE SCHEMA IF NOT EXISTS `logistics` DEFAULT CHARACTER SET utf8mb4;
CREATE SCHEMA IF NOT EXISTS `sales`     DEFAULT CHARACTER SET utf8mb4;
CREATE SCHEMA IF NOT EXISTS `production` DEFAULT CHARACTER SET utf8mb4;
