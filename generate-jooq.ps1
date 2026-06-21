Write-Host "Starting jOOQ code generation using Testcontainers..."
Write-Host "This will:"
Write-Host "  1. Start a PostgreSQL container using Testcontainers"
Write-Host "  2. Apply Flyway migrations to the container"
Write-Host "  3. Generate jOOQ code from the migrated schema"
Write-Host "  4. Clean up the container"
Write-Host ""

New-Item -ItemType Directory -Force -Path "target/generated-sources/jooq" | Out-Null

Write-Host "Compiling test classes..."
./mvnw process-test-resources compiler:testCompile "-Dmaven.main.skip=true" -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to compile test classes!"
    exit 1
}

Write-Host "Running jOOQ code generation..."
./mvnw "exec:java" "-Dexec.mainClass=com.womenconcern.api.codegen.TestcontainersJooqCodegen" "-Dexec.classpathScope=test" -q

$generatedPath = "target/generated-sources/jooq/com/womenconcern/api/jooq/generated"
if ((Test-Path $generatedPath) -and (Get-ChildItem $generatedPath)) {
    Write-Host ""
    Write-Host "jOOQ code generation completed successfully!"
    Write-Host "Generated code is available in: target/generated-sources/jooq"
} else {
    Write-Host ""
    Write-Host "jOOQ code generation failed! Please check the error messages above."
    exit 1
}
