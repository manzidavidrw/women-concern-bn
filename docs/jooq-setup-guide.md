# Setting Up jOOQ with Spring Boot and PostgreSQL

**Author:** Backend Team  
**Stack:** Spring Boot 3.x / 4.x · PostgreSQL · Maven · Flyway

---

## What is jOOQ?

jOOQ (Java Object Oriented Querying) generates Java classes directly from your database schema, giving you type-safe SQL queries at compile time. Instead of writing raw SQL strings that only fail at runtime, jOOQ catches errors before you even run the app.

**Without jOOQ:**
```java
// String-based — typos only caught at runtime
String sql = "SELECT * FROM leav_types"; // typo! won't fail until runtime
```

**With jOOQ:**
```java
// Type-safe — typo caught at compile time
dsl.selectFrom(Tables.LEAV_TYPES); // compiler error immediately
```

---

## Prerequisites

Before setting up jOOQ code generation, you need:
- Flyway configured and migrations applied (see the Flyway setup guide)
- A running PostgreSQL database with your schema already created by Flyway
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
    <version>3.19.35</version>
</dependency>

<!-- jOOQ code generator (used only during development) -->
<dependency>
    <groupId>org.jooq</groupId>
    <artifactId>jooq-codegen</artifactId>
    <version>3.19.35</version>
</dependency>

<!-- jOOQ metadata (used by the generator to read DB schema) -->
<dependency>
    <groupId>org.jooq</groupId>
    <artifactId>jooq-meta</artifactId>
    <version>3.19.35</version>
</dependency>
```

> You can define `<jooq.version>3.19.35</jooq.version>` in `<properties>` and reference it as `${jooq.version}` to keep versions consistent.

---

## Step 2: Configure the Generated Sources Directory

jOOQ will output generated Java files into a folder. You need to tell Maven to treat that folder as a source directory.

Add the `build-helper-maven-plugin` inside `<build><plugins>`:

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
                    <source>src/main/generated/jooq</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

> We use `src/main/generated/jooq` (not `target/`) so generated classes survive `mvn clean` and can be committed to git — which is required for Docker builds to work.

---

## Step 3: Create the Code Generator Class

Create the following class in `src/test/java/`:

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

public class JooqCodegen {

    private static final String URL      = "jdbc:postgresql://your-host:5432/your-db";
    private static final String USER     = "your-username";
    private static final String PASSWORD = "your-password";

    public static void main(String[] args) throws Exception {

        // Step 1: Run Flyway to ensure schema is up to date
        System.out.println("Running Flyway migrations...");
        Flyway.configure()
                .dataSource(URL, USER, PASSWORD)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();
        System.out.println("Flyway done.");

        // Step 2: Generate jOOQ classes from the current schema
        System.out.println("Generating jOOQ classes...");
        Configuration configuration = new Configuration()
                .withJdbc(new Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(URL)
                        .withUser(USER)
                        .withPassword(PASSWORD))
                .withGenerator(new Generator()
                        .withName("org.jooq.codegen.JavaGenerator")
                        .withDatabase(new Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withInputSchema("public"))
                        .withGenerate(new Generate()
                                .withRecords(true)
                                .withPojos(true))
                        .withTarget(new Target()
                                .withPackageName("com.yourcompany.yourapp.jooq.generated")
                                .withDirectory("src/main/generated/jooq")));

        GenerationTool.generate(configuration);
        System.out.println("jOOQ generation done.");
    }
}
```

### What this class does
1. Connects to your real database
2. Runs Flyway to make sure all migrations are applied
3. Reads your database schema (tables, columns, types, keys)
4. Generates Java classes that represent each table, record, and POJO

---

## Step 4: Run the Generator

From the terminal in your project root:

```bash
mvn test-compile exec:java \
  -Dexec.mainClass="com.yourcompany.yourapp.codegen.JooqCodegen" \
  -Dexec.classpathScope=test
```

Or from your IDE (IntelliJ / Eclipse):
- Open `JooqCodegen.java`
- Right-click → **Run 'JooqCodegen.main()'**

### What gets generated

After running, you will find these files under `src/main/generated/jooq/com/yourcompany/yourapp/jooq/generated/`:

```
generated/
├── DefaultCatalog.java          # top-level catalog
├── Public.java                  # your "public" schema
├── Tables.java                  # static references to all tables  ← use this
├── tables/
│   ├── LeaveTypes.java          # table definition
│   └── pojos/
│       └── LeaveTypes.java      # plain POJO
│   └── records/
│       └── LeaveTypesRecord.java  # jOOQ record (row)
└── ...
```

---

## Step 5: Commit the Generated Files

Unlike `target/`, files in `src/main/generated/` should be committed to git:

```bash
git add src/main/generated/
git commit -m "Add jOOQ generated sources"
```

This is important because:
- Docker builds compile from the committed source — they have no DB access
- Your teammates can build the project without running the generator themselves
- CI/CD pipelines work without needing a live database connection at build time

---

## Step 6: Use jOOQ in Your Services

Spring Boot's `spring-boot-starter-jooq` auto-configures a `DSLContext` bean from your datasource. Inject it anywhere:

```java
import com.yourcompany.yourapp.jooq.generated.Tables;
import com.yourcompany.yourapp.leave.entity.LeaveType;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveService {

    @Autowired
    private DSLContext dsl;

    // Fetch all rows and map to your JPA entity / DTO
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
}
```

### Key methods

| Method | Use |
|---|---|
| `.selectFrom(Tables.X)` | Select all columns from a table |
| `.select(Tables.X.COLUMN)` | Select specific columns |
| `.where(Tables.X.COL.eq(value))` | Filter rows |
| `.fetchInto(MyClass.class)` | Map result to a class |
| `.fetchOne()` | Fetch a single record |
| `.fetch()` | Fetch all as a jOOQ `Result` |

---

## Step 7: Regenerate After Schema Changes

Every time you add a new Flyway migration that changes the schema, re-run the generator:

```bash
mvn test-compile exec:java \
  -Dexec.mainClass="com.yourcompany.yourapp.codegen.JooqCodegen" \
  -Dexec.classpathScope=test
```

Then commit the updated generated files.

### Team workflow

```
1. Write new migration  →  V2__add_users_table.sql
2. Run JooqCodegen      →  new Users.java, UsersRecord.java generated
3. Use in service       →  dsl.selectFrom(Tables.USERS)...
4. Commit everything    →  migration file + generated sources
5. Push                 →  teammates pull, everything compiles
```

---

## Common Mistakes to Avoid

| Mistake | What Happens | Fix |
|---|---|---|
| Only adding `jooq-codegen` as a dependency but not running the generator | No classes generated | Run `JooqCodegen.main()` explicitly |
| Putting generated sources in `target/` | Docker build fails — `mvn clean` wipes them | Use `src/main/generated/jooq` and commit to git |
| Not running Flyway before the generator | Generator reads an outdated schema | Always migrate before generating (the `JooqCodegen` class does this automatically) |
| Referencing `LEAVE_TYPES` without import | Compiler error ("cannot find symbol") | Import from `Tables` — use `Tables.LEAVE_TYPES` |
| Casting `Result<XRecord>` to `List<MyEntity>` | Runtime `ClassCastException` | Use `.fetchInto(MyEntity.class)` instead |
| Forgetting `baselineOnMigrate(true)` on an existing DB | Flyway throws "non-empty schema, no history table" | Add `.baselineOnMigrate(true)` to `Flyway.configure()` |

---

## Summary

| What | Where / How |
|---|---|
| Dependencies | `jooq`, `jooq-codegen`, `jooq-meta`, `spring-boot-starter-jooq` |
| Generated sources dir | `src/main/generated/jooq` (committed to git) |
| Generator class | `src/test/java/.../codegen/JooqCodegen.java` |
| Run generator | `mvn test-compile exec:java -Dexec.mainClass=...JooqCodegen` |
| Use in code | Inject `DSLContext`, query via `Tables.YOUR_TABLE` |
| Regenerate | After every new Flyway migration |
| Commit generated files | Yes — required for Docker and team builds |
