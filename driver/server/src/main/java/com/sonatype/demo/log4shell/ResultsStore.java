package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shell.config.Configuration;
import com.sonatype.demo.log4shell.config.JavaVersion;
import com.sonatype.demo.log4shell.ui.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ResultsStore {

    private static final Logger log= LoggerFactory.getLogger(ResultsStore.class);
   private final List<Result> results=new LinkedList<>();
   private final Map<Integer,Result> resultsByID =new HashMap<>();
   private final Map<JavaVersion, Console> byJavaVersion=new TreeMap<>();
   private final Map<String,SummaryRecord> summary=new TreeMap<>();
    private final Map<String,Console> specialistConsoles=new HashMap<>();
    private Configuration config=null;
    private ResultsByAttackResult grouping=new ResultsByAttackResult();
    private AtomicInteger resultCounter=new AtomicInteger(0);


   public ResultsStore(Configuration config) {
       this.config=config;
       for(String s:config.getSpecialistConsoleNames()) {
           specialistConsoles.put(s,new Console(s));
       }
   }
    public Console addResults(Result dc) {

        Console c=null;
        dc.setId( resultCounter.addAndGet(1));
        results.add(dc);
        resultsByID.put(dc.getId(),dc);
        System.out.println("run "+dc.getId()+" / "+config.totalRuns);

       if(!config.isSilentMode()) {
           // create a console representation
           c = byJavaVersion.get(dc.getJv());
           if (c == null) throw new NullPointerException("missing console");
           c.addResult(dc);

       }
            grouping.addResult(dc);

            String resultKey=dc.getPrimaryKey();
            SummaryRecord sr=summary.get(resultKey);
            if(sr==null) {
                sr=new SummaryRecord(dc);
                summary.put(resultKey,sr);
            }
            sr.record(dc);



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
        resultCounter.set(0);
        grouping=new ResultsByAttackResult();

        for(Console c:byJavaVersion.values()) {
            c.records.clear();
        }
        for (Console cc : specialistConsoles.values()) {
            cc.records.clear();
        }

    }

    public Result getEntry(int resultID) {
        return resultsByID.get(resultID);
    }

    public List<Result> getResults() {
        return results;
    }

    public List<Console> getConsoles() {
       List<Console> consoles=new LinkedList<>();
       config.getAllJavaVersions().forEach(cv -> { consoles.add(byJavaVersion.get(cv.getBase()));});
        return consoles;
    }

    public Console getConsole(JavaVersion jv) {
        return byJavaVersion.get(jv);
    }

    public Collection<SummaryRecord> getSummary() {
            return summary.values();
    }


    public Console getSpecialistConsole(String type) {
        return specialistConsoles.get(type);
    }


    public void addSpecialistConsole(String name) {
       specialistConsoles.put(name,new Console(name));
    }

    public Collection<Console> getSpecialistConsoles() {
       return specialistConsoles.values();
    }

    public Integer resultsCount() {
       return resultsByID.size();
    }

    public List<ResultsByAttackResult.Group> getGroupResults() {
       return grouping.summary();
    }
}
