package org.jbake.template

/**
 * Thrown if rendering of a document failed.
 *
 * @author Cédric Champeau
 */
open class RenderingException : Exception {
    constructor(cause: Throwable?) : super(cause)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(message: String?) : super(message)
}
