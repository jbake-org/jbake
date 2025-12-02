# JBake End-to-End Tests

This module contains end-to-end tests for JBake using TestContainers. The tests verify that JBake can successfully bake sites with different template engines and configurations.

## Overview

The tests use Docker containers to run JBake in an isolated environment, ensuring that the complete application works as expected in a production-like setup.

## Key Features

- **Parametrized Tests**: Tests run against multiple template engines (Freemarker, Groovy, Thymeleaf)
- **Shared Test Data**: Reuses existing test fixtures from `test-data/fixture`
- **Option Combinations**: Tests verify different JBake command-line options
- **Content Verification**: Tests validate that content is correctly rendered
- **TestContainers**: Uses Docker containers for isolated testing

## Prerequisites

- Docker must be installed and running (for full E2E tests)
- JDK 17
- JBake Docker image must be built (see below)

## Test Data

This module uses shared test data from `/test-data/fixture` which contains:
- Sample content (blog posts, pages)
- Templates for multiple engines (Freemarker, Groovy, Thymeleaf)
- Configuration files

The test data location is passed via system property `jbake.test.data.dir` and defaults to `../test-data/fixture`.

## Building the Docker Image

### Using Gradle (Recommended)

```bash
./gradlew :jbake-dist:jibDockerBuild
```

### Using Maven (with JDK 17)

```bash
export JAVA_HOME=/path/to/jdk-17
cd jbake-dist
mvn package jib:dockerBuild -DskipTests
```

This builds the shaded JAR and then creates the Docker image.

## Running the Tests

### Resource Verification Tests (Fast, No Docker)

These tests verify that the test data fixture is properly configured.

**Gradle:**
```bash
./gradlew :jbake-e2e-tests:test --tests "*TestResourcesVerificationTest"
```

**Maven (with JDK 17):**
```bash
export JAVA_HOME=/path/to/jdk-17
cd jbake-e2e-tests
mvn test -Dtest=TestResourcesVerificationTest
```

### Full E2E Tests (Requires Docker)

These tests run JBake in containers and verify the output.

**Gradle:**
```bash
./gradlew :jbake-e2e-tests:test
```

**Maven (with JDK 17):**
```bash
export JAVA_HOME=/path/to/jdk-17
cd jbake-e2e-tests
mvn test
```

## Test Structure

### Test Classes

- `JBakeEndToEndTest.kt` - Main E2E tests using TestContainers
  - Parametrized tests for multiple template engines
  - Tests with different JBake options
  - Content rendering verification

- `TestResourcesVerificationTest.kt` - Fast tests without Docker
  - Verifies test-data fixture structure
  - Checks template files exist
  - Validates configuration files

### Shared Test Data

Located in `/test-data/fixture` (shared across modules):
- `content/` - Sample blog posts and pages
- `freemarkerTemplates/` - Freemarker templates (.ftl)
- `groovyTemplates/` - Groovy templates (.gsp)
- `thymeleafTemplates/` - Thymeleaf templates (.thyme)
- `jbake.properties` - Configuration file
- `assets/` - Static assets

### Test Scenarios

1. **Template Engine Tests**: Verify JBake works with Freemarker, Groovy, and Thymeleaf
2. **Options Tests**: Test various command-line options and their combinations
3. **Content Rendering Tests**: Validate that content is correctly rendered to HTML using JSoup

## What the Tests Verify

- ✅ JBake successfully bakes sites with different template engines
- ✅ Output files are created in the expected locations
- ✅ Generated HTML contains expected content
- ✅ Posts and pages are correctly rendered
- ✅ Navigation and structure are properly generated
- ✅ Different JBake options work correctly

## Adding New Tests

To add new test cases:

1. Add new parametrized test methods in `JBakeEndToEndTest.kt`
2. Create new provider methods for test parameters
3. Add verification methods for new features

## Troubleshooting

### Docker Image Not Found

```bash
# Gradle
./gradlew :jbake-dist:jibDockerBuild

# Maven (with JDK 17)
export JAVA_HOME=/path/to/jdk-17
cd jbake-dist && mvn compile jib:dockerBuild
```

### Maven Compilation Errors

Ensure you're using JDK 17:
```bash
export JAVA_HOME=/path/to/jdk-17
export PATH=$JAVA_HOME/bin:$PATH
java -version  # Should show Java 17
```

### Test Data Not Found

Verify the shared test data exists:
```bash
ls -la ../test-data/fixture/
```

The location can be overridden with:
```bash
mvn test -Djbake.test.data.dir=/path/to/fixture
```
