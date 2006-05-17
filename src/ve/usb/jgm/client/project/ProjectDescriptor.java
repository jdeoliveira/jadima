/*
 * ProjectDescriptor.java
 *
 * Created on 4 de mayo de 2005, 11:48 PM
 */

package ve.usb.jgm.client.project;

import java.util.*;
/**
 *
 * @author  Administrator
 */
public class ProjectDescriptor {

    private String name;
    private String description;
    private Collection<Dependency> dependencies;
    
    /** Creates a new instance of ProjectDescriptor */
    public ProjectDescriptor() {
        name = "";
        description = "";
        dependencies = new LinkedList<Dependency>();
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

    public Collection<Dependency> getDependencies() {
        return dependencies;
    }
    
    public void addDependency(Dependency d) {
        dependencies.add(d);
    }
    
    public void removeDependency(Dependency d) {
        dependencies.remove(d);
    }
    
    /*public void sortDependencies() {
        Collections.sort(dependencies, new Comparator() {
            public int compare(Object o1, Object o2) {
                Dependency d1 = (Dependency)o1;
                Dependency d2 = (Dependency)o2;
                return d1.getPriority() - d2.getPriority();
            }
        });
    }*/
}
