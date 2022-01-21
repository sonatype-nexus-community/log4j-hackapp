package com.sonatype.demo.log4shell.runner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.sonatype.demo.log4shell.runner.Runner.*;

public class GridRunner {

    public static final String GROUP_SEPERATOR =   "-- Grid Runner -----------------";

    public static void main(String[] args) throws Exception {

        System.out.println("Group Runner args=["+String.join(",",args));
        Map<String, List<String>> paramters=Runner.parseParams(args);

        System.out.println(paramters.keySet());
        List<String> payloads=paramters.get(PAYLOAD_SELECTOR);
        List<String> reports=paramters.get(REPORT_SELECTOR);
        List<String> logversions=paramters.get(LOG_SELECTOR);

        if(payloads==null || payloads.isEmpty()) {
            throw new RuntimeException("no payloads specified");
        }
        String cp=System.getProperty("java.class.path");

        if(logversions==null || logversions.isEmpty()) {
            throw new RuntimeException("no log versions specified");
        }
        for(String s:logversions) {
            String[] bits=s.split("::");
            String version=bits[0];
            String location=bits[1];
            String myCp=location+":"+cp;
            List<String> app=new LinkedList<>();
            if(reports!=null && reports.isEmpty()==false) {
                app.add(REPORT_CMD);
                app.addAll(reports);
            }
            app.add(PAYLOAD_CMD);
            app.addAll(payloads);

            System.out.println(GROUP_SEPERATOR +" "+version);
            ProcessLauncher.javaLaunch(myCp,Runner.class.getCanonicalName(),paramters.get(PROPERTIES_SELECTOR),app);
        }


    }
}
