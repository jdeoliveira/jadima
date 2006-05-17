/*
 * Cache.java
 *
 * Created on 1 de mayo de 2005, 05:47 PM
 */

package ve.usb.jgm.client.config;

/**
 *
 * @author  Administrator
 */
public class Cache {

    /**
     * Holds value of property size.
     */
    private long size;

    /**
     * Holds value of property policy.
     */
    private String policy;
    
    /** Creates a new instance of Cache */
    public Cache(long _size, String _policy) {
        size = _size;
        policy = _policy;
    }

    /**
     * Getter for property size.
     * @return Value of property size.
     */
    public long getSize() {

        return this.size;
    }

    /**
     * Setter for property size.
     * @param size New value of property size.
     */
    public void setSize(long size) {

        this.size = size;
    }

    /**
     * Getter for property policy.
     * @return Value of property policy.
     */
    public String getPolicy() {

        return this.policy;
    }

    /**
     * Setter for property policy.
     * @param policy New value of property policy.
     */
    public void setPolicy(String policy) {

        this.policy = policy;
    }
    
}
