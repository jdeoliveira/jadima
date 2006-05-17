/*
 * HbBackendRole.java
 *
 * Created on 9 de junio de 2005, 01:04 AM
 */

package ve.usb.jgm.repo.backend.hibernate;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class HbBackendRole {

    
    public static final int OP_PUBLISH = 0;
    public static final int OP_ADMIN = 1;
    
    /**
     * Holds value of property operation.
     */
    private int operation;

    /**
     * Holds value of property roleName.
     */
    private String roleName;

    /**
     * Holds value of property id.
     */
    private long id;
    
    /** Creates a new instance of HbBackendRole */
    public HbBackendRole() {
    }

    /**
     * Getter for property operation.
     * @return Value of property operation.
     */
    public int getOperation() {

        return this.operation;
    }

    /**
     * Setter for property operation.
     * @param operation New value of property operation.
     */
    public void setOperation(int operation) {

        this.operation = operation;
    }

    /**
     * Getter for property roleName.
     * @return Value of property roleName.
     */
    public String getRoleName() {

        return this.roleName;
    }

    /**
     * Setter for property roleName.
     * @param roleName New value of property roleName.
     */
    public void setRoleName(String roleName) {

        this.roleName = roleName;
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public long getId() {

        return this.id;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(long id) {

        this.id = id;
    }
    
}
