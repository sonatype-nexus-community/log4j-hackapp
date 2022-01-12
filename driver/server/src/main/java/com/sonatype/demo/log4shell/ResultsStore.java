package com.sonatype.demo.log4shell;

import java.util.*;

public class ResultsStore {

    private final List<Result> results=new LinkedList<>();
    public Map<JavaVersion,Console> byJavaVersion=new TreeMap<>();

    public Console addResults(DriverConfig dc, String[] lines) {

        Result r=new Result(dc,lines);
        results.add(r);
        Console  c=byJavaVersion.get(dc.jv);
        if(c==null) throw new NullPointerException("missing console");
        if(lines!=null) {
            for(String l:lines) {
                c.records.add(l);
            }
        }

        return c;
    }

    public Console getJavaVersionResults(JavaVersion v) {

       return byJavaVersion.get(v);
    }

    public void addJavaVersion(JavaVersion jv) {
        Console  c=byJavaVersion.get(jv);
        if(c==null) {
            c=new Console(jv.version);
            byJavaVersion.put(jv,c);
        }
    }

    public void clear() {
        results.clear();
        for(Console c:byJavaVersion.values()) {
            c.records.clear();
        }
    }
}
