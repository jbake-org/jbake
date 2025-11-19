# Gradle to Maven Migration

This document describes the migration of the JBake project from Gradle to Maven.

## Project Structure

The JBake project is a multi-module Maven project with the following structure:

```
jbake/
├── pom.xml                          # Parent POM
├── jbake-core/pom.xml               # Core library module
├── jbake-dist/pom.xml               # Distribution/CLI module
└── jbake-maven-plugin/pom.xml       # Maven plugin module
```

## Key Changes

### 1. Parent POM (`pom.xml`)

The parent POM consolidates all project metadata and dependency management:

- **Modules**: Declares all three modules (jbake-core, jbake-dist, jbake-maven-plugin)
- **Dependency Management**: Uses BOMs (Bill of Materials) for Kotlin and JUnit 5 for version consistency
- **Common Plugins**: Configured at parent level:
  - Maven Compiler Plugin
  - Surefire Plugin (testing)
  - JaCoCo (code coverage)
  - Checkstyle (code quality)
  - Kotlin Maven Plugin
  - Source and Javadoc plugins
  - GPG signing for releases

### 2. Dependency Management

All dependencies are managed in the parent POM's `<dependencyManagement>` section:

- **BOMs Imported**:
  - `org.jetbrains.kotlin:kotlin-bom` (v2.2.0)
  - `org.junit:junit-bom` (v5.8.2)

- **Optional Dependencies**: Marked with `<optional>true</optional>` for template engines:
  - Asciidoctor
  - Groovy/Groovy Templates
  - FreeMarker
  - Thymeleaf
  - Jade4j
  - Flexmark
  - Pebble

- **All versions** are centralized in the parent POM's properties section for easy updates

### 3. Module Details

#### jbake-core
- **Purpose**: Core JBake library
- **Key Dependencies**:
  - Commons libraries (IO, Configuration, Lang, VFS2)
  - Template engines (optional)
  - OrientDB
  - JSoup
  - Logging (SLF4J, Logback)
  - Kotlin stdlib
- **Packaging**: JAR
- **License**: MIT

#### jbake-dist
- **Purpose**: Command-line distribution
- **Key Dependencies**:
  - jbake-core (project dependency)
  - AsciidoctorJ Diagram
  - Kotlin stdlib
- **Build Plugins**:
  - Apache Assembly Plugin: Creates ZIP and TAR distributions
  - AppAssembler Plugin: Generates startup scripts
  - Failsafe Plugin: Integration tests (smoke tests)
- **Packaging**: JAR
- **License**: MIT

#### jbake-maven-plugin
- **Purpose**: Maven plugin for running JBake
- **Key Dependencies**:
  - jbake-core (project dependency)
  - Maven Core (provided scope)
  - Maven Plugin Tools Annotations (provided scope)
  - All template engines (included by default, unlike jbake-core)
  - Spark Core
- **Build Plugins**:
  - Maven Plugin Plugin: Generates plugin descriptor and help Mojo
  - Maven Invoker Plugin: Integration tests
- **Packaging**: maven-plugin
- **License**: Apache 2.0

## Build Commands

### Basic Build
```bash
mvn clean install
```

### Build Specific Module
```bash
mvn -pl :jbake-core clean install
```

### Run Tests
```bash
mvn test
mvn verify  # Includes integration tests (smoke tests)
```

### Generate Distribution
```bash
mvn -pl :jbake-dist package
```

### Generate Plugin Descriptor
```bash
mvn -pl :jbake-maven-plugin clean package
```

### Code Quality
```bash
mvn jacoco:report              # Generate code coverage report
mvn checkstyle:check           # Run Checkstyle validation
mvn checkstyle:checkstyle-aggregate  # Aggregate checkstyle report
```

### Release Build (with GPG signing)
```bash
mvn clean deploy -P release
```

## Configuration Files

### Parent POM Properties
The parent POM defines all versions as properties for easy centralization:

```properties
<properties>
  <maven.compiler.source>1.8</maven.compiler.source>
  <maven.compiler.target>1.8</maven.compiler.target>

  <!-- Runtime dependencies versions -->
  <asciidoctorj.version>2.5.7</asciidoctorj.version>
  <groovy.version>3.0.9</groovy.version>
  <!-- ... more versions ... -->

  <!-- Testing dependencies versions -->
  <junit5.version>5.8.2</junit5.version>
  <mockito.version>4.2.0</mockito.version>
  <!-- ... more versions ... -->
</properties>
```

## Test Configuration

### Unit Tests
- **Framework**: JUnit 5 (Jupiter) with JUnit 4 compatibility via Vintage Engine
- **Assertions**: AssertJ
- **Mocking**: Mockito
- **Extensions**: JUnit Pioneer

### Integration Tests
- **jbake-dist**: Smoke tests via Failsafe Plugin
- **jbake-maven-plugin**: Tests via Maven Invoker Plugin

### JVM Arguments for Tests
All test tasks use:
```
-Xms512m -Xmx3g -Dorientdb.installCustomFormatter=false -Djna.nosys=true -XX:MaxDirectMemorySize=2g
```

## Publishing

### Distribution Management
- **Repository Type**: Sonatype OSS (OSSRH)
- **Snapshot URL**: https://s01.oss.sonatype.org/content/repositories/snapshots
- **Release URL**: https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/

### GPG Signing
- Configured in the `release` profile
- Uses loopback pinentry mode
- Requires GPG key setup

## Differences from Gradle

| Aspect | Gradle | Maven |
|--------|--------|-------|
| Build Files | build.gradle, settings.gradle | pom.xml (hierarchical) |
| Dependency Management | Centralized in gradle.properties | Centralized in parent `<dependencyManagement>` |
| BOMs | Via platform() | Via import scope |
| Optional Dependencies | Via optional flag | Via `<optional>true</optional>` |
| Multi-module | Configured in settings.gradle | Configured in parent POM `<modules>` |
| Distribution | Custom tasks | Apache Assembly Plugin |
| Plugin Packaging | Custom configuration | maven-plugin packaging type |
| Code Coverage | Gradle JaCoCo plugin | Maven JaCoCo plugin |
| Testing | Custom test sources | Standard src/test/java layout |

## Kotlin Support

The project uses Kotlin for some code. Maven configuration includes:

- **Kotlin BOM**: Managed through parent POM's dependencyManagement
- **Kotlin Maven Plugin**: Configured in parent POM for compilation
- **Kotlin Stdlib JDK8**: Included as dependency for JDK 8 compatibility
- **JVM Target**: 1.8

## System Requirements

- **Maven**: 3.9.x (tested with 3.9.11)
- **Java**: 1.8+ (tested with Java 25)
- **JVM Memory**: Set `-Xmx3g` for builds requiring more memory

## Notes

1. **Managed Dependencies**: All dependencies are defined in the parent POM's dependencyManagement. Child POMs reference them without version tags.

2. **One-liner Dependencies**: All `<dependency>` elements are kept as one-liners for consistency and readability.

3. **Property Scope**: While some properties are duplicated in child POMs, they inherit from parent. The parent pom.xml is the source of truth.

4. **Custom Plugins**: The custom Gradle convention plugin (`org.jbake.convention.java-common`) has been replaced with inherited plugin configuration in the parent POM.

5. **Release Process**: The Gradle release.gradle (jreleaser configuration) is not directly ported. The release process should be managed through standard Maven release plugins or CI/CD pipelines.

## Migration Checklist

- [x] Created parent POM with all modules
- [x] Migrated all dependencies with versions
- [x] Set up dependency management with BOMs
- [x] Configured common build plugins
- [x] Set up test configuration
- [x] Configured distribution packaging
- [x] Set up Maven plugin packaging
- [x] Configured GPG signing
- [x] Validated POM structure

## Future Enhancements

1. Set up Maven Release Plugin for automated releases
2. Configure Maven Site Plugin for project documentation
3. Set up CI/CD pipeline (GitHub Actions) for automated builds
4. Configure SonarQube integration via properties
5. Add Maven Enforcer Plugin for build policy enforcement

