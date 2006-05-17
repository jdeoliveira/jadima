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
public class BackendConfigurationException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>MisconfiguredLocalRepositoryException</code> without detail message.
     */
    public BackendConfigurationException() {
    }
    
    
    /**
     * Constructs an instance of <code>MisconfiguredLocalRepositoryException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BackendConfigurationException(String msg) {
        super(msg);
    }
}
