/*
 * InvalidConfigurationFileException.java
 *
 * Created on 2 de mayo de 2005, 11:06 AM
 */

package ve.usb.jgm.client.project;

/**
 *
 * @author  Administrator
 */
public class InvalidProjectDescriptorFileException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>InvalidConfigurationFileException</code> without detail message.
     */
    public InvalidProjectDescriptorFileException() {
    }
    
    
    /**
     * Constructs an instance of <code>InvalidConfigurationFileException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvalidProjectDescriptorFileException(String msg) {
        super(msg);
    }
    
    public InvalidProjectDescriptorFileException(String msg, Throwable root) {
        super(msg, root);
    }
}
