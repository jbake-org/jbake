package org.jbake.util


/*
  Lazy evaluation of the messages sent to Slf4j.
  Usage:
    log.trace { "State dump: " + expensiveLongSerialisation(state) }
    See also https://jira.qos.ch/browse/SLF4J-371
*/

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.PrintStream

inline fun Logger.error(msg: () -> String) { if (this.isErrorEnabled) this.error(msg()) }
inline fun Logger.error(ex: Exception, msg: () -> String) { if (this.isErrorEnabled) this.error(msg(), ex) }
inline fun Logger.warn(msg: () -> String) { if (this.isWarnEnabled) this.warn(msg()) }
inline fun Logger.warn(ex: Exception, msg: () -> String) { if (this.isWarnEnabled) this.warn(msg(), ex) }
inline fun Logger.info(msg: () -> String) { if (this.isInfoEnabled) this.info(msg()) }
inline fun Logger.debug(msg: () -> String) { if (this.isDebugEnabled) this.debug(msg()) }
inline fun Logger.trace(msg: () -> String) { if (this.isTraceEnabled) this.trace(msg()) }

/**
 * Create a logger for a specific class (for top-level declarations).
 *
 * Usage at top-level or in functions:
 *   private val log: Logger by logger<MyClass>()
 */
inline fun <reified T : Any> logger(): Lazy<Logger> = lazy {
    LoggerFactory.getLogger(T::class.java)
}

object Logging {
    /**
     * Extension function to create a logger using the receiver's class.
     * Automatically detects and unwraps companion objects to use the enclosing class name.
     *
     * Usage in class instance:
     *   private val log: Logger by Logging.logger()
     *
     * Usage in companion object:
     *   companion object {
     *       private val log: Logger by Logging.logger()
     *   }
     *
     * This works by using the reified type parameter to get the actual class at compile time,
     * then checking if it's a companion object and unwrapping to the enclosing class if needed.
     */
    inline fun <reified R : Any> R.logger(): Lazy<Logger> = lazy {
        val javaClass = R::class.java
        val loggerClass = unwrapCompanionClass(javaClass)
        LoggerFactory.getLogger(loggerClass.name)
    }
}

/**
 * Unwraps the companion class to enclosing class given a Java Class.
 * In Kotlin, companion objects are compiled to nested classes with the name "Companion".
 * This function detects if a class is a companion object and returns the enclosing class instead.
 */
fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.enclosingClass != null && ofClass.simpleName == "Companion") {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}

object DebugUtil {
    fun <T : Any?> printMap(map: MutableMap<String, T?>, printStream: PrintStream) {
        printStream.println()
        for (entry in map.entries) {
            printStream.println(entry.key + " :: " + entry.value)
        }
        printStream.println()
    }
}

