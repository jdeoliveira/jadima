/*
 * JgmClassLoaderFactory.java
 *
 * Created on 15 de mayo de 2005, 11:55 PM
 */

package ve.usb.jgm.client;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class JgmClassLoaderFactory {
    
    public static JgmClassLoader createJgmClassLoader() {
        return new JgmClassLoader();
    }
    
}
