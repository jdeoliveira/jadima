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
public class VersionNotFoundFault extends org.apache.axis.AxisFault implements java.io.Serializable {
    
    /** Creates a new instance of LibraryAlreadyExistsFault */
    public VersionNotFoundFault() {
        
    }
    
    public VersionNotFoundFault(String msg) {
        super(msg);
    }
    
    public VersionNotFoundFault(String msg, Throwable cause) {
        super(msg, cause);
    }
}
