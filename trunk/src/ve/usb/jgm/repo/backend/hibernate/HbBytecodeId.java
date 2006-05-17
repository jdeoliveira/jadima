/*
 * HbBytecodeId.java
 *
 * Created on 8 de junio de 2005, 04:40 PM
 */

package ve.usb.jgm.repo.backend.hibernate;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class HbBytecodeId implements java.io.Serializable {

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
     * Holds value of property revision_number.
     */
    private int revision_number;

    /**
     * Holds value of property className.
     */
    private String className;
    
    /** Creates a new instance of HbBytecodeId */
    public HbBytecodeId() {
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
     * Getter for property revision_number.
     * @return Value of property revision_number.
     */
    public int getRevision_number() {

        return this.revision_number;
    }

    /**
     * Setter for property revision_number.
     * @param revision_number New value of property revision_number.
     */
    public void setRevision_number(int revision_number) {

        this.revision_number = revision_number;
    }

    /**
     * Getter for property className.
     * @return Value of property className.
     */
    public String getClassName() {

        return this.className;
    }

    /**
     * Setter for property className.
     * @param className New value of property className.
     */
    public void setClassName(String className) {

        this.className = className;
    }
    
}
