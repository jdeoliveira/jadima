/*
 * InvalidConfigurationFileException.java
 *
 * Created on 2 de mayo de 2005, 11:06 AM
 */

package ve.usb.jgm.client.config;

/**
 *
 * @author  Administrator
 */
public class InvalidConfigurationFileException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>InvalidConfigurationFileException</code> without detail message.
     */
    public InvalidConfigurationFileException() {
    }
    
    
    /**
     * Constructs an instance of <code>InvalidConfigurationFileException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvalidConfigurationFileException(String msg) {
        super(msg);
    }
    
    public InvalidConfigurationFileException(String msg, Throwable root) {
        super(msg, root);
    }
}
