/*
 * Revision.java
 *
 * Created on 7 de junio de 2005, 16:58
 */

package ve.usb.jgm.repo.backend.hibernate;

/**
 *
 * @author  jdeoliveira
 */
public class HbRevision {

    /**
     * Holds value of property id.
     */
    private HbRevisionId id;

    /**
     * Holds value of property number.
     */
    private int number;

    /**
     * Holds value of property bytecode.
     */
    private java.util.Set bytecode = new java.util.HashSet();

    /**
     * Holds value of property version.
     */
    private HbVersion version;
    
    /** Creates a new instance of Revision */
    public HbRevision() {
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public HbRevisionId getId() {

        return this.id;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(HbRevisionId id) {

        this.id = id;
    }

    /**
     * Getter for property number.
     * @return Value of property number.
     */
    public int getNumber() {

        return this.number;
    }

    /**
     * Setter for property number.
     * @param number New value of property number.
     */
    public void setNumber(int number) {

        this.number = number;
    }

    /**
     * Getter for property bytecode.
     * @return Value of property bytecode.
     */
    public java.util.Set getBytecode() {

        return this.bytecode;
    }

    /**
     * Setter for property bytecode.
     * @param bytecode New value of property bytecode.
     */
    public void setBytecode(java.util.Set bytecode) {

        this.bytecode = bytecode;
    }

    /**
     * Getter for property version.
     * @return Value of property version.
     */
    public HbVersion getVersion() {

        return this.version;
    }

    /**
     * Setter for property version.
     * @param version New value of property version.
     */
    public void setVersion(HbVersion version) {

        this.version = version;
    }
    
    public void addBytecode(HbBytecode b) {
        b.setRevision(this);
        this.getBytecode().add(b);
    }
    
}
