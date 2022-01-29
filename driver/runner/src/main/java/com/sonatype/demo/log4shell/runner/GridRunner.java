package com.sonatype.demo.log4shell.runner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.sonatype.demo.log4shell.runner.Runner.*;

public class GridRunner {

    public static final String LOG4J_VERSION_SEPERATOR =   "-- Grid Runner -----------------";
    public static final String LOG4J_VM_PROPERTY       =   "-- VM Property -----------------";

    public static void main(String[] args) throws Exception {


        Map<String, List<String>> parameters=Runner.parseParams(args);

        List<String> payloads=parameters.get(PAYLOAD_SELECTOR);
        List<String> reports=parameters.get(REPORT_SELECTOR);
        List<String> logversions=parameters.get(LOG_SELECTOR);
        List<VMProperty> vmProperties=parseProperties(parameters.get(PROPERTIES_SELECTOR));

        boolean combo=parameters.containsKey(COMBO_SELECTOR);

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

            System.out.println(LOG4J_VERSION_SEPERATOR +" "+version);
            for(VMProperty vm:vmProperties) {
                System.out.println(LOG4J_VM_PROPERTY+" "+vm.id+" "+vm.name);
            }
        
            ProcessLauncher.javaLaunch(myCp,Runner.class.getCanonicalName(),vmProperties,app);
        }


    }


    private static List<VMProperty> parseProperties(List<String> parameters) {

        List<VMProperty> r=new LinkedList<>();
        for(int i=0;i<parameters.size();i++) {
            VMProperty p=new VMProperty();
            p.id=parameters.get(i);
            i++;
            p.name=parameters.get(i);
            i++;
            p.value=parameters.get(i);
           r.add(p);
        }

        return r;
    }


}
