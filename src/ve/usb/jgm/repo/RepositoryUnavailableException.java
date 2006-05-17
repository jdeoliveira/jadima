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
public class RepositoryUnavailableException extends java.rmi.RemoteException {
    
    /**
     * Creates a new instance of <code>UnsatisfiedRequestException</code> without detail message.
     */
    public RepositoryUnavailableException() {
    }
    
    
    /**
     * Constructs an instance of <code>UnsatisfiedRequestException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RepositoryUnavailableException(String msg) {
        super(msg);
    }
    
    public RepositoryUnavailableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
 