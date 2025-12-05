package org.jbake.app



open class JBakeException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
open class RenderingException(message: String, cause: Throwable? = null) : JBakeException(message, cause)
class NoModelExtractorException(message: String, cause: Throwable? = null) : RenderingException(message, cause)


/**
 * Thrown by JBake API on processing error.
 */
class JBakeExitException(
    private val exit: SystemExit,
    message: String,
    cause: Throwable? = null,
)
    : RuntimeException(message, cause)
{
    fun getExit() = exit.errorNumber
}

enum class SystemExit {
    SUCCESS,
    ERROR,
    CONFIG_ERROR,
    INIT_ERROR,
    SERVER_ERROR;

    val errorNumber: Int get() = this.ordinal
}
