package com.sonatype.demo.log4shell;

import lombok.Data;

import java.util.*;

@Data
public class TestResult {

    public int id;
    public boolean mutated;
    public List<Record> console;
    ResultType type;
    JavaVersion jv;
    LogVersion lv;
    List<SystemProperty> vmargs=new LinkedList<>();
    Map<String,String> reportingProperties=new HashMap<>();
    private String logMsg;
    private List<String> results=new LinkedList<>();


    public TestResult(String preMsg) {
        this.logMsg=preMsg;
    }
    /*
    public TestResult(JavaVersion jv, LogVersion lv, List<SystemProperty> vmprops, String msg,String[] results) {
        this.jv=jv;
        this.lv=lv;
        this.logMsg =msg;
        this.results=results;
        this.vmargs=vmprops;

    }*/

    public void addPropertyReport(String k,String v) {
        reportingProperties.put(k,v);
    }

    public Integer[] getActivePropertyIDs() {
        List<Integer> li=new LinkedList<>();

        for(SystemProperty s:vmargs) {
            li.add(s.id);
        }
        return li.toArray(new Integer[0]);
    }

    public void setType(ResultType t) {
        this.type =t;
    }


    public String reportedJava() {
        return reportingProperties.getOrDefault("java.version","n/a");
    }


    public String getConsole() {
        StringBuilder sb=new StringBuilder();
        if(console!=null) {
            for(Record r: console) {
                sb.append(r.line);
                sb.append("<br>");
            }
        }
        return new String(sb);
    }

    public void add(String line) {
        results.add(line);
    }
}
