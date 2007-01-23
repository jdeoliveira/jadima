/*
 * LibraryObject.java
 *
 * Created on 1 de agosto de 2006, 11:17 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ve.usb.jgm.gui;

/**
 *
 * @author karyn
 */
public class LibraryObject {
    
    /** Creates a new instance of LibraryObject */
    private String name;
    private int minorVersion;
    private int majorVersion;
        
    public LibraryObject(String _name, int _minorVersion, int _majorVersion) {
        setname(_name);
        setminorVersion(_minorVersion);
        setmajorVersion(_majorVersion);
        
    }
    
    public String getname() {
        return name;
    }
    
    public int getminorVersion() {
        return minorVersion;
    }
    
    public int getmajorVersion() {
        return majorVersion;
    }
    
    public void setname(String _name) {
        name = _name;
    }
    
    public void setminorVersion(int _minorVersion) {
        minorVersion = _minorVersion;
    }
    
     public void setmajorVersion(int _majorVersion) {
        majorVersion = _majorVersion;
    }
}
