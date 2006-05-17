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
public class RepositoryInternalErrorException extends java.rmi.RemoteException {
    
    /**
     * Creates a new instance of <code>UnsatisfiedRequestException</code> without detail message.
     */
    public RepositoryInternalErrorException() {
    }
    
    
    /**
     * Constructs an instance of <code>UnsatisfiedRequestException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RepositoryInternalErrorException(String msg) {
        super(msg);
    }
    
    public RepositoryInternalErrorException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
 