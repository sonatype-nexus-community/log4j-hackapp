package com.sonatype.demo.log4shell;


import java.util.*;

public class ResultSet {


    private Map<String, Map<String,TestResult>> msgsets=new HashMap<>();

    private TestResult buildTestResult(String logVersion, String preMsg) {
        if(logVersion==null) return null;
        if(preMsg==null) return null;

        Map<String,TestResult> set=msgsets.get(logVersion);

        if(set==null) {
            set=new HashMap<>();
            msgsets.put(logVersion,set);
        }

        TestResult tr=set.get(preMsg);
        if(tr==null) {
            tr=new TestResult(preMsg);
            set.put(preMsg,tr);
        }
        return tr;

    }
    public void add(String logVersion, String preMsg, String line) {

        TestResult tr=buildTestResult(logVersion,preMsg);
        if(tr!=null) tr.add(line);
    }

    public Collection<String> logVersionNames() {
        return msgsets.keySet();
    }

    public Collection<String> getVersionMessages(String v) {
        return msgsets.get(v).keySet();
    }

    public TestResult getResults(String v, String premsg) {

        return msgsets.get(v).get(premsg);
    }

    public void addMissingProperty(String logVersion, String preMsg,String key) {
        TestResult tr=buildTestResult(logVersion,preMsg);
        if(tr!=null) tr.addPropertyReport(key,"n/a");
    }

    public void addPropertyValue(String logVersion, String preMsg,String key,String value) {
        TestResult tr=buildTestResult(logVersion,preMsg);
        if(tr!=null) tr.addPropertyReport(key,value);
    }

}
