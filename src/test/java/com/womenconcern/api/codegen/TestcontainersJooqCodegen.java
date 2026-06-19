package com.womenconcern.api.codegen;

import org.flywaydb.core.Flyway;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;

public class TestcontainersJooqCodegen {

    private static final String URL = "jdbc:postgresql://ep-sparkling-wildflower-atliyvmb.c-9.us-east-1.aws.neon.tech/neondb?sslmode=require";
    private static final String USER = "neondb_owner";
    private static final String PASS = "npg_7gFRVJE6GAux";

    public static void main(String[] args) throws Exception {

        System.out.println("Starting Flyway...");

        Flyway.configure()
                .dataSource(URL, USER, PASS)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();

        System.out.println("Flyway completed");

        System.out.println("Generating jOOQ...");

        Configuration configuration = new Configuration()
                .withJdbc(new Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(URL)
                        .withUser(USER)
                        .withPassword(PASS))
                .withGenerator(new Generator()
                        .withName("org.jooq.codegen.JavaGenerator")
                        .withDatabase(new Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withInputSchema("public"))
                        .withGenerate(new Generate()
                                .withRecords(true)
                                .withPojos(true))
                        .withTarget(new Target()
                                .withPackageName("com.womenconcern.api.jooq.generated")
                                .withDirectory("target/generated-sources/jooq")));

        GenerationTool.generate(configuration);

        System.out.println("DONE");
    }
}