/*
 * Prefetch.java
 *
 * Created on 2 de mayo de 2005, 08:47 PM
 */

package ve.usb.jgm.client.config;

/**
 *
 * @author  Administrator
 */
public class Prefetch {

    /**
     * Holds value of property thereshold.
     */
    private long thereshold;
    
    /** Creates a new instance of Prefetch */
    public Prefetch(long _thereshold) {
        thereshold = _thereshold;
    }

    /**
     * Getter for property thereshold.
     * @return Value of property thereshold.
     */
    public long getThereshold() {

        return this.thereshold;
    }

    /**
     * Setter for property thereshold.
     * @param thereshold New value of property thereshold.
     */
    public void setThereshold(long thereshold) {

        this.thereshold = thereshold;
    }
    
}
