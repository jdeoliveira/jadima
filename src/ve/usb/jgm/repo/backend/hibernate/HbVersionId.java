/*
 * HbVersionId.java
 *
 * Created on 8 de junio de 2005, 04:34 PM
 */

package ve.usb.jgm.repo.backend.hibernate;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class HbVersionId implements java.io.Serializable {

    /**
     * Holds value of property library_name.
     */
    private String library_name;

    /**
     * Holds value of property major.
     */
    private int major;

    /**
     * Holds value of property minor.
     */
    private int minor;
    
    /** Creates a new instance of HbVersionId */
    public HbVersionId() {
    }

    /**
     * Getter for property library_name.
     * @return Value of property library_name.
     */
    public String getLibrary_name() {

        return this.library_name;
    }

    /**
     * Setter for property library_name.
     * @param library_name New value of property library_name.
     */
    public void setLibrary_name(String library_name) {

        this.library_name = library_name;
    }

    /**
     * Getter for property major.
     * @return Value of property major.
     */
    public int getMajor() {

        return this.major;
    }

    /**
     * Setter for property major.
     * @param major New value of property major.
     */
    public void setMajor(int major) {

        this.major = major;
    }

    /**
     * Getter for property minor.
     * @return Value of property minor.
     */
    public int getMinor() {

        return this.minor;
    }

    /**
     * Setter for property minor.
     * @param minor New value of property minor.
     */
    public void setMinor(int minor) {

        this.minor = minor;
    }
    
}
