package org.jbake

class NoStackTraceException(message: String) : RuntimeException(message) {
    // Overriding this prevents the JVM from gathering the stack trace information, i.e. improving performance when exceptions are used for control flow.
    override fun fillInStackTrace(): Throwable = this
}

class ShortTraceException(message: String) : RuntimeException(message) {
    override fun fillInStackTrace(): Throwable {
        // 1. Generate the standard full trace
        super.fillInStackTrace()

        // 2. Keep only the first 2 elements (safe even if trace has 0 or 1 items)
        this.stackTrace = this.stackTrace.take(2).toTypedArray()

        return this
    }
}
