

/*
 * Util.java
 *
 * Created on 29 de septiembre de 2004, 18:07
 */

package ve.usb.jgm.tools.stubgenerator;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.log4j.*;
import java.util.*;
/**
 *
 * @author  jdeoliveira
 */
public class Util {
    
    private static Logger logger = Logger.getLogger(Util.class);
    
    public int getArraySize(Object[] array) {
        if (array == null)
            return 0;
        return array.length;
    }
    
    public boolean isNull(Object o) {
        return (o == null);
    }
    
    public String replaceInnerClass(String _in) {
        if (_in.startsWith("java.")) {
            return _in.replace('$', '.');
        } else {
            return _in;
        }
    }
    
    public Method getConstructor(JavaClass theSubClass) {
        JavaClass theClass = theSubClass.getSuperClass();
        
        logger.debug("Searching constructor of " + theClass.getClassName() + " for " + theSubClass.getClassName());
        
        String subClassName = theSubClass.getClassName();
        String superClassName = theClass.getClassName();
        
        int minArgs = 999;
        Method theCons = null;
        Method[] theMethods = theClass.getMethods();
        for (int i = 0; i < theMethods.length; i++) {
            Method aMethod = theMethods[i];
            
            logger.debug("A method is " + aMethod.getName() + " is protected: " + aMethod.isProtected() + " is public: " + aMethod.isPublic() + " is private: " + aMethod.isPrivate());
            logger.debug("has parameters: " + aMethod.getSignature());
            if (aMethod.getName().equals("<init>") && (!aMethod.isPrivate())) {
                if (!aMethod.isPublic() && !aMethod.isProtected()) {
                    logger.debug("the method has package access");
                    //El metodo tiene acceso default, chequeamos el paquete
                    if (subClassName.substring(0, subClassName.lastIndexOf(".")).equals(superClassName.substring(0, superClassName.lastIndexOf(".")))) {
                        //la subclase tiene el mismo paquete que la superclase, seguimos
                        logger.debug("The super and subclass belongs to the same packages, OK");
                    } else {
                        logger.debug("The super and subclass belongs to diferent packages, trying next method");
                        continue;
                    }
                }
                //IMPORTANT: La revision del paquete esta comentada, hay que probar con otras libs a ver
                //si continua funcionando con esto (es necesario para que java.mail funcione
                //===>> MUST CHECK PACKAGE ACCESS. si el constructor encontrado es default (no es private pero
                //tampoco es public, entonces solo puede usarse si y solo si los paquetes de la clase y
                //la superclase coinciden (test case: bean utils)
                logger.debug("Found a suitable cons: " + aMethod.getSignature());

                int args = aMethod.getArgumentTypes().length;
                if (args < minArgs) {
                    minArgs = args;
                    theCons = aMethod;
                }
            } 
        }
        if (theCons == null) {
            //se deberia lanzar una excepcion aqui
            logger.debug(theClass.getClassName() + " has no constructor suitable");
        }
        return theCons;
    }
    
    public boolean isConstructor(Method theMethod) {
        return theMethod.getName().equals("<init>");
    }
    
    public String extractClassName(JavaClass theClass) {
        String name = theClass.getClassName();
        if (name.indexOf('$') != -1) {
            //es una clase interna, devolvemos desde el $ hasta el fin
            return name.substring(name.lastIndexOf('$') + 1);
        } else {
            return name.substring(name.lastIndexOf('.') + 1);
        }
    }
    
    public boolean isStaticInnerClass(JavaClass outerClass, JavaClass innerClass) {
        
        logger.debug("Determining if this inner class is static: " + innerClass.toString());
        
        InnerClasses innerAt = null;
        for (Attribute at: outerClass.getAttributes()) {
            try {
                innerAt = (InnerClasses)at;
            } catch (ClassCastException e) {
                //nada, continuar con el proximo at
            }
            
        }
        
        if (innerAt == null) {
            logger.warn("The class " + outerClass.getClassName() + " contains inner classes but doesn't have the corresponding constant pool attribute");
            return false;
        }
        
        AccessFlags flags = null;
        ConstantPool pool = outerClass.getConstantPool();
        for (InnerClass i: innerAt.getInnerClasses()) {
            try {
                ConstantClass c = (ConstantClass)pool.getConstant(i.getInnerClassIndex());
                
                String innerName = c.getBytes(pool).replace('/', '.');
                logger.debug("Found a constant pool entry: " + innerName);
                
                if (innerName.equals(innerClass.getClassName())) {
                    //hack para poder interpretar los access flags usando un metodo dummy
                    flags = new Method();
                    flags.setAccessFlags(i.getInnerAccessFlags());

                    logger.debug("Access flags says it's " + ((flags.isStatic())?("static"):("non-static")));
                    
                    return flags.isStatic();
                }
            } catch (ClassCastException e) {
                //nada, continuar con el proximo i
            }
        }
        
        logger.warn("The class " + outerClass.getClassName() + " doesn't have information for it's inner class " + innerClass.getClassName());
        
        return false;
    }
    
    public boolean hasAnonymousClassParameters(Method m) {
        for (Type t: m.getArgumentTypes()) {
            if (t.toString().indexOf('$') != -1) {
                String[] temp = t.toString().split("\\$");
                try {
                    new Integer(temp[1]);
                    //si caemos aqui es porque realmente es un entero (la clase es anonima)
                    return true;
                } catch (NumberFormatException f) {
                    //si caemos aqui estamos bien, no es anonimo el parametro
                }
            }
        }
        return false;
    }
    
    public boolean overridesConstructor(JavaClass cl, Method m) {
        logger.debug("Verifing if " + cl.getClassName() + " declares new constructors");
        if (m.getName().equals("<init>")) {

            JavaClass sup = cl.getSuperClass();
            if (sup != null) {
                for (Method aMethod: sup.getMethods()) {
                    if (aMethod.getName().equals("<init>")) {
                        if (Arrays.deepEquals(aMethod.getArgumentTypes(), m.getArgumentTypes())) {
                            logger.debug("Both methods had the same parameters, returning false");
                            return false;
                        }
                    }
                }
                //no encontramos constructor con los mismos parametros en la superclase, devolvemos true
                logger.debug("No super constructor with same parameters");
                return true;

            } else {
                //superclas es null (raro) entonces SI override constructor 
                logger.debug("Doesn't have superclass");
                return true;
            }
        } else {
            //este metodo no es constructor, devolvemos true para que entre en el if del template
            logger.debug("Supplied method is not constructor");
            return true;
        }
    }
    
    /*public String getConstant(Field field) {
        return field.getConstantValue();
    }*/
    
    public String[] getExceptionNames(Method m) {
        String[] out = new String[0];
        if (m.getExceptionTable() != null) {
            logger.debug("Method " + m.getName() + " has normal exception table");
            out = m.getExceptionTable().getExceptionNames();
        } else {
            logger.debug("Method " + m.getName() + " has no exception table");
            
            //aqui deberia buscar los nombres de las exceptions a partir de
            //reflection.
            
            //no hace falta, porque ahora nunca devuelve null, al tener
            //los stubs de las deps durante la generacion de stubs con bcel+velocity
            
        }
        return out;
    }
    
    //esto ya no hace falta, porque 
    public String getSuperClassName(JavaClass theClass) {
        ClassGen g = new ClassGen(theClass);
        return g.getSuperclassName();
    }
    
}
