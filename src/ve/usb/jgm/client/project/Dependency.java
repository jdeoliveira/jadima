/*
 * Dependency.java
 *
 * Created on 4 de mayo de 2005, 11:49 PM
 */

package ve.usb.jgm.client.project;

/**
 *
 * @author  Administrator
 */
public class Dependency {

    /**
     * Holds value of property libraryName.
     */
    private String libraryName;

    /**
     * Holds value of property majorVersion.
     */
    private int majorVersion;

    /**
     * Holds value of property minorVersion.
     */
    private int minorVersion;

    /**
     * Holds value of property priority.
     */
    private int priority;
    
    /** Creates a new instance of Dependency */
    public Dependency(String _libraryName, int _majorVersion, int _minorVersion, int _priority) {
        libraryName = _libraryName;
        majorVersion = _majorVersion;
        minorVersion = _minorVersion;
        priority = _priority;
    }

    /**
     * Getter for property libraryName.
     * @return Value of property libraryName.
     */
    public String getLibraryName() {

        return this.libraryName;
    }

    /**
     * Setter for property libraryName.
     * @param libraryName New value of property libraryName.
     */
    public void setLibraryName(String libraryName) {

        this.libraryName = libraryName;
    }

    /**
     * Getter for property majorVersion.
     * @return Value of property majorVersion.
     */
    public int getMajorVersion() {

        return this.majorVersion;
    }

    /**
     * Setter for property majorVersion.
     * @param majorVersion New value of property majorVersion.
     */
    public void setMajorVersion(int majorVersion) {

        this.majorVersion = majorVersion;
    }

    /**
     * Getter for property minorVersion.
     * @return Value of property minorVersion.
     */
    public int getMinorVersion() {

        return this.minorVersion;
    }

    /**
     * Setter for property minorVersion.
     * @param minorVersion New value of property minorVersion.
     */
    public void setMinorVersion(int minorVersion) {

        this.minorVersion = minorVersion;
    }
    
    public boolean equals(Object obj) {
        try {
            Dependency d = (Dependency)obj;
            return d.getLibraryName().equals(libraryName);
        } catch (ClassCastException e) {
            return false;
        }
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
}
