package com.sonatype.demo.log4shell.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Runner {

    public static final String JDEMO_PROPS = "LOG4JDEMO_PROPS";

    public static final String PAYLOAD_RESULTS_SEPERATOR = "-- Runner Payload Start -----------";
    public static final String PAYLOAD_RESULTS_TERMINATOR = "-- Runner Payload End  -----------";
    public static final String SYSTEM_PROPERTY_VALUE = "-- Runner Property ---------------";

    public static final String PAYLOAD_SELECTOR = "payload";
    public static final String REPORT_SELECTOR = "report";
    public static final String LOG_SELECTOR = "log";
    public static final String PROPERTIES_SELECTOR = "properties";
    public static final String COMBO_SELECTOR = "combo";

    public static final String REPORT_CMD = "--"+REPORT_SELECTOR;
    public static final String PAYLOAD_CMD = "--"+ PAYLOAD_SELECTOR;
    public static final String LOG_CMD = "--"+LOG_SELECTOR;
    public static final String PROPERTIES_CMD = "--"+PROPERTIES_SELECTOR;
    public static final String COMBO_CMD = "--"+COMBO_SELECTOR;


    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(Runner.class);
        if(args.length<1) {
            logger.error("!!? no parameters");
            return;
        }

        Map<String, List<String>> paramters=parseParams(args);

       List<String> propList=paramters.get(REPORT_SELECTOR);

        if(propList!=null) {

            Properties p= System.getProperties();
            for(String key:propList) {
                if(p.containsKey(key)) {
                    System.out.println(SYSTEM_PROPERTY_VALUE +" "+key+"!!="+p.getProperty(key));
                } else {
                    System.out.println(SYSTEM_PROPERTY_VALUE +" "+key+"!!?");
                }
            }
        }

        
        List<String> msgs=paramters.get(PAYLOAD_SELECTOR);
        // payloads come as a pair
        // element 1 is the id
        // element 2 is the actual payload
        Iterator<String> i=msgs.iterator();
        while(i.hasNext()) {
            String id=i.next();
            String payload=i.next();
            System.out.println(PAYLOAD_RESULTS_SEPERATOR +" "+id+" "+payload);
            logger.info(payload);
            System.out.println(PAYLOAD_RESULTS_TERMINATOR +" "+id);
        }

    }

    static Map<String, List<String>> parseParams(String[] args) {


        Map<String, List<String>> results=new HashMap<>();
        String type= PAYLOAD_SELECTOR;
        List<String> entries=new LinkedList<>();
        results.put(type,entries);

        for(String s:args) {

            if(s.startsWith("--")) {
                type=s.substring(2);
                entries=results.get(type);
                if(entries==null) {
                    entries=new LinkedList<>();
                    results.put(type,entries);
                }

            } else {
                entries.add(s);
            }
        }

        return results;
    }
}
