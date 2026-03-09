# OrientDB Removal & Java 21 Migration

## Overview

Two major changes in a single effort:

1. **Removed OrientDB** — replaced with a simple in-memory `ArrayList<DocumentModel>` + Java Streams
2. **Migrated to Java 21** — upgraded Gradle, all dependencies, and build conventions

---

## OrientDB Removal

OrientDB was used as an embedded database for content caching, indexing, and template queries. It added ~38MB of JAR dependencies and complexity for what is essentially an in-memory data store during site generation.

### What Changed

- **`ContentStore`** — completely rewritten. 12 SQL queries replaced with Java Stream operations on `ArrayList<DocumentModel>`. Lifecycle methods (`startup`, `close`, `shutdown`) are now no-ops.
- **`DBUtil`** — removed OrientDB-specific type handling (`OResult`, `OTrackedList`). Factory creates plain `ContentStore` instances.
- **`DocumentList`** — simplified to a plain `LinkedList<T>` subclass, removing `OResultSet` wrapping.
- **`Crawler`**, **`Oven`** — removed OrientDB imports and `updateSchema()` calls.
- **`JadeTemplateEngine`**, **`ThymeleafTemplateEngine`** — migrated `commons-lang` → `commons-lang3` (was a transitive dependency of OrientDB).
- **Build files** — removed `orientdb-core` dependency, `orientDbVersion` property, OrientDB JVM args, JaCoCo exclusions, and `db.store`/`db.path` properties.
- **Tests** — updated `FakeDocumentBuilder`, `ContentStoreIntegrationTest`, `ContentStoreTest`, `CrawlerTest`, `PaginationTest` to use the in-memory store.

---

## Java 21 Migration

### Gradle

- **Wrapper**: 7.3.3 → **9.4.0**
- **Source/target compatibility**: 1.8 → **21**

### Dependency Upgrades

| Dependency | Old | New |
|---|---|---|
| SLF4J | 1.7.32 | 2.0.16 |
| Logback | 1.2.10 | 1.5.15 |
| Jetty | 9.4.44 | 9.4.57 |
| JaCoCo | 0.8.7 | 0.8.14 |
| JUnit 5 | 5.8.2 | 5.11.4 |
| Mockito | 4.2.0 | 5.22.0 |
| AssertJ | 3.21.0 | 3.27.3 |
| junit-pioneer | 1.5.0 | 2.3.0 |
| grgit | 4.1.1 | 5.3.0 |
| nexus-publish | 1.1.0 | 2.0.0 |
| versions-plugin | 0.40.0 | 0.51.0 |
| maven-plugin-dev | 0.3.1 | 1.0.3 |

### Plugin Changes

- **Removed** `nebula.optional-base` — incompatible with Gradle 9 (uses removed `MavenPlugin` API). Replaced `optional` markers with `compileOnly` + `testImplementation`.
- **Replaced** `de.benediktritter.maven-plugin-development` → `org.gradlex.maven-plugin-development` (deprecated plugin).

### Build Convention (`java-common.gradle`)

- Java source/target 1.8 → 21
- Removed Java 9 compatibility guards and `MaxDirectMemorySize` JVM arg
- Added Mockito/ByteBuddy JVM args (`--add-opens`, `-Dnet.bytebuddy.experimental=true`)
- Simplified Javadoc configuration

### Disabled Tests

- **`MainTest.shouldTellUserThatTemplateOptionRequiresInitOption`** — uses `ExitGuard`/`SecurityManager` which is removed in Java 25.
- **`GroovyTemplateEngineRenderingTest`** (9 tests) — Groovy 3.x ASM cannot handle Java 25 class files. Will be fixed by upgrading to Groovy 4.x.

---

## Breaking Changes

- **Java 21 minimum required** to run jbake after this migration.
- Groovy SimpleTemplate engine does not work when running on Java 25 (Groovy 3.x limitation). Groovy MarkupTemplate engine works fine.
