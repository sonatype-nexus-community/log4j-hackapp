package com.sonatype.demo.log4shell;

import java.io.IOException;
import java.util.*;

public class ResultsStore {

   private final List<Result> results=new LinkedList<>();
   private final Map<JavaVersion,Console> byJavaVersion=new TreeMap<>();
   private final Map<String,SummaryRecord> summary=new TreeMap<>();


    public Console addResults(DriverConfig dc, String line) {
        return addResults(dc,new String[]{line});
    }

    public Console addResults(DriverConfig dc, String[] lines) {


        // create a console representation
        Console  c=byJavaVersion.get(dc.jv);
        if(c==null) throw new NullPointerException("missing console");

        List<Result> runList=ResultsParser.parse(dc,lines);
        for(Result r:runList) {
            r.id=results.size()+1;
            results.add(r);
            c.addResult(dc,r);

            String sk=r.jv.version+"/"+r.lv.version;
            SummaryRecord sr=summary.get(sk);
            if(sr==null) {
                sr=new SummaryRecord(r.jv.version,r.lv.version);
                summary.put(sk,sr);
            }
            sr.score[r.type.ordinal()]=sr.score[r.type.ordinal()]+1;
        }

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

    public Result getEntry(int resultID) {
        return results.get(resultID-1);
    }

    public List<Result> getResults() {
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
