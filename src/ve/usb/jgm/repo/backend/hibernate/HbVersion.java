/*
 * Version.java
 *
 * Created on 7 de junio de 2005, 16:54
 */

package ve.usb.jgm.repo.backend.hibernate;

/**
 *
 * @author  jdeoliveira
 */
public class HbVersion {

    /**
     * Holds value of property id.
     */
    private HbVersionId id;

    /**
     * Holds value of property major.
     */
    private int major;

    /**
     * Holds value of property minor.
     */
    private int minor;

    /**
     * Holds value of property description.
     */
    private String description;

    /**
     * Holds value of property revisions.
     */
    private java.util.Set revisions = new java.util.HashSet();

    /**
     * Holds value of property allowedRoles.
     */
    private java.util.Set allowedRoles = new java.util.HashSet();

    /**
     * Holds value of property stubsJar.
     */
    private byte[] stubsJar;

    /**
     * Holds value of property javadocZip.
     */
    private byte[] javadocZip;

    /**
     * Holds value of property library.
     */
    private HbLibrary library;
    
    /** Creates a new instance of Version */
    public HbVersion() {
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public HbVersionId getId() {

        return this.id;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(HbVersionId id) {

        this.id = id;
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
     * Getter for property revisions.
     * @return Value of property revisions.
     */
    public java.util.Set getRevisions() {

        return this.revisions;
    }

    /**
     * Setter for property revisions.
     * @param revisions New value of property revisions.
     */
    public void setRevisions(java.util.Set revisions) {

        this.revisions = revisions;
    }

    /**
     * Getter for property allowedRoles.
     * @return Value of property allowedRoles.
     */
    public java.util.Set getAllowedRoles() {

        return this.allowedRoles;
    }

    /**
     * Setter for property allowedRoles.
     * @param allowedRoles New value of property allowedRoles.
     */
    public void setAllowedRoles(java.util.Set allowedRoles) {

        this.allowedRoles = allowedRoles;
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
     * Getter for property javadocZip.
     * @return Value of property javadocZip.
     */
    public byte[] getJavadocZip() {
        return this.javadocZip;
    }

    /**
     * Setter for property javadocZip.
     * @param javadocZip New value of property javadocZip.
     */
    public void setJavadocZip(byte[] javadocZip) {

        this.javadocZip = javadocZip;
    }

    /**
     * Getter for property library.
     * @return Value of property library.
     */
    public HbLibrary getLibrary() {

        return this.library;
    }

    /**
     * Setter for property library.
     * @param library New value of property library.
     */
    public void setLibrary(HbLibrary library) {

        this.library = library;
    }
    
    public void addRevision(HbRevision r) {
        r.setVersion(this);
        this.getRevisions().add(r);
    }
}
