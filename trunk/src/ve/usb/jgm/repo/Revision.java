/*
 * Revision.java
 *
 * Created on 14 de mayo de 2005, 06:06 PM
 */

package ve.usb.jgm.repo;

import java.util.*;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class Revision {

    /**
     * Holds value of property revisionNumber.
     */
    private int revisionNumber;

    /**
     * Holds value of property classes.
     */
    private Bytecode[] classes;

    /**
     * Holds value of property description.
     */
    private String description;
    
    /** Creates a new instance of Revision */
    public Revision() {
        classes = new Bytecode[0];
    }

    /**
     * Getter for property revisionNumber.
     * @return Value of property revisionNumber.
     */
    public int getRevisionNumber() {

        return this.revisionNumber;
    }

    /**
     * Setter for property revisionNumber.
     * @param revisionNumber New value of property revisionNumber.
     */
    public void setRevisionNumber(int revisionNumber) {

        this.revisionNumber = revisionNumber;
    }

    /**
     * Getter for property classes.
     * --transient, may or maynot contain this field--
     * @return Value of property classes.
     */
    public Bytecode[] getClasses() {

        return this.classes;
    }

    /**
     * Setter for property classes.
     * @param classes New value of property classes.
     */
    public void setClasses(Bytecode[] classes) {

        this.classes = classes;
    }

    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * Setter for property description.
     * @param description New value of property description.
     */
    public void setDescription(String description) {

        this.description = description;
    }
    
    public void addClass(Bytecode b) {
        Bytecode[] newB = new Bytecode[classes.length + 1];
        System.arraycopy(classes, 0, newB, 0, classes.length);
        newB[classes.length] = b;
        classes = newB;
    }
    
}
