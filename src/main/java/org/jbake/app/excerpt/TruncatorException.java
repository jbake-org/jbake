package org.jbake.app.excerpt;

public class TruncatorException extends Exception {

	private static final long serialVersionUID = 1L;

    public TruncatorException(final Throwable cause) {
        super(cause);
    }

    public TruncatorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TruncatorException(final String message) {
        super(message);
    }
}
