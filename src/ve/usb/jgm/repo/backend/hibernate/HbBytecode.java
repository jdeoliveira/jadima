/*
 * Bytecode.java
 *
 * Created on 7 de junio de 2005, 11:09 PM
 */

package ve.usb.jgm.repo.backend.hibernate;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class HbBytecode {

    /**
     * Holds value of property revision.
     */
    private HbRevision revision;

    /**
     * Holds value of property className.
     */
    private String className;

    /**
     * Holds value of property classData.
     */
    private byte[] classData;

    /**
     * Holds value of property id.
     */
    private HbBytecodeId id;
    
    /** Creates a new instance of Bytecode */
    public HbBytecode() {
    }

    /**
     * Getter for property revision.
     * @return Value of property revision.
     */
    public HbRevision getRevision() {

        return this.revision;
    }

    /**
     * Setter for property revision.
     * @param revision New value of property revision.
     */
    public void setRevision(HbRevision revision) {

        this.revision = revision;
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

    /**
     * Getter for property classData.
     * @return Value of property classData.
     */
    public byte[] getClassData() {

        return this.classData;
    }

    /**
     * Setter for property classData.
     * @param classData New value of property classData.
     */
    public void setClassData(byte[] classData) {

        this.classData = classData;
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public HbBytecodeId getId() {

        return this.id;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(HbBytecodeId id) {

        this.id = id;
    }
    
}
