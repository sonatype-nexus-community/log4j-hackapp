package com.sonatype.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class Runner {

 // -Dcom.sun.jndi.ldap.object.trustURLCodebase=true -Dcom.sun.jndi.rmi.object.trustURLCodebase=true
 //While RCE is not possible without these flags, you will still get pingback, in minecraft's example, allowing you to get the IP of everyone connected.
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(Runner.class);
        if(args.length<1) {
            logger.error("!!? no parameters");
            return;
        }


        String msg=args[0];

        if(args.length>1) {
            Properties p= System.getProperties();

            for(int i=1;i<args.length;i++) {
                String key=args[i];
                if(p.containsKey(key)) {
                    logger.warn(key+" !!= {}",p.getProperty(key));
                }
                else {
                    logger.error(key+" !!- missing");
                }

            }
        }



        try {

            logger.info("!!! "+msg);

        } catch(Throwable t) {
            t.printStackTrace();;
        }
    }
}
