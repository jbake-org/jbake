package org.jbake.app;

/**
 * This runtime exception is thrown by JBake API to indicate an processing
 * error.
 * <p>
 * It always contains an error message and if available the cause.
 */
public class JBakeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public enum SystemExit {
        ERROR(1),
        CONFIGURATION_ERROR(2),
        INIT_ERROR(3),
        SERVER_ERROR(4);

        private final int status;

        SystemExit(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

    }
    final private SystemExit exit;

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
