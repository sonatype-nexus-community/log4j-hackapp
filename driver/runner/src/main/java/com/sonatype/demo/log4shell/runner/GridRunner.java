package com.sonatype.demo.log4shell.runner;

import java.util.*;

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

        if(payloads==null || payloads.isEmpty()) {
            throw new RuntimeException("no payloads specified");
        }


        if(logversions==null || logversions.isEmpty()) {
            throw new RuntimeException("no log versions specified");
        }


        Set<Set<VMProperty>> propertyCombos=new HashSet<>();

        if(parameters.containsKey(COMBO_SELECTOR)) {
           propertyCombos=ComboBuilder.buildCombos(vmProperties);
        }
        else {
            Set<VMProperty> props=new HashSet<>();
            props.addAll(vmProperties);
            propertyCombos.add(props);
        }

        // run all tests for the given list of Log4J versions

        for(String s:logversions) {

            String[] bits=s.split("::");
            String version=bits[0];
            String location=bits[1];

            for(Set<VMProperty> propcombo:propertyCombos) {
                runLogTest(version, location, reports, payloads, propcombo);
            }
        }


    }



    private static void runLogTest(String version, String location, List<String> reports, List<String> payloads, Set<VMProperty> vmProperties) throws Exception {

        String cp=System.getProperty("java.class.path");

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

        JavaProcessLauncher.javaLaunch(myCp,Runner.class.getCanonicalName(),vmProperties,app);
    }


    private static List<VMProperty> parseProperties(List<String> parameters) {

        List<VMProperty> r=new LinkedList<>();
        if(parameters==null || parameters.isEmpty()) return r;
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
