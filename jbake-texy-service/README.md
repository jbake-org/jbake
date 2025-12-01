# JBake Texy Service

Docker container for the Texy markup processing service, with Maven integration for building, testing, and pushing to Docker Hub.

## Overview

This module provides:
- **Docker Image**: PHP-based Texy service ready to deploy
- **Maven Build**: Automated Docker image building
- **E2E Tests**: End-to-end tests using TestContainers
- **JBake Integration Tests**: Tests verifying JBake + Texy service integration
- **Docker Hub Push**: Automated publishing to Docker Hub

## Project Structure

```
jbake-texy-service/
├── Dockerfile                         # Docker image definition
├── pom.xml                            # Maven build configuration
├── src/
│   ├── main/
│   │   └── php/
│   │       └── texy-service.php       # Texy HTTP service implementation
│   └── test/
│       ├── kotlin/
│       │   └── org/jbake/texy/
│       │       ├── TexyServiceE2ETest.kt         # Service tests
│       │       └── JBakeTexyIntegrationTest.kt   # JBake integration tests
│       └── resources/
│           └── test-site/
│               ├── content/
│               │   └── test-post.texy           # Test content
│               └── jbake.properties             # Test configuration
```

## Building

### Build Docker Image

```bash
# Build the Docker image
mvn clean package

# Skip Docker build
mvn clean package -PskipDocker

# Build without tests
mvn clean package -DskipTests
```

This will create:
- `jbake/texy-service:5.0.0-rc1` (versioned)
- `jbake/texy-service:latest` (latest tag)

### Push to Docker Hub

```bash
# Set Docker Hub credentials
export DOCKERHUB_USERNAME=your-username
export DOCKERHUB_PASSWORD=your-password

# Build and push
mvn clean deploy -Pdocker-push

# Or push manually
docker push jbake/texy-service:5.0.0-rc1
docker push jbake/texy-service:latest
```

## Testing

### Run All Tests

```bash
mvn test
```

This will:
1. Build the Docker image
2. Start a container with the Texy service
3. Run end-to-end service tests
4. Run JBake integration tests
5. Verify service functionality

### Test Categories

**Service Tests** (`TexyServiceE2ETest.kt`):
- Container startup
- Health check endpoint
- Texy markup processing
- UTF-8 character handling
- Error responses
- Multiple requests

**Integration Tests** (`JBakeTexyIntegrationTest.kt`):
- JBake + Texy service integration
- `.texy` file processing
- Error handling
- Multiple file processing

## Docker Image

### Image Details

- **Base**: `php:8.2-cli`
- **Port**: 8080
- **Endpoint**: `/texy` (POST)
- **Health Check**: `/` (GET)

### Running the Image

```bash
# Run locally
docker run -d -p 8080:8080 --name texy jbake/texy-service:latest

# Test the service
curl -X POST http://localhost:8080/texy \
  -H "Content-Type: text/plain" \
  -d "This is **bold** text"

# View logs
docker logs texy

# Stop and remove
docker stop texy && docker rm texy
```

### Environment Variables

None required. The service runs with default settings:
- Port: 8080
- No authentication
- UTF-8 encoding

## Maven Plugins

### Dockerfile Maven Plugin

Handles Docker image building and pushing:

```xml
<plugin>
    <groupId>com.spotify</groupId>
    <artifactId>dockerfile-maven-plugin</artifactId>
    <version>1.4.13</version>
</plugin>
```

**Phases**:
- `package`: Build image and tag as latest
- `deploy`: Push image to Docker Hub

**Properties**:
- `docker.image.name`: Image name (default: `jbake/texy-service`)
- `docker.image.tag`: Image tag (default: project version)
- `skipDocker`: Skip Docker operations (default: false)

### Surefire Plugin

Runs tests with Docker integration:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
</plugin>
```

## Build Profiles & Properties

### Maven Profiles

#### skipDocker

Skip Docker operations:

```bash
mvn clean install -PskipDocker
```

#### docker-push

Enable Docker Hub push during deploy:

```bash
mvn clean deploy -Pdocker-push
```

### Gradle Properties

#### skipDocker

Skip Docker operations:

```bash
./gradlew build -PskipDocker=true
```

#### Custom Image Name/Tag

```bash
./gradlew dockerBuild \
  -Pdocker.image.name=myrepo/texy \
  -Pdocker.image.tag=custom-tag
```

Set in `gradle.properties`:
```properties
docker.image.name=myrepo/texy-service
dockerhub.username=your-username
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Push Texy Service

on:
  push:
    tags:
      - 'v*'

jobs:
  build-push:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '17'

      - name: Build and Push
        env:
          DOCKERHUB_USERNAME: ${{ secrets.DOCKERHUB_USERNAME }}
          DOCKERHUB_PASSWORD: ${{ secrets.DOCKERHUB_PASSWORD }}
        run: |
          cd jbake-texy-service
          mvn clean deploy -Pdocker-push
```

### GitLab CI Example

```yaml
stages:
  - build
  - test
  - deploy

build-texy-service:
  stage: build
  script:
    - cd jbake-texy-service
    - mvn clean package

test-texy-service:
  stage: test
  script:
    - cd jbake-texy-service
    - mvn test

push-texy-service:
  stage: deploy
  only:
    - tags
  script:
    - cd jbake-texy-service
    - mvn deploy -Pdocker-push
```

## Dependencies

### Runtime (Docker Image)
- PHP 8.2
- Composer
- Texy library (via Composer)

### Test Dependencies
- JUnit Jupiter
- Kotest
- TestContainers
- Apache HttpClient 5
- JBake Core

## Configuration

### Maven Properties

```xml
<properties>
    <!-- Docker image settings -->
    <docker.image.name>jbake/texy-service</docker.image.name>
    <docker.image.tag>${project.version}</docker.image.tag>

    <!-- Docker Hub credentials (from environment) -->
    <dockerhub.username>${env.DOCKERHUB_USERNAME}</dockerhub.username>
    <dockerhub.password>${env.DOCKERHUB_PASSWORD}</dockerhub.password>

    <!-- Skip options -->
    <skipTests>false</skipTests>
    <skipDocker>false</skipDocker>
</properties>
```

### Override Properties

```bash
# Custom image name
mvn package -Ddocker.image.name=myrepo/texy

# Custom tag
mvn package -Ddocker.image.tag=custom-tag

# Skip tests
mvn package -DskipTests=true
```

## Troubleshooting

### Docker Build Fails

**Problem**: Docker daemon not running or not accessible

**Solution**:
```bash
# Check Docker is running
docker info

# Start Docker daemon (Linux)
sudo systemctl start docker

# Or skip Docker build
mvn package -PskipDocker
```

### Tests Fail with TestContainers

**Problem**: TestContainers cannot connect to Docker

**Solution**:
```bash
# Set Docker socket permissions
sudo chmod 666 /var/run/docker.sock

# Or set environment variable
export DOCKER_HOST=unix:///var/run/docker.sock
```

### Push to Docker Hub Fails

**Problem**: Authentication error

**Solution**:
```bash
# Login manually first
docker login

# Or set credentials
export DOCKERHUB_USERNAME=your-username
export DOCKERHUB_PASSWORD=your-password
```

### Image Not Found in Tests

**Problem**: Tests cannot find the Docker image

**Solution**:
```bash
# Build the image first
mvn package

# Verify image exists
docker images | grep texy-service

# Or rebuild without cache
mvn clean package -DskipTests
mvn test
```

## Development

### Local Development

**Using Maven:**
```bash
# 1. Build image
mvn package -DskipTests

# 2. Run container
docker run -d -p 8080:8080 --name texy jbake/texy-service:latest

# 3. Run tests
mvn test

# 4. Cleanup
docker stop texy && docker rm texy
```

**Using Gradle:**
```bash
# 1. Build image
./gradlew :jbake-texy-service:assemble

# 2. Run container
docker run -d -p 8080:8080 --name texy jbake/texy-service:latest

# 3. Run tests
./gradlew :jbake-texy-service:test

# 4. Cleanup
docker stop texy && docker rm texy
```

### Modifying the Service

1. Edit `src/main/php/texy-service.php`
2. Rebuild image:
   - Maven: `mvn package -DskipTests`
   - Gradle: `./gradlew :jbake-texy-service:assemble`
3. Test manually: `docker run ...`
4. Run tests:
   - Maven: `mvn test`
   - Gradle: `./gradlew :jbake-texy-service:test`

### Gradle Helper Tasks

```bash
# Show Docker configuration
./gradlew :jbake-texy-service:dockerInfo

# Show all Docker tasks and examples
./gradlew :jbake-texy-service:dockerHelp
```

### Adding Tests

1. Create test in `src/test/kotlin/org/jbake/texy/`
2. Extend `FunSpec` (Kotest)
3. Use `@Testcontainers` annotation
4. Run: `mvn test`

## Related Modules

- **jbake-core**: Contains `TexyEngine` implementation
- **jbake-e2e-tests**: Other end-to-end tests (kept original Dockerfile copy there)

## License

Same as JBake (MIT License)

## Links

- [Texy Official Site](https://texy.info/)
- [TestContainers](https://www.testcontainers.org/)
- [Spotify Dockerfile Maven Plugin](https://github.com/spotify/dockerfile-maven)

