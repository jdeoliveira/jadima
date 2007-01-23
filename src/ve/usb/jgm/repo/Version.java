/*
 * Version.java
 *
 * Created on 14 de mayo de 2005, 01:18 PM
 */

package ve.usb.jgm.repo;

import java.util.*;
import org.apache.log4j.*;
/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class Version implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(Version.class);
    
    private String libraryName;
    
    /**
     * Holds value of property numberMajor.
     */
    private int numberMajor;

    /**
     * Holds value of property numberMinor.
     */
    private int numberMinor;

    /**
     * Holds value of property description.
     */
    private String description;

    /**
     * Holds value of property classes.
     */
    private Revision[] revisions;

    /**
     * Holds value of property stubsJar.
     */
    private byte[] stubsJar;
    
    private byte[] javadocZip;
    
    private String[] allowedRoles;

    /**
     * Holds value of property libraryDescription.
     */
    private String libraryDescription;

    /**
     * Holds value of property priority.
     */
    private int priority;
    
    /** Creates a new instance of Version */
    public Version() {
        revisions = new Revision[0];
    }

    /**
     * Getter for property numberMajor.
     * @return Value of property numberMajor.
     */
    public int getNumberMajor() {

        return this.numberMajor;
    }

    /**
     * Setter for property numberMajor.
     * @param numberMajor New value of property numberMajor.
     */
    public void setNumberMajor(int numberMajor) {

        this.numberMajor = numberMajor;
    }

    /**
     * Getter for property numberMinor.
     * @return Value of property numberMinor.
     */
    public int getNumberMinor() {

        return this.numberMinor;
    }

    /**
     * Setter for property numberMinor.
     * @param numberMinor New value of property numberMinor.
     */
    public void setNumberMinor(int numberMinor) {

        this.numberMinor = numberMinor;
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

    /**
     * Getter for property classes.
     * @return Value of property classes.
     */
    public Revision[] getRevisions() {

        return this.revisions;
    }
    
    public void setRevisions(Revision[] revisions) {

        this.revisions = revisions;
    }

    /**
     * Getter for property stubsJar.
     * @return Value of property stubsJar.
     */
    public byte[] getStubsJar() {

        return this.stubsJar;
    }

    /**
     * Setter for property stubsJar.
     * @param stubsJar New value of property stubsJar.
     */
    public void setStubsJar(byte[] stubsJar) {

        this.stubsJar = stubsJar;
    }
    
    /**
     * Getter for property stubsJar.
     * @return Value of property stubsJar.
     */
    public byte[] getJavadocZip() {
        return this.javadocZip;
    }

    /**
     * Setter for property stubsJar.
     * @param stubsJar New value of property stubsJar.
     */
    public void setJavadocZip(byte[] javadocZip) {

        this.javadocZip = javadocZip;
    }
    
    public void setAllowedRoles(String[] roles) {
        allowedRoles = roles;
    }
    
    public String[] getAllowedRoles() {
        return allowedRoles;
    }
    
    public String getLibraryName() {
        return libraryName;
    }
    
    public void setLibraryName(String _libraryName) {
        libraryName = _libraryName;
    }
    
    /*public int getlastRevision() {
        int maxRev = -1;
        for (Revision r: revisions) {
            if (r.getRevisionNumber() > maxRev) maxRev = r.getRevisionNumber();
        }
        return maxRev;
    }*/
    
    public String toString() {
        return ((libraryName == null)?("<unknown>"):(libraryName)) + "-" + numberMajor + "." + numberMinor; 
    }
    
    public boolean equals(Object o) {
        try {
            Version v = (Version)o;
            return (
                (libraryName.equals(v.getLibraryName())) &&
                (numberMajor == v.getNumberMajor()) &&
                (numberMinor == v.getNumberMinor())
            );
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    public int hashCode() {
        return (numberMajor  * 1000) + numberMinor; 
    }
    
    public void addRevision(Revision r) {
        Revision[] newRev = new Revision[revisions.length + 1];
        System.arraycopy(revisions, 0, newRev, 0, revisions.length);
        newRev[revisions.length] = r;
        revisions = newRev;
    }

    /**
     * Getter for property libraryDescription.
     * @return Value of property libraryDescription.
     */
    public String getLibraryDescription() {

        return this.libraryDescription;
    }

    /**
     * Setter for property libraryDescription.
     * @param libraryDescription New value of property libraryDescription.
     */
    public void setLibraryDescription(String libraryDescription) {

        this.libraryDescription = libraryDescription;
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
