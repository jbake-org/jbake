package org.jbake.publisher;

public class PublisherException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PublisherException(String message) {
        super(message);
    }
    
    public PublisherException(String message, Throwable e) {
        super(message, e);
    }
    
    public PublisherException(Throwable e) {
        super(e);
    }
    
}
