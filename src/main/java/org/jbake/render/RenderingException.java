package org.jbake.render;

/**
 * Thrown when one rendering step fails for any reason
 * @author ndx
 *
 */
public class RenderingException extends Exception {

	public RenderingException() {
	}

	public RenderingException(String message) {
		super(message);
	}

	public RenderingException(Throwable cause) {
		super(cause);
	}

	public RenderingException(String message, Throwable cause) {
		super(message, cause);
	}

}
