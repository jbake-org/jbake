package org.jbake.app

import org.jbake.launcher.SystemExit

/**
 * This runtime exception is thrown by JBake API to indicate an processing
 * error.
 *
 *
 * It always contains an error message and if available the cause.
 */
class JBakeException
/**
 *
 * @param message
 * The error message.
 * @param cause
 * The causing exception or `null` if no cause
 * available.
 */ @JvmOverloads constructor(private val exit: SystemExit, message: String?, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    fun getExit(): Int {
        return exit.getStatus()
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
