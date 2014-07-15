package org.jbake.launcher;

public class JBakeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public JBakeException(String message, Throwable cause) {
		super(message, cause);
	}

	public JBakeException(String message) {
		super(message);
	}

}
