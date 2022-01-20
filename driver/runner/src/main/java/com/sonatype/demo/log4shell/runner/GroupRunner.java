package com.sonatype.demo.log4shell.runner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.sonatype.demo.log4shell.runner.Runner.*;

public class GroupRunner {

    public static final String RUNNER_GROUP_SEPERATOR =   "-- Runner Group -----------------";

    public static void main(String[] args) throws Exception {

        System.out.println("Group Runner args=["+String.join(",",args));
        Map<String, List<String>> paramters=Runner.parseParams(args);

        System.out.println(paramters.keySet());
        List<String> messages=paramters.get(MSG_SELECTOR);
        List<String> reports=paramters.get(REPORT_SELECTOR);
        List<String> logversions=paramters.get(LOG_SELECTOR);

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
            app.add(MSG_CMD);
            app.addAll(messages);

            System.out.println(RUNNER_GROUP_SEPERATOR+" "+version);
            ProcessLauncher.javaLaunch(myCp,Runner.class.getCanonicalName(),paramters.get(PROPERTIES_SELECTOR),app);
        }


    }
}
