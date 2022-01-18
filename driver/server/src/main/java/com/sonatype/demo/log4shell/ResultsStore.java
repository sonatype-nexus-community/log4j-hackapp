package com.sonatype.demo.log4shell;

import java.util.*;

public class ResultsStore {

   private final List<TestResult> results=new LinkedList<>();
   private final Map<JavaVersion,Console> byJavaVersion=new TreeMap<>();
   private final Map<String,SummaryRecord> summary=new TreeMap<>();


    //public Console addResults(TestResult dc, String line) {
     //   return addResults(dc,new String[]{line});
   // }

    public Console addResults(TestResult dc) {


        // create a console representation
        Console  c=byJavaVersion.get(dc.jv);
        if(c==null) throw new NullPointerException("missing console");
            dc.id=results.size()+1;
            results.add(dc);
            c.addResult(dc);

            String sk=dc.jv.version+"/"+dc.lv.getVersion();
            SummaryRecord sr=summary.get(sk);
            if(sr==null) {
                sr=new SummaryRecord(dc.jv.version,dc.lv.getVersion());
                summary.put(sk,sr);
            }
            sr.score[dc.type.ordinal()]=sr.score[dc.type.ordinal()]+1;


        return c;
    }


    public Console getJavaVersionResults(JavaVersion v) {

       return byJavaVersion.get(v);
    }

    public void addJavaVersion(JavaVersion jv) {

            Console c = byJavaVersion.get(jv);
            if (c == null) {
                c = new Console(jv.version);
                byJavaVersion.put(jv, c);
            }

    }

    public void clear() {
        results.clear();
        summary.clear();
        for(Console c:byJavaVersion.values()) {
            c.records.clear();
        }
    }

    public TestResult getEntry(int resultID) {
        return results.get(resultID-1);
    }

    public List<TestResult> getResults() {
        return results;
    }

    public Collection<Console> getConsoles() {
        return byJavaVersion.values();
    }

    public Console getConsole(JavaVersion jv) {
        return byJavaVersion.get(jv);
    }

    public Collection<SummaryRecord> getSummary() {
            return summary.values();
    }
}
