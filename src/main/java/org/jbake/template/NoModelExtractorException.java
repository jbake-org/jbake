package org.jbake.template;

import org.jbake.render.RenderingException;

public class NoModelExtractorException extends RenderingException {

	public NoModelExtractorException() {
	}

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
