package com.womenconcern.api.codegen;

import org.flywaydb.core.Flyway;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;

public class TestcontainersJooqCodegen {

    private static final String URL  = "jdbc:postgresql://ep-sparkling-wildflower-atliyvmb.c-9.us-east-1.aws.neon.tech/neondb?sslmode=require";
    private static final String USER = "neondb_owner";
    private static final String PASS = "npg_7gFRVJE6GAux";

    public static void main(String[] args) {
        try {
            System.out.println("Applying Flyway migrations...");
            Flyway.configure()
                    .dataSource(URL, USER, PASS)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .outOfOrder(true)
                    .validateOnMigrate(true)
                    .load()
                    .migrate();
            System.out.println("Flyway migrations applied successfully!");

            System.out.println("Generating jOOQ code...");
            Configuration configuration = new Configuration()
                    .withLogging(Logging.WARN)
                    .withJdbc(new Jdbc()
                            .withDriver("org.postgresql.Driver")
                            .withUrl(URL)
                            .withUser(USER)
                            .withPassword(PASS))
                    .withGenerator(new Generator()
                            .withName("org.jooq.codegen.JavaGenerator")
                            .withDatabase(new Database()
                                    .withName("org.jooq.meta.postgres.PostgresDatabase")
                                    .withInputSchema("public")
                                    .withExcludes("flyway.*")
                                    .withIncludeIndexes(false))
                            .withGenerate(new Generate()
                                    .withDeprecated(false)
                                    .withRecords(true)
                                    .withImmutablePojos(true)
                                    .withFluentSetters(true)
                                    .withImplicitJoinPathsToOne(true))
                            .withTarget(new Target()
                                    .withPackageName("com.womenconcern.api.jooq.generated")
                                    .withDirectory("target/generated-sources/jooq"))
                            .withStrategy(new Strategy()
                                    .withName("org.jooq.codegen.DefaultGeneratorStrategy")));

            GenerationTool.generate(configuration);
            System.out.println("jOOQ code generated successfully!");

            System.exit(0);
        } catch (Exception e) {
            System.err.println("jOOQ code generation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
