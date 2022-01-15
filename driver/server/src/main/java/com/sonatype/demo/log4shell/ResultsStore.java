package com.sonatype.demo.log4shell;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class ResultsStore {

   private final List<Result> results=new LinkedList<>();
   // private PrintWriter pw;

    public Map<JavaVersion,Console> byJavaVersion=new TreeMap<>();
    private Driver d;

    public ResultsStore(Driver driver) throws IOException {
        d=driver;
    //    pw=new PrintWriter(new File("/tmp/results.csv"));
     //   pw.print("java,log4j,actual_java,mutated,message,result");
      //  for(String s:d.vmProperties.keySet()) {
      //      pw.print(","+s);
      //  }
      //  pw.println("");


    }
    public Console addResults(DriverConfig dc, String line) {
        List<String> s=new LinkedList<>();
        s.add(line);
        return addResults(dc,s);
    }

    public Console addResults(DriverConfig dc, List<String> lines) {


        Result r=ResultsParser.parse(dc,lines);
        r.id=results.size()+1;
        results.add(r);


        // create a console representation
        Console  c=byJavaVersion.get(dc.jv);


        if(c==null) throw new NullPointerException("missing console");
        c.addResult(dc,r);


        printResult(r);

        return c;
    }

    private void printResult(Result r) {
        /*
        pw.print(r.config.jv.version);
        pw.print(",");
        pw.print(r.config.lv.version);
        pw.print(",");
        pw.print(r.reportingProperties.get("java.version"));
        pw.print(",");
        pw.print(r.mutated);
        pw.print(",");
        pw.print("\""+r.config.msg+"\"");
        pw.print(",");
        pw.print("\""+String.join("/",r.lines)+"\"");
        for(SystemProperty s:d.vmProperties.values()) {
            if(r.config.vmargs.contains(s)) {
                pw.print(",true");
            } else {
                pw.print(",false");
            }
        }
        pw.println("");
        pw.flush();
        */

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
}
