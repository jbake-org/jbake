package org.jbake.template;

public class NoModelExtractorException extends RenderingException {

	public NoModelExtractorException(String message) {
		super(message);
	}

	public NoModelExtractorException(Throwable cause) {
		super(cause);
	}

	public NoModelExtractorException(String message, Throwable cause) {
		super(message, cause);
	}

}