/*
 * LibraryAlreadyExistsFault.java
 *
 * Created on 19 de mayo de 2005, 12:01 PM
 */

package ve.usb.jgm.ws.faults;

import java.rmi.RemoteException;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class RepositoryInternalErrorFault extends org.apache.axis.AxisFault implements java.io.Serializable {
    
    /** Creates a new instance of LibraryAlreadyExistsFault */
    public RepositoryInternalErrorFault() {
        
    }
    
    public RepositoryInternalErrorFault(String msg) {
        super(msg);
    }
    
    public RepositoryInternalErrorFault(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
