package org.jbake.template;

/**
 * Thrown if rendering of a document failed.
 *
 * @author Cédric Champeau
 */
public class RenderingException extends Exception {
    public RenderingException(final Throwable cause) {
        super(cause);
    }

    public RenderingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RenderingException(final String message) {
        super(message);
    }
}
