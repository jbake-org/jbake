package org.jbake

import java.nio.file.Path

class NoStackTraceException(message: String) : RuntimeException(message) {
    // Overriding this prevents the JVM from gathering the stack trace information.
    // - Improves performance when exceptions are used for control flow.
    // - Reduces log clutter.
    override fun fillInStackTrace(): Throwable = this
}

class ShortTraceException(message: String, val cutStackAt: Int) : RuntimeException(message) {
    override fun fillInStackTrace(): Throwable {
        // Generate the standard full trace
        super.fillInStackTrace()

        // Keep only the first 2 elements (safe even if trace has 0 or 1 items)
        this.stackTrace = this.stackTrace.take(cutStackAt).toTypedArray()

        return this
    }
}

val buildOutputDir: Path
    = System.getProperty("jbake.buildOutputDir")?.let { Path.of(it) }
        ?: Path.of(System.getProperty("java.io.tmpdir"))
