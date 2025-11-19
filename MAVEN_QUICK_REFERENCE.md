# JBake Maven - Quick Reference Cheat Sheet

## Essential Commands

```bash
# Build
mvn clean compile          # Compile only
mvn clean package          # Build JAR/distribution
mvn clean verify           # Full build with tests
mvn clean install          # Build and install to local repo

# Build specific module
mvn -pl :jbake-core compile
mvn -pl :jbake-dist package
mvn -pl :jbake-maven-plugin verify

# Skip tests
mvn package -DskipTests
mvn clean verify -DskipTests

# Parallel build (threads)
mvn -T 1C clean verify     # 1 thread per core
```

## Testing

```bash
# Unit tests only
mvn test

# All tests (unit + integration)
mvn verify

# Specific test
mvn test -Dtest=MainTest

# Skip tests during package
mvn package -DskipTests

# Smoke tests only (jbake-dist)
mvn -pl :jbake-dist verify

# With coverage report
mvn verify jacoco:report
# Open: target/site/jacoco/index.html
```

## Code Quality

```bash
# Generate coverage report
mvn jacoco:report

# Check code style
mvn checkstyle:check

# Generate checkstyle report
mvn checkstyle:checkstyle
# Open: target/site/checkstyle.html

# Full quality check
mvn verify jacoco:report checkstyle:check
```

## Modules

```bash
# List modules
mvn -pl help:active-profiles

# Build all
mvn clean verify

# Build specific module
mvn -pl :jbake-core clean verify

# Build module and dependencies
mvn -pl jbake-core -am clean verify

# Build module and dependents
mvn -pl jbake-core -amd clean verify
```

## Cleaning

```bash
# Clean build directory
mvn clean

# Clean and build
mvn clean compile

# Remove local jbake artifacts
rm -rf ~/.m2/repository/org/jbake/

# Clean all (with cache clear)
rm -rf ~/.m2/repository/org/jbake && mvn clean
```

## Dependencies

```bash
# Show dependency tree
mvn dependency:tree

# Show for specific module
mvn -pl :jbake-core dependency:tree

# Check for conflicts
mvn dependency:tree -Dverbose

# Resolve plugins
mvn help:describe -Dplugin=org.apache.maven.plugins:maven-compiler-plugin

# Update dependencies
mvn versions:display-dependency-updates
```

## Packaging & Distribution

```bash
# Build core library JAR
mvn -pl :jbake-core package

# Build distribution
mvn -pl :jbake-dist package
# Outputs: ZIP, TAR, JAR

# Build Maven plugin
mvn -pl :jbake-maven-plugin package

# Build everything
mvn package
```

## Release & Deploy

```bash
# Snapshot to local repository
mvn install

# Deploy to Sonatype OSSRH (requires credentials & GPG)
mvn clean deploy -P release

# Dry run without signing
mvn clean deploy -P release -Darguments="-DskipSigning"

# Stage for release (Maven Release Plugin)
mvn release:prepare
mvn release:perform
```

## Plugin Goals

```bash
# Compiler
mvn compiler:compile         # Compile
mvn compiler:testCompile     # Test compile

# Surefire (unit tests)
mvn surefire:test
mvn surefire:test -Dtest=MainTest

# Failsafe (integration tests)
mvn failsafe:integration-test
mvn failsafe:verify

# JAR
mvn jar:jar                  # Create JAR

# Assembly (distribution)
mvn assembly:single         # Create distribution

# JaCoCo
mvn jacoco:report           # Generate coverage
mvn jacoco:prepare-agent    # For custom test runs

# Help
mvn help:describe -Dplugin=org.jbake:jbake-maven-plugin
```

## Common Issues & Fixes

```bash
# OutOfMemoryError during build
export MAVEN_OPTS="-Xmx3g"

# Can't find symbol (Kotlin issue)
mvn clean compile  # Ensures Kotlin compiles first

# Plugin not found
mvn help:describe -Dplugin=:jbake-maven-plugin

# Slow builds
mvn -T 1C clean verify  # Parallel threads

# Dependency version conflicts
mvn dependency:tree -Dverbose

# Clear everything and rebuild
rm -rf ~/.m2/repository/org/jbake && mvn clean verify
```

## Environment Variables

```bash
# More memory for builds
export MAVEN_OPTS="-Xmx3g -XX:MaxPermSize=512m"

# Debug mode
export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# Verbose output
mvn -X clean verify

# Quiet mode
mvn -q clean verify
```

## Project Structure

```
jbake/
├── pom.xml                    ← Start here (parent)
├── jbake-core/pom.xml
├── jbake-dist/pom.xml
├── jbake-maven-plugin/pom.xml
├── MAVEN_MIGRATION_COMPLETE.md  ← Full guide
├── KOTLIN_COMPILATION.md         ← Kotlin setup
└── Documentation files
```

## POM Reference

### Properties
```xml
<properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <kotlin.version>2.2.0</kotlin.version>
    <junit5.version>5.8.2</junit5.version>
</properties>
```

### BOM Import
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-bom</artifactId>
            <version>${kotlin.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Dependency
```xml
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <!-- Version inherited from parent -->
</dependency>
```

## Version Info

- **Maven**: 3.9.x+
- **Java**: 1.8+
- **Kotlin**: 2.2.0
- **JUnit**: 5.8.2

## Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM, dependency mgmt |
| `jbake-core/pom.xml` | Core library |
| `jbake-dist/pom.xml` | CLI distribution |
| `jbake-maven-plugin/pom.xml` | Maven plugin |
| `config/checkstyle/checkstyle.xml` | Code style rules |
| `jbake-dist/src/assembly/distribution.xml` | Distribution config |

## Help & Resources

```bash
# Built-in help
mvn help:describe -Dplugin=org.apache.maven.plugins:maven-compiler-plugin

# List available goals for plugin
mvn help:describe -Dplugin=:maven-compiler-plugin -Ddetail

# JBake plugin help
mvn help:describe -Dplugin=org.jbake:jbake-maven-plugin

# Full POM
mvn help:effective-pom

# Effective settings
mvn help:effective-settings
```

## Tips & Tricks

```bash
# Skip javadoc generation (faster)
mvn package -Dskip.javadoc=true

# Skip all quality checks
mvn package -DskipTests -Dskip.jacoco=true -Dskip.checkstyle=true

# Resume build from failure point
mvn -rf :jbake-dist clean verify

# Show download progress
mvn -B clean verify

# Build in offline mode (no internet)
mvn -o clean compile

# Show effective POM
mvn help:effective-pom > effective-pom.xml
```

---
**Last Updated**: November 19, 2025
**JBake Version**: 2.7.0-SNAPSHOT
**Maven**: 3.9.11

