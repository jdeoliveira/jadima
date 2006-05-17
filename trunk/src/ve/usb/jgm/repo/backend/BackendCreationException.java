/*
 * MisconfiguredLocalRepositoryException.java
 *
 * Created on 25 de septiembre de 2004, 10:27 PM
 */

package ve.usb.jgm.repo.backend;

/**
 *
 * @author  Jesus
 */
public class BackendCreationException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>MisconfiguredLocalRepositoryException</code> without detail message.
     */
    public BackendCreationException() {
    }
    
    
    /**
     * Constructs an instance of <code>MisconfiguredLocalRepositoryException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BackendCreationException(String msg) {
        super(msg);
    }
    
    public BackendCreationException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }
}
