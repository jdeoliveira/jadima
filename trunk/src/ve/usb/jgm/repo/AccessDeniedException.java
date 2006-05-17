/*
 * UnsatisfiedRequestException.java
 *
 * Created on 25 de septiembre de 2004, 09:20 PM
 */

package ve.usb.jgm.repo;

/**
 *
 * @author  Jesus
 */
public class AccessDeniedException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>UnsatisfiedRequestException</code> without detail message.
     */
    public AccessDeniedException() {
    }
    
    
    /**
     * Constructs an instance of <code>UnsatisfiedRequestException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public AccessDeniedException(String msg) {
        super(msg);
    }
    
    public AccessDeniedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
 