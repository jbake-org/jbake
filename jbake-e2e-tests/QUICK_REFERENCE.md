# Quick Reference: JBake E2E Tests

### Build with Gradle

```bash
# Run tests (no Docker needed for resource verification)
./gradlew :jbake-e2e-tests:test --tests "*TestResourcesVerificationTest"

# Build Docker image (for full E2E tests)
./gradlew :jbake-dist:jibDockerBuild

# Run all tests (requires Docker)
./gradlew :jbake-e2e-tests:test
```

### With Maven (Requires JDK 17)

```bash
# Setup
export JAVA_HOME=.../.jdks/temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH

# Run tests
mvn -pl jbake-e2e-tests test -Dtest=TestResourcesVerificationTest

# Build Docker image
cd ../jbake-dist
mvn compile jib:dockerBuild

# Run all tests (requires Docker)
cd ../jbake-e2e-tests
mvn test
```

## Key Configuration

### System Property
- `jbake.test.data.dir`: Location of test fixture
- Default: `../test-data/fixture`
- Configurable per-test run

### Docker Image
- Name: `jbake/jbake:2.7.0-SNAPSHOT`
- Base: `eclipse-temurin:21-jre-alpine`
- Built with: Jib plugin

### Test Engines
- Freemarker (.ftl)
- Groovy (.gsp)
- Thymeleaf (.thyme)

## Troubleshooting

### "Test data directory not found"
```bash
# Verify fixture exists
ls -la test-data/fixture/

# If missing, copy from jbake-core
cp -r jbake-core/src/test/resources/fixture test-data/
```

### Maven compilation errors
```bash
# Ensure JDK 17
java -version  # Must be 17.x

# Set correct JAVA_HOME
export JAVA_HOME=/path/to/jdk-17
export PATH=$JAVA_HOME/bin:$PATH
```

### Docker image not found
```bash
# Build with Gradle
./gradlew :jbake-dist:jibDockerBuild

# Or with Maven
cd jbake-dist && mvn compile jib:dockerBuild
```

## Testing Workflow

1. **Fast Verification** (no Docker, ~5 seconds)
   ```bash
   ./gradlew :jbake-e2e-tests:test --tests "*TestResourcesVerificationTest"
   ```

2. **Build Docker Image** (if needed)
   ```bash
   ./gradlew :jbake-dist:jibDockerBuild
   ```

3. **Full E2E Tests** (with Docker, ~2-5 minutes)
   ```bash
   ./gradlew :jbake-e2e-tests:test
   ```

## CI/CD Integration

```yaml
# Example GitHub Actions / GitLab CI
- name: Run E2E Tests
  run: |
    # Build Docker image
    ./gradlew :jbake-dist:jibDockerBuild

    # Run tests
    ./gradlew :jbake-e2e-tests:test
```

## Adding New Template Engine

1. Add templates to `test-data/fixture/myEngineTemplates/`
2. Update `templateEngineProvider()` in `JBakeEndToEndTest.kt`:
   ```kotlin
   Arguments.of("myengine", "myEngineTemplates")
   ```
3. Run tests to verify
