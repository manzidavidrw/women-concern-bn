#!/bin/bash

echo "Starting jOOQ code generation using Testcontainers..."
echo "This will:"
echo "  1. Start a PostgreSQL container using Testcontainers"
echo "  2. Apply Flyway migrations to the container"
echo "  3. Generate jOOQ code from the migrated schema"
echo "  4. Clean up the container"
echo ""

mkdir -p target/generated-sources/jooq

echo "Compiling test classes..."
./mvnw clean process-test-resources compiler:testCompile -Dmaven.main.skip=true -q

if [ $? -ne 0 ]; then
    echo "Failed to compile test classes!"
    exit 1
fi

echo "Running jOOQ code generation..."
./mvnw exec:java -Dexec.mainClass="com.womenconcern.api.codegen.TestcontainersJooqCodegen" -Dexec.classpathScope="test" -q || true

if [ -d "target/generated-sources/jooq/com/womenconcern/api/jooq/generated" ] && [ "$(ls -A target/generated-sources/jooq/com/womenconcern/api/jooq/generated)" ]; then
    echo ""
    echo "jOOQ code generation completed successfully!"
    echo "Generated code is available in: target/generated-sources/jooq"
else
    echo ""
    echo "jOOQ code generation failed!"
    echo "Please check the error messages above."
    exit 1
fi
