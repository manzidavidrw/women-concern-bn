# Setting Up jOOQ with Spring Boot and PostgreSQL

**Stack:** Spring Boot 3.x / 4.x · PostgreSQL · Maven · Flyway · Testcontainers

---

## What is jOOQ?

jOOQ (Java Object Oriented Querying) generates Java classes directly from your database schema, giving you type-safe SQL queries at compile time. Instead of writing raw SQL strings that only fail at runtime, jOOQ catches errors before you even run the app.

**Without jOOQ:**
```java
// Typo only caught at runtime
dsl.fetch("SELECT * FROM leav_types");
```

**With jOOQ:**
```java
// Compiler error immediately
dsl.selectFrom(Tables.LEAV_TYPES);
```

---

## Prerequisites

- Flyway configured and migration files written (see the Flyway setup guide)
- **Docker Desktop installed and running** — the code generator uses Testcontainers to spin up a temporary PostgreSQL container
- Maven

---

## Step 1: Add Dependencies

In your `pom.xml`, add inside `<dependencies>`:

```xml
<!-- jOOQ Spring Boot starter (auto-configures DSLContext) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jooq</artifactId>
</dependency>

<!-- jOOQ core -->
<dependency>
    <groupId>org.jooq</groupId>
    <artifactId>jooq</artifactId>
    <version>${jooq.version}</version>
</dependency>

<!-- jOOQ code generator -->
<dependency>
    <groupId>org.jooq</groupId>
    <artifactId>jooq-codegen</artifactId>
    <version>${jooq.version}</version>
</dependency>

<!-- jOOQ metadata -->
<dependency>
    <groupId>org.jooq</groupId>
    <artifactId>jooq-meta</artifactId>
    <version>${jooq.version}</version>
</dependency>

<!-- Testcontainers — for spinning up PostgreSQL during code generation -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.20.4</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.20.4</version>
    <scope>test</scope>
</dependency>
```

Define the jOOQ version in `<properties>`:

```xml
<properties>
    <jooq.version>3.19.35</jooq.version>
</properties>
```

---

## Step 2: Configure the Generated Sources Directory

Tell Maven to include the generated folder as a source directory. Add the `build-helper-maven-plugin` inside `<build><plugins>`:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>add-source</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>add-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>target/generated-sources/jooq</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## Step 3: Add the Maven Profile for Code Generation

Add this profile to your `pom.xml`. It wires the codegen class into the Maven lifecycle so you can trigger it with a single command:

```xml
<profiles>
    <profile>
        <id>jooq-generate</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.codehaus.mojo</groupId>
                    <artifactId>exec-maven-plugin</artifactId>
                    <version>3.1.0</version>
                    <executions>
                        <execution>
                            <id>generate-jooq</id>
                            <phase>test-compile</phase>
                            <goals>
                                <goal>java</goal>
                            </goals>
                            <configuration>
                                <mainClass>com.yourcompany.yourapp.codegen.JooqCodegen</mainClass>
                                <classpathScope>test</classpathScope>
                                <includeProjectDependencies>true</includeProjectDependencies>
                                <includePluginDependencies>true</includePluginDependencies>
                            </configuration>
                        </execution>
                    </executions>
                    <dependencies>
                        <dependency>
                            <groupId>org.postgresql</groupId>
                            <artifactId>postgresql</artifactId>
                            <version>${postgresql.version}</version>
                        </dependency>
                        <dependency>
                            <groupId>org.testcontainers</groupId>
                            <artifactId>testcontainers</artifactId>
                            <version>1.20.4</version>
                        </dependency>
                        <dependency>
                            <groupId>org.testcontainers</groupId>
                            <artifactId>postgresql</artifactId>
                            <version>1.20.4</version>
                        </dependency>
                        <dependency>
                            <groupId>org.jooq</groupId>
                            <artifactId>jooq-codegen</artifactId>
                            <version>${jooq.version}</version>
                        </dependency>
                        <dependency>
                            <groupId>org.jooq</groupId>
                            <artifactId>jooq-meta</artifactId>
                            <version>${jooq.version}</version>
                        </dependency>
                        <dependency>
                            <groupId>org.flywaydb</groupId>
                            <artifactId>flyway-core</artifactId>
                            <version>${flyway.version}</version>
                        </dependency>
                        <dependency>
                            <groupId>org.flywaydb</groupId>
                            <artifactId>flyway-database-postgresql</artifactId>
                            <version>${flyway.version}</version>
                        </dependency>
                    </dependencies>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

---

## Step 4: Create the Code Generator Class

Create this class in `src/test/java/`:

```
src/
└── test/
    └── java/
        └── com/yourcompany/yourapp/codegen/
            └── JooqCodegen.java
```

```java
package com.yourcompany.yourapp.codegen;

import org.flywaydb.core.Flyway;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class JooqCodegen {

    private static final String POSTGRES_IMAGE = "postgres:16.4-alpine3.20";
    private static final String DATABASE_NAME = "your_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";

    public static void main(String[] args) {
        try {
            try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(USERNAME)
                    .withPassword(PASSWORD)) {

                postgres.start();

                String jdbcUrl = postgres.getJdbcUrl();
                System.out.println("Started PostgreSQL container at: " + jdbcUrl);

                applyMigrations(jdbcUrl);
                generateJooqCode(jdbcUrl);

                System.out.println("jOOQ code generation completed successfully!");
                postgres.stop();
            }

            System.exit(0);

        } catch (Exception e) {
            System.err.println("jOOQ code generation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void applyMigrations(String jdbcUrl) {
        System.out.println("Applying Flyway migrations...");

        Flyway.configure()
                .dataSource(jdbcUrl, USERNAME, PASSWORD)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .outOfOrder(true)
                .validateOnMigrate(true)
                .load()
                .migrate();

        System.out.println("Flyway migrations applied successfully!");
    }

    private static void generateJooqCode(String jdbcUrl) throws Exception {
        System.out.println("Generating jOOQ code...");

        Configuration configuration = new Configuration()
                .withLogging(Logging.WARN)
                .withJdbc(new Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(jdbcUrl)
                        .withUser(USERNAME)
                        .withPassword(PASSWORD))
                .withGenerator(new Generator()
                        .withName("org.jooq.codegen.JavaGenerator")
                        .withDatabase(new Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withInputSchema("public")
                                .withExcludes("flyway.*")       // exclude Flyway history table
                                .withIncludeIndexes(false))
                        .withGenerate(new Generate()
                                .withDeprecated(false)
                                .withRecords(true)
                                .withImmutablePojos(true)
                                .withFluentSetters(true)
                                .withImplicitJoinPathsToOne(true))
                        .withTarget(new Target()
                                .withPackageName("com.yourcompany.yourapp.jooq.generated")
                                .withDirectory("target/generated-sources/jooq"))
                        .withStrategy(new Strategy()
                                .withName("org.jooq.codegen.DefaultGeneratorStrategy")));

        GenerationTool.generate(configuration);
        System.out.println("jOOQ code generated successfully!");
    }
}
```

### Why Testcontainers?

- **No hardcoded credentials** — no real database URL in source code
- **Always consistent** — uses the exact same migrations as production
- **Works for everyone** — any developer with Docker can generate without setting up a local DB
- **Clean slate** — fresh container every time, no leftover data issues

---

## Step 5: Run the Generator

> **Docker Desktop must be running** before executing this command.

**On Windows (PowerShell):**
```powershell
mvn test-compile "exec:java" "-Dexec.mainClass=com.yourcompany.yourapp.codegen.JooqCodegen" "-Dexec.classpathScope=test"
```

**Or use the PowerShell script** (`generate-jooq.ps1`):
```powershell
./generate-jooq.ps1
```

**On Linux/Mac:**
```bash
./generate-jooq.sh
```

### What happens when you run it

1. Testcontainers pulls and starts a `postgres:16.4-alpine3.20` Docker container
2. Flyway applies all your migration files to that container
3. jOOQ reads the resulting schema and generates Java classes
4. The container is destroyed
5. Generated files appear in `target/generated-sources/jooq/`

### What gets generated

```
target/generated-sources/jooq/
└── com/yourcompany/yourapp/jooq/generated/
    ├── DefaultCatalog.java
    ├── Public.java
    ├── Tables.java              ← use this to reference tables
    ├── Keys.java
    └── tables/
        ├── LeaveTypes.java      ← table definition
        ├── pojos/
        │   └── LeaveTypes.java  ← immutable POJO
        └── records/
            └── LeaveTypesRecord.java  ← jOOQ record (one row)
```

---

## Step 6: Use jOOQ in Your Services

Spring Boot's `spring-boot-starter-jooq` auto-configures a `DSLContext` bean. Inject it directly:

```java
import com.yourcompany.yourapp.jooq.generated.Tables;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final DSLContext dsl;

    // Fetch all and map to your entity/DTO
    public List<LeaveType> getLeaveTypes() {
        return dsl
                .selectFrom(Tables.LEAVE_TYPES)
                .fetchInto(LeaveType.class);
    }

    // Select specific columns
    public List<String> getLeaveTypeNames() {
        return dsl
                .select(Tables.LEAVE_TYPES.NAME)
                .from(Tables.LEAVE_TYPES)
                .fetchInto(String.class);
    }

    // With a WHERE condition
    public List<LeaveType> getPaidLeaveTypes() {
        return dsl
                .selectFrom(Tables.LEAVE_TYPES)
                .where(Tables.LEAVE_TYPES.IS_PAID.isTrue())
                .fetchInto(LeaveType.class);
    }

    // With pagination
    public List<LeaveType> getLeaveTypesPaged(int offset, int limit) {
        return dsl
                .selectFrom(Tables.LEAVE_TYPES)
                .orderBy(Tables.LEAVE_TYPES.NAME.asc())
                .limit(limit)
                .offset(offset)
                .fetchInto(LeaveType.class);
    }
}
```

### Key DSLContext methods

| Method | Use |
|---|---|
| `.selectFrom(Tables.X)` | Select all columns from a table |
| `.select(Tables.X.COLUMN)` | Select specific columns |
| `.where(Tables.X.COL.eq(value))` | Filter rows |
| `.fetchInto(MyClass.class)` | Map result to a class |
| `.fetchOne()` | Fetch a single record (null if not found) |
| `.fetchOneInto(MyClass.class)` | Fetch single record mapped to a class |
| `.fetch()` | Fetch all as a jOOQ `Result` |

---

## Step 7: Docker Build Workflow

Since `target/` is gitignored, the generated sources are **not committed to git**. For Docker builds to work, you must generate the classes locally before building the image. The `Dockerfile` copies the `target/` folder in before packaging:

```dockerfile
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src
COPY target target          # brings in pre-generated jOOQ classes

RUN ./mvnw package -DskipTests   # no "clean" — preserves target/
```

### Full workflow before deploying

```
1. Add a new migration file  →  V2__add_users_table.sql
2. Generate jOOQ classes     →  ./generate-jooq.ps1  (Docker must be running)
3. Build Docker image        →  docker compose up --build
```

---

## Step 8: Regenerate After Schema Changes

Every time you add a new Flyway migration, re-run the generator:

```powershell
# Windows
./generate-jooq.ps1

# Linux/Mac
./generate-jooq.sh
```

### Team workflow summary

```
Developer adds V2__add_users_table.sql
         ↓
Run ./generate-jooq.ps1
         ↓
New Users.java, UsersRecord.java appear in target/
         ↓
Use in service: dsl.selectFrom(Tables.USERS)...
         ↓
Run docker compose up --build to deploy
```

---

## Common Mistakes to Avoid

| Mistake | What Happens | Fix |
|---|---|---|
| Docker Desktop not running when generating | Testcontainers fails to start | Start Docker Desktop first |
| Running `mvn clean` before Docker build | Wipes generated sources from `target/` | Generate again before building |
| Adding `mvn clean` inside Dockerfile | Build fails — generated sources gone | Use `package -DskipTests` without `clean` |
| Casting jOOQ `Result` to `List<MyEntity>` | Runtime `ClassCastException` | Use `.fetchInto(MyEntity.class)` |
| Referencing table without import | Compiler error "cannot find symbol" | Import `Tables` and use `Tables.YOUR_TABLE` |
| Not regenerating after new migration | New table/column not available in code | Always re-run generator after adding migrations |

---

## Summary

| What | Where / How |
|---|---|
| Dependencies | `jooq`, `jooq-codegen`, `jooq-meta`, `spring-boot-starter-jooq`, `testcontainers` |
| Generator class | `src/test/java/.../codegen/JooqCodegen.java` |
| Generated sources | `target/generated-sources/jooq/` (gitignored) |
| Run generator (Windows) | `./generate-jooq.ps1` — Docker must be running |
| Run generator (Linux/Mac) | `./generate-jooq.sh` — Docker must be running |
| Use in code | Inject `DSLContext`, query via `Tables.YOUR_TABLE` |
| Regenerate when | After every new Flyway migration |
| Before Docker build | Run the generator first, then `docker compose up --build` |
