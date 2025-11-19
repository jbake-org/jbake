# JBake Maven Migration - Kotlin/Java Mixed Compilation Guide

## Overview

The JBake project contains a mix of Java and Kotlin source files in the same directories (`src/main/java`, `src/test/java`, etc.). Maven's standard compiler plugins require special configuration to handle this mixed setup correctly.

## Compilation Strategy

### Mixed Source Directories

The project uses:
- **Main sources**: Both `src/main/java` (Java) and `src/main/java` (Kotlin `.kt` files)
- **Test sources**: Both `src/test/java` (Java) and `src/test/java` (Kotlin `.kt` files)
- **Smoke test sources**: `src/smoke-test/java` (Kotlin `.kt` files in jbake-dist)

Note: While Kotlin files are in `java` directories, they are distinguished by the `.kt` extension.

### Compilation Order

Maven must compile Kotlin **before** Java because:
1. Kotlin code may call Java classes (no dependency)
2. Java code may call Kotlin code (requires Kotlin to compile first)

**Execution order configured in parent POM:**
1. **Phase: compile** → Kotlin Compiler (`kotlin-maven-plugin`)
2. **Phase: compile** → Java Compiler (`maven-compiler-plugin`)
3. **Phase: test-compile** → Kotlin Compiler (test)
4. **Phase: test-compile** → Java Compiler (test)

## POM Configuration

### Parent POM (pom.xml)

#### Kotlin Maven Plugin
```xml
<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <version>${kotlin.version}</version>
    <executions>
        <execution>
            <id>kotlin-compile</id>
            <phase>compile</phase>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
        <execution>
            <id>kotlin-test-compile</id>
            <phase>test-compile</phase>
            <goals>
                <goal>test-compile</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <jvmTarget>1.8</jvmTarget>
        <sourceDirs>
            <sourceDir>${project.basedir}/src/main/java</sourceDir>
            <sourceDir>${project.basedir}/src/main/kotlin</sourceDir>
        </sourceDirs>
        <testSourceDirs>
            <sourceDir>${project.basedir}/src/test/java</sourceDir>
            <sourceDir>${project.basedir}/src/test/kotlin</sourceDir>
        </testSourceDirs>
    </configuration>
</plugin>
```

**Key settings:**
- `<jvmTarget>1.8</jvmTarget>` - Targets Java 1.8 bytecode
- `<sourceDirs>` - Specifies both Java and Kotlin source directories
- `<testSourceDirs>` - Specifies both test directories

#### Java Compiler Plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <executions>
        <execution>
            <id>compile-after-kotlin</id>
            <phase>compile</phase>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
        <execution>
            <id>testCompile-after-kotlin</id>
            <phase>test-compile</phase>
            <goals>
                <goal>testCompile</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <excludes>
            <exclude>**/*.kt</exclude>
        </excludes>
    </configuration>
</plugin>
```

**Key settings:**
- `<excludes>` - Prevents compilation of `.kt` files (already handled by Kotlin compiler)
- Execution IDs clearly indicate "after-kotlin"

### Module-Specific Configuration

#### Build Helper Maven Plugin (All Modules)

Each module includes the build-helper plugin to ensure Maven properly recognizes source directories:

**jbake-core & jbake-maven-plugin:**
```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.5.0</version>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals>
                <goal>add-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>src/main/java</source>
                </sources>
            </configuration>
        </execution>
        <execution>
            <id>add-test-source</id>
            <phase>generate-test-sources</phase>
            <goals>
                <goal>add-test-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>src/test/java</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**jbake-dist (with smoke-test sources):**
```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.5.0</version>
    <executions>
        <execution>
            <phase>generate-test-sources</phase>
            <goals>
                <goal>add-test-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>src/smoke-test/java</source>
                </sources>
            </configuration>
        </execution>
        <execution>
            <id>add-test-resource</id>
            <phase>generate-test-resources</phase>
            <goals>
                <goal>add-test-resource</goal>
            </goals>
            <configuration>
                <resources>
                    <resource>
                        <directory>src/smoke-test/resources</directory>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Build Commands

### Clean Build
```bash
mvn clean compile
```
This runs Kotlin compiler first, then Java compiler on the mixed sources.

### Compile Specific Module
```bash
mvn -pl :jbake-core compile
```

### Full Build with Tests
```bash
mvn clean verify
```
This includes:
- Unit tests (via maven-surefire-plugin)
- Integration tests (via maven-failsafe-plugin for smoke tests)

### Build and Run Tests
```bash
mvn clean test
```

### Run Integration Tests Only
```bash
mvn failsafe:integration-test
```

## Troubleshooting

### Issue: Kotlin files not compiled
**Symptom:** "Cannot find symbol" errors for Kotlin classes when compiling Java files
**Solution:** Ensure Kotlin plugin is before Java compiler plugin in execution order. Verify phase is "compile" for both.

### Issue: Java files not compiled after Kotlin
**Symptom:** Changes in Java files not picked up
**Solution:** Check that Java compiler plugin execution has `<phase>compile</phase>` (or `test-compile`), not a later phase.

### Issue: Kotlin compiler can't find Java classes
**Symptom:** "Unresolved reference" in Kotlin for Java classes
**Solution:** Ensure Java sources are included in `<sourceDirs>` configuration in kotlin-maven-plugin.

### Issue: ClassNotFoundException for Kotlin classes at runtime
**Symptom:** Runtime error saying Kotlin class not found
**Solution:** Verify kotlin-stdlib-jdk8 dependency is included in project dependencies.

## Performance Considerations

The two-pass compilation (Kotlin, then Java) adds a small overhead compared to single-pass compilation. For large codebases:

1. Consider separating Kotlin and Java into different modules
2. Use `-T` flag for parallel module builds: `mvn -T 1C clean verify`
3. Enable incremental compilation if using recent Kotlin compiler versions

## Kotlin Version

Current configuration uses:
- **Kotlin Version**: 2.2.0
- **Kotlin BOM**: Managed in parent POM's dependencyManagement
- **JVM Target**: 1.8 (for Java 8 compatibility)
- **Stdlib**: kotlin-stdlib-jdk8

## Future Improvements

1. **Separate Source Trees**: Create distinct `src/main/kotlin` and `src/test/kotlin` directories
2. **Incremental Compilation**: Enable Kotlin incremental compilation for faster builds
3. **IDE Integration**: Ensure IDE (IntelliJ, VS Code) recognizes mixed sources properly
4. **Module Separation**: Consider refactoring to separate Kotlin and Java modules if codebase grows
5. **Build Analysis**: Use `mvn org.codehaus.mojo:clirr-maven-plugin` to track API changes

## References

- [Kotlin Maven Plugin Documentation](https://kotlinlang.org/docs/maven.html)
- [Maven Compiler Plugin](https://maven.apache.org/plugins/maven-compiler-plugin/)
- [Build Helper Maven Plugin](https://www.mojohaus.org/build-helper-maven-plugin/)
- [Mixed Java/Kotlin Projects](https://kotlinlang.org/docs/reference/using-maven.html#working-with-mixed-java-and-kotlin-projects)

