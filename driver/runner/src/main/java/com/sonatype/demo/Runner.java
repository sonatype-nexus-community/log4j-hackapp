package com.sonatype.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class Runner {

    public static final String JDEMO_PROPS = "LOG4JDEMO_PROPS";

    // -Dcom.sun.jndi.ldap.object.trustURLCodebase=true -Dcom.sun.jndi.rmi.object.trustURLCodebase=true
 //While RCE is not possible without these flags, you will still get pingback, in minecraft's example, allowing you to get the IP of everyone connected.
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(Runner.class);
        if(args.length<1) {
            logger.error("!!? no parameters");
            return;
        }

       String proplist=System.getenv(JDEMO_PROPS);

        if(proplist!=null) {
            String[] propNames=proplist.split(" ");
            Properties p= System.getProperties();
            for(String key:propNames) {
                if(p.containsKey(key)) {
                    logger.warn("!!={}/{}",key,p.getProperty(key));
                }
            }
        }
        String msg=args[0];

        try {

            logger.info(msg);

        } catch(Throwable t) {
            t.printStackTrace();;
        }
    }
}
