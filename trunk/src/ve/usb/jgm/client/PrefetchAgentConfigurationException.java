/*
 * RepositoryClientConfigurationException.java
 *
 * Created on 17 de mayo de 2005, 10:20 PM
 */

package ve.usb.jgm.client;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class PrefetchAgentConfigurationException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>RepositoryClientConfigurationException</code> without detail message.
     */
    public PrefetchAgentConfigurationException() {
    }
    
    
    /**
     * Constructs an instance of <code>RepositoryClientConfigurationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PrefetchAgentConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
