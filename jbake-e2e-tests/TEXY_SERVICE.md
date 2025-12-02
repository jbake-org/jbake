# Texy Service for E2E Tests

## Overview

End-to-end tests that require the Texy markup service should use the Docker image built from the `jbake-texy-service` module rather than maintaining duplicate Dockerfiles.

## Using the Texy Service in Tests

### Build the Docker Image

Before running tests that require the Texy service, build the Docker image:

```bash
# Using Gradle
./gradlew :jbake-texy-service:dockerBuild

# Using Maven
cd jbake-texy-service
mvn package
docker build -t jbake/texy-service:latest .
```

### Use in TestContainers

```kotlin
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

val texyContainer = GenericContainer("jbake/texy-service:latest")
    .withExposedPorts(8080)
    .waitingFor(Wait.forHttp("/").forStatusCode(200))
    .withStartupTimeout(Duration.ofMinutes(2))

texyContainer.start()

val serviceUrl = "http://${texyContainer.host}:${texyContainer.getMappedPort(8080)}/texy"
```

## Why This Approach?

Previously, the `jbake-e2e-tests` module contained duplicate copies of the Texy Dockerfile and PHP service. This has been removed to:

1. **Eliminate Duplication**: Single source of truth for the Texy service
2. **Simplify Maintenance**: Changes only need to be made in one place
3. **Consistent Testing**: All tests use the same Texy service image
4. **Follow Docker Best Practices**: Build once, use everywhere

## Service Details

- **Image**: `jbake/texy-service:latest`
- **Port**: 8080
- **Endpoint**: `POST /texy`
- **Source**: `jbake-texy-service` module

See the `jbake-texy-service/README.md` for more details about the service implementation.

