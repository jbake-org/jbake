package org.jbake.exception;

import org.jbake.launcher.SystemExit;

/**
 * This runtime exception is thrown by JBake API to indicate an processing
 * error.
 * <p>
 * It always contains an error message and if available the cause.
 */
public class JBakeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final SystemExit exit;

    /**
     *
     * @param message
     *            The error message.
     * @param cause
     *            The causing exception or <code>null</code> if no cause
     *            available.
     */
    public JBakeException(final SystemExit exit, final String message, final Throwable cause) {
        super(message, cause);
        this.exit = exit;
    }

    public JBakeException(final SystemExit exit, final String message) {
        this(exit, message, null);
    }

    public int getExit() {
        return exit.getStatus();
    }
}
