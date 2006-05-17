/*
 * MisconfiguredLocalRepositoryException.java
 *
 * Created on 25 de septiembre de 2004, 10:27 PM
 */

package ve.usb.jgm.repo;

/**
 *
 * @author  Jesus
 */
public class VersionNotFoundException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>MisconfiguredLocalRepositoryException</code> without detail message.
     */
    public VersionNotFoundException() {
    }
    
    
    /**
     * Constructs an instance of <code>MisconfiguredLocalRepositoryException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public VersionNotFoundException(String msg) {
        super(msg);
    }
    
    public VersionNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
