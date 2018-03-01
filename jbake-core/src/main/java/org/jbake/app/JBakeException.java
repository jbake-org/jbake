package org.jbake.app;

/**
 * This runtime exception is thrown by JBake API to indicate an processing
 * error.
 * <p>
 * It always contains an error message and if available the cause.
 */
public class JBakeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 *            The error message.
	 * @param cause
	 *            The causing exception or <code>null</code> if no cause
	 *            available.
	 */
	public JBakeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public JBakeException(final String message) {
		this(message, null);
	}
}
