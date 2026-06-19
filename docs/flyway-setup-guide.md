# Setting Up Flyway with Spring Boot and PostgreSQL

**Author:** Backend Team  
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

Spring Boot manages the Flyway version automatically via its BOM (Bill of Materials), so no version number is needed.

---

## Step 2: Configure the Database Connection

In `src/main/resources/application.properties`, add your database configuration:

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

# Optional: see Flyway logs during startup
logging.level.org.flywaydb=DEBUG
```

If you use `application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  flyway:
    enabled: true
    locations: classpath:db/migration
    schemas: public
    default-schema: public

logging:
  level:
    org.flywaydb: DEBUG
```

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

Flyway looks for files in `classpath:db/migration` by default (matching what you configured above).

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
| `{version}` | `1`, `2`, `1.1` | Must increase with each migration |
| `__` | `__` | Two underscores |
| `{description}` | `create_leave_types` | Words separated by underscores |

**Examples:**
```
V1__create_leave_types.sql
V2__add_leave_requests_table.sql
V3__add_status_column_to_leave_requests.sql
V4__create_users_table.sql
```

> **Never modify an existing migration file** once it has been applied. Flyway checksums each file — if you change one, it will fail on the next startup. Always create a new migration to alter existing tables.

---

## Step 5: Set Environment Variables

Flyway reads the DB connection from Spring's datasource config, so you just need to set:

```bash
DB_URL=jdbc:postgresql://your-host:5432/your-db
DB_USERNAME=your-user
DB_PASSWORD=your-password
```

For Docker, pass them in your `docker-compose.yml`:

```yaml
services:
  app:
    environment:
      DB_URL: jdbc:postgresql://db:5432/mydb
      DB_USERNAME: myuser
      DB_PASSWORD: mypassword
```

---

## Step 6: Run the Application

Start your Spring Boot application. In the logs you should see:

```
INFO  o.f.c.FlywayExecutor - Database: jdbc:postgresql://... (PostgreSQL 18.4)
INFO  o.f.core.internal.command.DbValidate - Successfully validated 1 migration
INFO  o.f.core.internal.command.DbMigrate - Current version of schema "public": << Empty Schema >>
INFO  o.f.core.internal.command.DbMigrate - Migrating schema "public" to version "1 - create leave types"
INFO  o.f.core.internal.command.DbMigrate - Successfully applied 1 migration to schema "public"
```

A `flyway_schema_history` table is created in your database to track applied migrations.

---

## Step 7: Adding More Migrations

Every time you need to change the database schema:

1. Create a new SQL file in `src/main/resources/db/migration/` with the next version number
2. Write your SQL (ALTER TABLE, CREATE TABLE, DROP COLUMN, etc.)
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
| Editing an existing migration file | App fails to start (checksum mismatch) | Create a new migration instead |
| Using `spring.jpa.hibernate.ddl-auto=update` alongside Flyway | Hibernate may auto-alter tables, conflicting with Flyway | Set `ddl-auto=validate` or `none` in production |
| No `DB_URL` env variable set | App crashes on startup | Ensure all env vars are provided |
| Database already has tables but no `flyway_schema_history` | Flyway refuses to migrate ("non-empty schema") | Set `spring.flyway.baseline-on-migrate=true` once |

---

## Summary

| What | Where |
|---|---|
| Dependencies | `pom.xml` — `flyway-core` + `flyway-database-postgresql` |
| Config | `application.properties` — `spring.flyway.*` |
| Migration files | `src/main/resources/db/migration/V{n}__{desc}.sql` |
| Runs automatically | Yes — on every app startup |
| History table | `flyway_schema_history` in your database |
