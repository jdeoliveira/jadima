/*
 * Library.java
 *
 * Created on 7 de junio de 2005, 16:53
 */

package ve.usb.jgm.repo.backend.hibernate;

/**
 *
 * @author  jdeoliveira
 */
public class HbLibrary {

    /**
     * Holds value of property id.
     */
    private Long id;

    /**
     * Holds value of property name.
     */
    private String name;

    /**
     * Holds value of property description.
     */
    private String description;

    /**
     * Holds value of property allowedRoles.
     */
    private java.util.Set allowedRoles = new java.util.HashSet();

    /**
     * Holds value of property versions.
     */
    private java.util.Set versions = new java.util.HashSet();
    
    /** Creates a new instance of Library */
    public HbLibrary() {
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public Long getId() {

        return this.id;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(Long id) {

        this.id = id;
    }

    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public String getName() {

        return this.name;
    }

    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {

        this.name = name;
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
     * Getter for property versions.
     * @return Value of property versions.
     */
    public java.util.Set getVersions() {

        return this.versions;
    }

    /**
     * Setter for property versions.
     * @param versions New value of property versions.
     */
    public void setVersions(java.util.Set versions) {

        this.versions = versions;
    }
    
    public void addVersion(HbVersion v) {
        v.setLibrary(this);
        this.getVersions().add(v);
    }
}
