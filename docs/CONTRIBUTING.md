# Contributing to JBake

Thank you for your interest in contributing to JBake! This document provides guidelines and coding standards for the project.

## Project Overview

JBake is both a **tool** and a **library**:
- As a tool, it's used directly by end users to generate static sites
- As a library, it's consumed by Java projects as a dependency

Therefore, all API classes must be **Java-friendly** and maintain backward compatibility.

## Coding Style Guidelines

### Kotlin-Java Interoperability

Since JBake is used as a library by Java projects, always keep Java interoperability in mind:

- Use `@JvmStatic` for static methods that should be accessible from Java
- Use `@JvmField` for properties that should be exposed as public fields in Java
- Use `@JvmOverloads` for constructors/methods with default parameters
- Avoid Kotlin-only features in public APIs that don't translate well to Java

Example:
```kotlin
object PropertyList {
    @JvmField val ARCHIVE_FILE: Property = Property("archive.file", "Output filename for archive file")

    @JvmStatic
    fun getPropertyByKey(key: String): Property {
        // ...
    }
}
```

### Conditional Assignments

For conditional assignments where both branches return single-line values of similar length, prefer the `if-else` format **without braces**:

```kotlin
// ✅ Preferred
val dbUri =
    if (type.equals(ODatabaseType.PLOCAL.name, ignoreCase = true))
        "$type:$name"
    else "$type:"

orient = OrientDB(dbUri, OrientDBConfig.defaultConfig())
```

**Not:**
```kotlin
// ❌ Avoid
val dbUri = if (type.equals(ODatabaseType.PLOCAL.name, ignoreCase = true)) {
    "$type:$name"
} else {
    "$type:"
}
```

### Trivial If Statements

For simple if statements with a single-line body, prefer the compact 2-line format without braces:

```kotlin
// ✅ Preferred
if (!schema.existsClass(Schema.DOCUMENTS))
    createDocType(schema)

if (!needed)
    clearCache = updateTemplateSignatureIfChanged(templateDir)
```

**Not:**
```kotlin
// ❌ Avoid unnecessary braces for simple statements
if (!schema.existsClass(Schema.DOCUMENTS)) {
    createDocType(schema)
}
```

**Exception:** Use braces when the if body contains multiple statements or complex logic.

### Reducing Duplication

When only a small part varies in similar code blocks, extract the varying part to eliminate duplication:

```kotlin
// ✅ Preferred - extract the variable URL
val dbUri =
    if (type.equals(ODatabaseType.PLOCAL.name, ignoreCase = true))
        "$type:$name"
    else "$type:"

orient = OrientDB(dbUri, OrientDBConfig.defaultConfig())
```

**Not:**
```kotlin
// ❌ Duplicates the constructor call
orient =
    if (type.equals(ODatabaseType.PLOCAL.name, ignoreCase = true))
        OrientDB("$type:$name", OrientDBConfig.defaultConfig())
    else
        OrientDB("$type:", OrientDBConfig.defaultConfig())
```

### SQL Conventions

Always write SQL keywords in **UPPERCASE**:

```kotlin
// ✅ Preferred
private const val STATEMENT_GET_ALL_CONTENT = "SELECT * FROM Documents WHERE type='%s' ORDER BY date DESC"

fun getDocumentByUri(uri: String?): DocumentList<DocumentModel> {
    return query("SELECT * FROM Documents WHERE sourceuri=?", uri)
}
```

**Not:**
```kotlin
// ❌ Lowercase SQL keywords
private const val STATEMENT_GET_ALL_CONTENT = "select * from Documents where type='%s' order by date desc"
```

### Long Lines for Constants

For SQL statements and similar constants, prefer keeping them on a single line even if they're long:

```kotlin
// ✅ Preferred
companion object {
    private const val STATEMENT_GET_PUBLISHED_POST_BY_TYPE_AND_TAG = "SELECT * FROM Documents WHERE status='published' AND type='%s' AND ? IN tags ORDER BY date DESC"
    private const val STATEMENT_GET_DOCUMENT_STATUS_BY_DOCTYPE_AND_URI = "SELECT sha1,rendered FROM Documents WHERE sourceuri=?"
}
```

**Not:**
```kotlin
// ❌ Unnecessary line breaks for constants
companion object {
    private const val STATEMENT_GET_PUBLISHED_POST_BY_TYPE_AND_TAG =
        "SELECT * FROM Documents WHERE status='published' " +
        "AND type='%s' AND ? IN tags ORDER BY date DESC"
}
```

### Nullability

During the Java-to-Kotlin migration, many nullable types were added automatically. Apply common sense and test semantics to determine what should actually be non-nullable:

```kotlin
// ✅ Most setters shouldn't accept null
fun setPreviousContent(previousDocumentModel: DocumentModel) {
    // Not DocumentModel? - why would you set a null value?
}

// ✅ Query methods shouldn't accept null for required parameters
fun getPublishedContent(docType: String): DocumentList<DocumentModel> {
    // Not String? - querying for null doesn't make sense
}
```

Use `lateinit` for properties that are initialized in `@BeforeEach`/`@Before` methods:
```kotlin
private lateinit var config: DefaultJBakeConfiguration
```

### Property Access vs Getters

Prefer property access over getter methods when using Kotlin code:

```kotlin
// ✅ Preferred
config.contentDir
config.destinationDir

// ❌ Avoid (Java-style)
config.getContentDir()
config.getDestinationDir()
```

## Testing

- All tests should pass before submitting a pull request
- Add tests for new functionality
- Update existing tests when changing behavior

### Database Setup in Tests

For tests that use OrientDB, ensure proper setup and cleanup:

```kotlin
@BeforeEach
fun setUp() {
    // Initialize database
}

@AfterEach
fun tearDown() {
    if (contentStore?.isActive == true) {
        contentStore?.close()
        contentStore?.shutdown()
    }
}
```

## Commit Messages

- Use clear, descriptive commit messages
- Start with a verb in present tense: "Add", "Fix", "Update", "Remove"
- Reference issue numbers when applicable

## Pull Requests

1. Fork the repository
2. Create a feature branch from `master`
3. Make your changes following these guidelines
4. Ensure all tests pass
5. Submit a pull request with a clear description of changes

## Questions?

If you have questions about these guidelines or need clarification, please open an issue for discussion.

---

Thank you for contributing to JBake!

# Next steps

- Migrate the templating engines to their latest versions
- Continue refactoring the central map into type-safe models
- Reduce the warnings from the Kotlin compiler
- Rebrand to KBake?
- Set up publising as per https://github.com/OndraZizka/csv-cruncher
- Publish a new release 4.0.0 under ch.zizka.kbake groupId
