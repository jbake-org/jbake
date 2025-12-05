package org.jbake.app

/**
 * Thrown by JBake API on processing error.
 */
class JBakeExitException(private val exit: SystemExit, message: String, cause: Throwable? = null)
    : RuntimeException(message, cause)
{
    fun getExit() = exit.status
}

enum class SystemExit {
    SUCCESS,
    ERROR,
    CONFIG_ERROR,
    INIT_ERROR,
    SERVER_ERROR;

    val status: Int get() = this.ordinal
}
