/*
 * RepositoryObject.java
 *
 * Created on 1 de agosto de 2006, 05:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ve.usb.jgm.gui;

/**
 *
 * @author karyn
 */
public class RepositoryObject {
    
    /** Creates a new instance of RepositoryObject */
    private String name;
    private String type;
    private int priority;
    private String URL;
    
    public RepositoryObject(String _name, String _type, int _priority, String _URL) {
        setname(_name);
        settype(_type);
        setpriority(_priority);
        setURL(_URL);
    }
    
    public String getname() {
        return name;
    }
    
    public String gettype() {
        return type;
    }
    
    public int getpriority() {
        return priority;
    }
    
    public String getURL() {
        return URL;
    }
    
    public void setname(String _name) {
        name = _name;
    }
    
    public void settype(String _type) {
        type = _type;
    }
    
    public void setpriority(int _priority) {
        priority = _priority;
    }
    
    public void setURL(String _URL) {
        URL = _URL;
    }
    
}
