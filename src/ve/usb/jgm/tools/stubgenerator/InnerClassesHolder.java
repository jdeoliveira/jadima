/*
 * CollectionHolder.java
 *
 * Created on 5 de junio de 2005, 01:18 PM
 */

package ve.usb.jgm.tools.stubgenerator;

import org.apache.bcel.classfile.*;
import java.util.*;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class InnerClassesHolder {
    
    private Collection<JavaClass> namedInnerClasses;
    private Collection<JavaClass> anonymousInnerClasses;
    
    /** Creates a new instance of CollectionHolder */
    public InnerClassesHolder() {
        namedInnerClasses = new HashSet<JavaClass>();
        anonymousInnerClasses = new HashSet<JavaClass>();
    }
    
    public void addNamedInnerClass(JavaClass theClass) {
        namedInnerClasses.add(theClass);
    }
    
    public void addAnonymousInnerClass(JavaClass theClass) {
        anonymousInnerClasses.add(theClass);
    }
    
    public Collection<JavaClass> getNamedInnerClasses() {
        return namedInnerClasses;
    }
    
    public Collection<JavaClass> getAnonymousInnerClasses() {
        return anonymousInnerClasses;
    }
}

