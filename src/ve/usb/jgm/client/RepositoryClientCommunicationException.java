/*
 * RepositoryClientCommunicationException.java
 *
 * Created on 19 de mayo de 2005, 10:51 AM
 */

package ve.usb.jgm.client;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class RepositoryClientCommunicationException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>RepositoryClientCommunicationException</code> without detail message.
     */
    public RepositoryClientCommunicationException() {
    }
    
    
    /**
     * Constructs an instance of <code>RepositoryClientCommunicationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RepositoryClientCommunicationException(String msg) {
        super(msg);
    }
    
    public RepositoryClientCommunicationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
