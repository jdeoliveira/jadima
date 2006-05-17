/*
 * InputStreamUtil.java
 *
 * Created on 30 de mayo de 2005, 11:45 AM
 */

package ve.usb.jgm.util;

import java.io.*;
import org.apache.log4j.*;


/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class InputStreamUtil {
    
    private static final Logger logger = Logger.getLogger(InputStreamUtil.class);
    
    public static byte[] readAll(InputStream in) throws IOException {
        int totalReadedBytes = 0;
        int readedBytes = 0;
        byte[] smallBuffer = new byte[10240]; 
        byte[] longBuffer = new byte[10485760];

        while ((readedBytes = in.read(smallBuffer)) != -1) {

            for (int j = 0; j < readedBytes; j++) {
                longBuffer[j + totalReadedBytes] = smallBuffer[j];
            }   
            totalReadedBytes += readedBytes;
        }

        logger.debug("readed a total of " + totalReadedBytes);

        byte[] finalBytes = new byte[totalReadedBytes];
        for (int k = 0; k < totalReadedBytes; k++) {
            finalBytes[k] = longBuffer[k];
        }
        
        logger.debug("returning byte array of " + finalBytes.length);
        
        return finalBytes;
    }
    
}
