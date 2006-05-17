/*
 * DummyPrefetchAgent.java
 *
 * Representa un agente de prefetching inutil, para pruebas
 *
 * Created on 18 de mayo de 2005, 12:54 AM
 */

package ve.usb.jgm.client.prefetch;

import ve.usb.jgm.client.*;
import ve.usb.jgm.repo.*;
import java.util.*;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class NoPrefetchAgent extends PrefetchAgent {
    
    public NoPrefetchAgent() {
    }

    public void beginTransaction(String className, int majorVersion, int minorVersion) {
        //do nothing
    }

    public void configure(com.softcorporation.xmllight.Element el) throws PrefetchAgentConfigurationException {
        //nothing needs to be configurated
    }

    public void endTransaction(String className, int majorVersion, int minorVersion) {
        //do nothing
    }

    public Collection<Bytecode> getRelatedClasses(ve.usb.jgm.repo.Bytecode mainClass) {
        //return an empty collection, this prefetch agent prefetches nothing
        return new HashSet<Bytecode>();
    }

    public void init() throws PrefetchAgentInitializationException {
        //do nothing
    }
    
}
