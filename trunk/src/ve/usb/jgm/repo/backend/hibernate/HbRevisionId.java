/*
 * HbRevisionId.java
 *
 * Created on 8 de junio de 2005, 04:39 PM
 */

package ve.usb.jgm.repo.backend.hibernate;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class HbRevisionId implements java.io.Serializable {

    /**
     * Holds value of property library_name.
     */
    private String library_name;

    /**
     * Holds value of property version_major.
     */
    private int version_major;

    /**
     * Holds value of property version_minor.
     */
    private int version_minor;

    /**
     * Holds value of property number.
     */
    private int number;
    
    /** Creates a new instance of HbRevisionId */
    public HbRevisionId() {
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
     * Getter for property version_major.
     * @return Value of property version_major.
     */
    public int getVersion_major() {

        return this.version_major;
    }

    /**
     * Setter for property version_major.
     * @param version_major New value of property version_major.
     */
    public void setVersion_major(int version_major) {

        this.version_major = version_major;
    }

    /**
     * Getter for property version_minor.
     * @return Value of property version_minor.
     */
    public int getVersion_minor() {

        return this.version_minor;
    }

    /**
     * Setter for property version_minor.
     * @param version_minor New value of property version_minor.
     */
    public void setVersion_minor(int version_minor) {

        this.version_minor = version_minor;
    }

    /**
     * Getter for property number.
     * @return Value of property number.
     */
    public int getNumber()  {

        return this.number;
    }

    /**
     * Setter for property number.
     * @param number New value of property number.
     */
    public void setNumber(int number)  {

        this.number = number;
    }
    
}
