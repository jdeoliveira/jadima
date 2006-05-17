/*
 * Repository.java
 *
 * Created on 1 de mayo de 2005, 05:45 PM
 */

package ve.usb.jgm.client.config;

/**
 *
 * @author  Administrator
 */
public class Repository {

    /**
     * Holds value of property url.
     */
    private String url;

    /**
     * Holds value of property priority.
     */
    private int priority;
    
    /** Creates a new instance of Repository */
    public Repository(String _url, int _priority) {
        url = _url;
        priority = _priority;
    }

    /**
     * Getter for property url.
     * @return Value of property url.
     */
    public String getUrl() {

        return this.url;
    }

    /**
     * Setter for property url.
     * @param url New value of property url.
     */
    public void setUrl(String url) {

        this.url = url;
    }

    /**
     * Getter for property priority.
     * @return Value of property priority.
     */
    public int getPriority() {

        return this.priority;
    }

    /**
     * Setter for property priority.
     * @param priority New value of property priority.
     */
    public void setPriority(int priority) {

        this.priority = priority;
    }
    
    public String toString() {
        return "(" + priority + ") " + url;
    }
    
    public boolean equals(Object o) {
        try {
            Repository r = (Repository)o;
            return r.getUrl().equals(url);
        } catch (ClassCastException e) {
            return false;
        }
    }
    
}
