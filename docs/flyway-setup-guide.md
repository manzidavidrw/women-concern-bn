# Setting Up Flyway with Spring Boot and PostgreSQL

**Stack:** Spring Boot 3.x / 4.x · PostgreSQL · Maven

---

## What is Flyway?

Flyway is a database migration tool that tracks, versions, and applies changes to your database schema automatically. Instead of manually running SQL scripts or relying on Hibernate's `ddl-auto=update` (which is dangerous in production), Flyway gives you full control and a clear history of every schema change.

When your Spring Boot app starts, Flyway:
1. Connects to your database
2. Checks which migrations have already been applied
3. Applies any pending ones in order
4. Records everything in a `flyway_schema_history` table

---

## Step 1: Add Dependencies

Open your `pom.xml` and add the following inside `<dependencies>`:

```xml
<!-- Flyway core -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

<!-- Required for PostgreSQL support in Flyway 10+ -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

> **Important:** Since Flyway 10, PostgreSQL support was moved to a separate module (`flyway-database-postgresql`). If you only add `flyway-core`, Flyway will silently do nothing against a PostgreSQL database. Always add both.

Spring Boot manages the Flyway version automatically via its BOM, so no version number is needed.

---

## Step 2: Configure the Database Connection

In `src/main/resources/application.properties`, add your database and Flyway configuration:

```properties
# Database connection
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# Flyway settings
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.schemas=public
spring.flyway.default-schema=public
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.out-of-order=true
spring.flyway.validate-on-migrate=true
spring.flyway.postgresql.transactional-lock=false
logging.level.org.flywaydb=INFO

# JPA — use validate, not update, when Flyway manages the schema
spring.jpa.hibernate.ddl-auto=validate
```

### What each Flyway setting does

| Setting | Value | Why |
|---|---|---|
| `baseline-on-migrate` | `true` | Handles databases that already have tables but no Flyway history yet |
| `baseline-version` | `0` | Baselines at version 0 so V1 and above all run normally |
| `out-of-order` | `true` | Allows applying migrations that arrive out of sequence (useful in team environments) |
| `validate-on-migrate` | `true` | Checks that already-applied migrations haven't been modified before running new ones |
| `postgresql.transactional-lock` | `false` | Avoids locking issues on some PostgreSQL hosted providers (e.g. Neon) |

> **Never use `ddl-auto=update` alongside Flyway.** Use `validate` — Hibernate will verify the schema matches your entities but won't modify it. Flyway is in charge of all schema changes.

---

## Step 3: Create the Migrations Directory

Create the following folder structure inside your project:

```
src/
└── main/
    └── resources/
        └── db/
            └── migration/
                └── V1__create_leave_types.sql
```

---

## Step 4: Write Your First Migration

Create `V1__create_leave_types.sql` inside `src/main/resources/db/migration/`:

```sql
CREATE TABLE leave_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    max_days_per_year INTEGER,
    requires_attachment BOOLEAN DEFAULT FALSE,
    is_paid BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### Flyway Naming Convention

Migration files **must** follow this pattern:

```
V{version}__{description}.sql
```

| Part | Example | Notes |
|---|---|---|
| `V` | `V` | Always uppercase V |
| `{version}` | `1`, `2`, `1.1`, `20260620143000` | Must increase with each migration — timestamps work well in teams |
| `__` | `__` | Two underscores |
| `{description}` | `create_leave_types` | Words separated by underscores |

**Examples:**
```
V1__create_leave_types.sql
V2__add_leave_requests_table.sql
V20260620143000__add_status_column_to_leave_requests.sql
```

> **Never modify an existing migration file** once it has been applied. Flyway checksums each file — if you change one, startup will fail with a checksum mismatch error. Always create a new migration to alter existing tables.

---

## Step 5: Set Environment Variables

Flyway reads the DB connection from Spring's datasource config. Set these environment variables:

```bash
DB_URL=jdbc:postgresql://your-host:5432/your-db
DB_USERNAME=your-user
DB_PASSWORD=your-password
```

For Docker, pass them in your `docker-compose.yaml`:

```yaml
services:
  api:
    environment:
      SPRING_DATASOURCE_URL: ${DB_URL}
      SPRING_DATASOURCE_USERNAME: ${DB_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
```

---

## Step 6: Run the Application

Start your Spring Boot application. In the logs you should see:

```
INFO  o.f.c.FlywayExecutor - Database: jdbc:postgresql://... (PostgreSQL 18.4)
INFO  o.f.core.internal.command.DbValidate - Successfully validated 1 migration
INFO  o.f.core.internal.command.DbMigrate - Migrating schema "public" to version "1 - create leave types"
INFO  o.f.core.internal.command.DbMigrate - Successfully applied 1 migration to schema "public"
```

A `flyway_schema_history` table is created in your database to track applied migrations.

---

## Step 7: Adding More Migrations

Every time you need to change the database schema:

1. Create a new SQL file in `src/main/resources/db/migration/` with the next version
2. Write your SQL (`ALTER TABLE`, `CREATE TABLE`, `DROP COLUMN`, etc.)
3. Start the app — Flyway applies it automatically

```sql
-- V2__add_status_to_leave_types.sql
ALTER TABLE leave_types ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
```

---

## Common Mistakes to Avoid

| Mistake | What Happens | Fix |
|---|---|---|
| Only adding `flyway-core` without `flyway-database-postgresql` | Flyway silently skips PostgreSQL | Add both dependencies |
| Editing an existing migration file after it has been applied | App fails to start — checksum mismatch | Create a new migration instead |
| Using `ddl-auto=update` alongside Flyway | Hibernate may auto-alter tables, conflicting with Flyway | Set `ddl-auto=validate` |
| No `DB_URL` env variable set | App crashes on startup | Ensure all env vars are provided |
| Database already has tables but no `flyway_schema_history` | Flyway refuses to migrate ("non-empty schema") | Already handled by `baseline-on-migrate=true` |

---

## Summary

| What | Where |
|---|---|
| Dependencies | `flyway-core` + `flyway-database-postgresql` in `pom.xml` |
| Config | `application.properties` — `spring.flyway.*` |
| Migration files | `src/main/resources/db/migration/V{n}__{desc}.sql` |
| Runs automatically | Yes — on every app startup |
| History table | `flyway_schema_history` in your database |
| JPA setting | `spring.jpa.hibernate.ddl-auto=validate` |
