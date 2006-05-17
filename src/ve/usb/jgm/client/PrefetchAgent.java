/*
 * PrefetchAgent.java
 *
 * Created on 18 de mayo de 2005, 12:16 AM
 */

package ve.usb.jgm.client;

import java.util.*;
import ve.usb.jgm.repo.*;
import com.softcorporation.xmllight.*;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public abstract class PrefetchAgent {
    
    public abstract void configure(Element el) throws PrefetchAgentConfigurationException;
    
    public abstract void init() throws PrefetchAgentInitializationException;
    
    public abstract Collection<Bytecode> getRelatedClasses(Bytecode mainClass);
    
    public abstract void beginTransaction(String className, int majorVersion, int minorVersion);
    
    public abstract void endTransaction(String className, int majorVersion, int minorVersion);
    
}
