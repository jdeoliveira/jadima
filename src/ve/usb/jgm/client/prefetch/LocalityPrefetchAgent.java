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
import org.apache.log4j.*;
import java.io.*;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class LocalityPrefetchAgent extends PrefetchAgent {

    private static final Logger logger = Logger.getLogger(LocalityPrefetchAgent.class);
    
    //perfil de ejecucion anterior (previamente generado y cargado)
    private Map<Bytecode, Long> oldProfile;
    private ArrayList<Map.Entry<Bytecode, Long>> orderedOldProfile;
    
    //perfil de ejecucion actual
    private Map<Bytecode, Long> newProfile;
    
    //clases en proceso de carga (nombre, timestamp de solicitud)
    private Map<String, Long> currentTransactions;
    
    //acumulado de tiempos de transferencia
    private long accumulatedTransferTime = 0;
    
    //thereshold de prefetch (prefetching window size)
    private long thereshold = 0;
    
    private ArrayList<String> alreadyLoadedClasses;
    
    public LocalityPrefetchAgent() {
        oldProfile = new HashMap<Bytecode, Long>();
        newProfile = new HashMap<Bytecode, Long>();
        alreadyLoadedClasses = new ArrayList<String>();
        currentTransactions = new HashMap<String, Long>();
    }

    public void beginTransaction(String className, int majorVersion, int minorVersion) {
        //comenzar contador de tiempo para la clase que estan pidiendo
        
        logger.debug("Begin transaction for " + className + " "  + majorVersion + "." + minorVersion);
        
        //1. tomamos el tiempo actual
        long now = System.currentTimeMillis();
        
        //2. guardamos la entrada en la lista de transacciones actual
        currentTransactions.put(className, now);
        
        //3. calculamos el tiempo efectivo de carga
        long effectiveRequestTime = now - accumulatedTransferTime;
        
        //4. lo guardamos en el perfil actual
        newProfile.put(new Bytecode(className, majorVersion, minorVersion), effectiveRequestTime);
        
        logger.debug("Class " + className + " requested at " + now + ", effective request time is " + effectiveRequestTime);
    }

    public void configure(com.softcorporation.xmllight.Element el) throws PrefetchAgentConfigurationException {
        //nothing needs to be configurated

        try {
            logger.debug("Starting Locality Prefetch Agent");
            thereshold = new Integer(el.getAttr("thereshold"));
            logger.debug("Locality Prefetch Agent configured with a thereshold of " + thereshold + " msec");
            
        /*} catch (XMLLightException e) {
            logger.warn("Errors parsing prefetch configuration data", e);
            throw new RepositoryClientConfigurationException("Errors parsing prefetch configuration data", e);*/
        } catch (NumberFormatException e) {
            logger.warn("Errors parsing prefetch configuration data (supplied thereshold value is not an integer)", e);
            throw new PrefetchAgentConfigurationException("Errors parsing prefetch configuration data (supplied thereshold value is not an integer)", e);
        }
    }

    public void endTransaction(String className, int majorVersion, int minorVersion) {
        //calcular tiempo de transferencia y tiempo de carga efectivo
        
        //TODO: por ahora no nos importa la version, con el nombre es suficiente
        
        //1. tomamos el tiempo actual
        long arrivalTime = System.currentTimeMillis();
        
        //2. tomamos el tiempo en que fue solicitada
        long requestTime = currentTransactions.get(className);
        
        //3. calculamos el tiempo de transferencia
        long transferTime = arrivalTime - requestTime;
        
        //4. lo agregamos al tiempo acumulado
        accumulatedTransferTime += transferTime;
        
        //5. Sacamos la clase de la lista de transacciones
        currentTransactions.remove(className);

        logger.debug("Class " + className + " received at " + arrivalTime + ", added " + transferTime + " to accumulated transfer time (now is " + accumulatedTransferTime + ")");

    }

    public Collection<Bytecode> getRelatedClasses(ve.usb.jgm.repo.Bytecode mainClass) {
        
        //aqui debemos analizar el mapa actual de clases para determinar los clusters
        
        //TODO: por ahora no consideramos el numero de version, solo nos interesa el nombre
        //TODO: el thereshold esta cableado
        
        //int thereshold = 500;
        
        HashSet<Bytecode> related = new HashSet<Bytecode>();
        
        logger.debug("Determing related classes to " + mainClass);
        
        alreadyLoadedClasses.add(mainClass.getClassName());
        if (oldProfile.containsKey(mainClass)) {
        
            //vemos el tiempo de carga efectivo de la clase solicitada
            long effective = oldProfile.get(mainClass);

            logger.debug(mainClass.getClassName() + " was loaded effectively at " + effective);


            //el perfil esta ordenado, lo iteramos
            Iterator<Map.Entry<Bytecode, Long>> it = orderedOldProfile.iterator();
            while (it.hasNext()) {
                Map.Entry<Bytecode, Long> current = it.next();
                
                logger.debug("comparing with " + current.getKey() + " loaded at " + current.getValue());
                
                if (!(current.getKey().getClassName().equals(mainClass.getClassName()))) {
                    if (alreadyLoadedClasses.contains(current.getKey().getClassName())) {
                        logger.debug("Already loaded, not adding to related classes");
                    } else {
                        long difference = Math.abs(current.getValue() - effective);

                        logger.debug("difference: " + difference + ", thereshold " + thereshold);

                        if (difference <= thereshold) {
                            logger.debug("Is in the cluster, adding it");
                            related.add(current.getKey());
                            alreadyLoadedClasses.add(current.getKey().getClassName());
                        } else {
                            logger.debug("Is not in the cluster");
                        }
                    }
                }
            }
            
        } else {
            logger.debug(mainClass.getClassName() + " was never loaded before");
        }
        
        logger.debug("Done determing related classes to " + mainClass.getClassName());
        
        return related;
    }

    public void init() throws PrefetchAgentInitializationException {
        //cargamos el archivo de profile (esta cableado a "/tmp/prefetch-profile.dat"
        logger.debug("Initializing Locality Prefetch Agent");
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream("/tmp/prefetch-profile.dat"));
            oldProfile = (HashMap<Bytecode, Long>)in.readObject();
            
            //primero ordenamos el perfil
            orderedOldProfile = new ArrayList<Map.Entry<Bytecode, Long>>(oldProfile.entrySet());
            Collections.sort(orderedOldProfile, new Comparator<Map.Entry<Bytecode, Long>>() {
                
                public int compare(Map.Entry<Bytecode, Long> obj1, Map.Entry<Bytecode, Long> obj2) {
                    long c1 = obj1.getValue().longValue();
                    long c2 = obj2.getValue().longValue();
                    if (c1 < c2) {
                        return -1;
                    } else if (c1 > c2) {
                        return 1;
                    } else { 
                        return 0;
                    }
                }               
            }
            
            
            );        
            
            logger.debug("Loaded execution profile: " + Arrays.deepToString(orderedOldProfile.toArray()));
        } catch (FileNotFoundException e) {
            logger.info("Execution profile not found, not using prefetch");
        } catch (IOException e) {
            logger.error("Exception de-serializing previous execution profile", e);
        } catch (ClassNotFoundException e) {
            logger.error("Exception de-serializing previous execution profile", e);
        } catch (ClassCastException e) {
            logger.error("Exception de-serializing previous execution profile", e);
        } finally {
            try { in.close(); } catch (Exception e) {}
        }
        
        Runtime r = Runtime.getRuntime();
        r.addShutdownHook(new Thread() {
            public void run() {
                //aqui serializamos el profile nuevo al archivo "/tmp/prefetch-profile.dat"
                logger.debug("FINALIZING PREFETCH AGENT");

                ObjectOutputStream os = null;
                try {
                    os = new ObjectOutputStream(new FileOutputStream("/tmp/prefetch-profile.dat"));
                    os.writeObject(newProfile);
                } catch (IOException e) {
                    logger.error("Exception serializing current execution profile", e);
                } finally {
                    try { os.close(); } catch (Exception e) {}
                }       
            } 
            
        });
    }
    
    public void finalize() {
        
        
    }
    
}
