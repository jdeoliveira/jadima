/*
 * LibraryAlreadyExistsFault.java
 *
 * Created on 19 de mayo de 2005, 12:01 PM
 */

package ve.usb.jgm.ws.faults;

import java.rmi.RemoteException;
import org.apache.axis.*;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class LibraryAlreadyExistsFault extends RemoteException implements java.io.Serializable {
    
    /** Creates a new instance of LibraryAlreadyExistsFault */
    public LibraryAlreadyExistsFault() {
        
    }
    
    public LibraryAlreadyExistsFault(String msg) {
        super(msg);
    }
    
    public LibraryAlreadyExistsFault(String msg, Throwable cause) {
        super(msg, cause);
    }
}
