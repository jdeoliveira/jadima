/*
 * Library.java
 *
 * Created on 14 de mayo de 2005, 01:18 PM
 */

package ve.usb.jgm.repo;

import java.util.*;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class Library {

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
    private String[] allowedRoles;

    /**
     * Holds value of property versions.
     */
    private Version[] versions;
    
    /** Creates a new instance of Library */
    public Library() {
        versions = new Version[0];
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
    public String[] getAllowedRoles() {

        return this.allowedRoles;
    }

    /**
     * Setter for property allowedRoles.
     * @param allowedRoles New value of property allowedRoles.
     */
    public void setAllowedRoles(String[] allowedRoles) {

        this.allowedRoles = allowedRoles;
    }

    /**
     * Getter for property versions.
     * @return Value of property versions.
     */
    public Version[] getVersions() {

        return this.versions;
    }

    /**
     * Setter for property versions.
     * @param versions New value of property versions.
     */
    public void setVersions(Version[] versions) {

        this.versions = versions;
    }
    
    public void addVersion(Version v) {
        Version[] newVer = new Version[versions.length + 1];
        System.arraycopy(versions, 0, newVer, 0, versions.length);
        newVer[versions.length] = v;
        versions = newVer;
    }
    
}
