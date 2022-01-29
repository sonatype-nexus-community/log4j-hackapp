package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shell.config.Attack;
import com.sonatype.demo.log4shell.config.AttackType;
import com.sonatype.demo.log4shell.config.Configuration;
import com.sonatype.demo.log4shell.config.LogVersion;
import com.sonatype.demo.log4shell.runner.GridRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.sonatype.demo.log4shell.runner.GridRunner.LOG4J_VM_PROPERTY;
import static com.sonatype.demo.log4shell.runner.Runner.*;

public class RawResultsConverter implements ResultsLineHandler {

    private static final Logger log= LoggerFactory.getLogger(RawResultsConverter.class);

    private Configuration.RunnerConfig c;
    private ResultHandler rh;

    // log version specific data
    private LogVersion lv=null;
    private Map<String,Object> systemPropertyValues=null;
    private Set<Integer> activeProperties;
    private List<String> data=null;
    private String payload=null;
    private Attack a=null;
    private Throwable error;


    /*
            Converts the lines of data returned by the process execution
            There are specific magic markers to indicate the seperation
            of each attack payload and each log4j version in use

         */

    public RawResultsConverter(Configuration.RunnerConfig c,ResultHandler rh) {
        this.c=c;
        this.rh=rh;
    }
    public static void convert(List<String> rawResults,  Configuration.RunnerConfig c  ,ResultHandler rh) {

        RawResultsConverter r = new RawResultsConverter(c,rh);
        r.converter(rawResults);
    }

    private void converter(List<String> rawResults) {

        log.info("input {}",rawResults.size());

        for(String line:rawResults) {
            handle(line);
        }

        reportOutstanding();




    }

    @Override
    public void handle(String line) {

        // new log version?

        int index= line.indexOf(GridRunner.LOG4J_VERSION_SEPERATOR);

        if(index>=0) {
            reportOutstanding();
            setLogVersion(line,index);
            return;
        }

        index= line.indexOf(SYSTEM_PROPERTY_VALUE);
        if(index>=0) {
            storePropertyValue(line,index);
            return;

        }

        index= line.indexOf(LOG4J_VM_PROPERTY);
        if(index>=0) {
            storeActiveVMProperty(line,index);
            return;

        }

        index= line.indexOf(PAYLOAD_RESULTS_SEPERATOR);

        if(index>=0) {
            reportOutstanding();
            storePayload(line,index);
            return;
        }

        index= line.indexOf(PAYLOAD_RESULTS_TERMINATOR);

        if(index>=0) {
            reportOutstanding();
            return;
        }

        if(data==null) System.out.println("data::"+line);
        if(data!=null) data.add(line);
    }

    private void storeActiveVMProperty(String line, int index) {
        String suffix = line.substring(index + 1+(LOG4J_VM_PROPERTY.length()));
        int space=suffix.indexOf(" ");
        int propID=Integer.parseInt(suffix.substring(0,space));
        activeProperties.add(propID);
    }

    @Override
    public void error(Throwable t) {
        if(data!=null) data.add(t.getMessage());
        this.error=t;
        reportOutstanding();
    }


    private void storePayload(String line, int index) {


        // new payload - until the next group or payload encountered all lines
        // are for this payload
        // report any outstanding
        data=new LinkedList<>();
        String suffix = line.substring(index + 1+(PAYLOAD_RESULTS_SEPERATOR.length()));
        int space=suffix.indexOf(" ");
        int attackID=Integer.parseInt(suffix.substring(0,space));
        payload=suffix.substring(space+1);

        a=c.getAttack(attackID);
        if(attackID==0) { // adhoc
            a = Attack.buildAttack(AttackType.ADHOC, payload);
        }
        log.info("found new payload {} {} {}",attackID,a.type.name(),payload);
        error=null;
    }

    private void storePropertyValue(String line,int index) {


        // the result of reporting a Java property
        // this is top level log info
        // so shouldn't show up in a single attack response


        String l=line.substring(index + 1 +(SYSTEM_PROPERTY_VALUE.length()));
        index=l.indexOf("!!");
        String key=l.substring(0,index);
        String suffix=l.substring(index+2);
        char t=suffix.charAt(0);
        log.info("found reported property type:{} key:{}",t,key);
        switch(t) {
            case '?' : //missing
                systemPropertyValues.put(key,null);
                break;
            case '=' : // got a value
                systemPropertyValues.put(key,suffix.substring(1));
                break;
        }

    }

    private void reportOutstanding() {
        // report any outstanding

        if(data!=null) {
            Result r=new Result(a);
            r.data=data;
            r.setError(error);
            r.setJv(c.getJavaVersion());
            r.setActiveVMProperties(activeProperties);
            r.setLv(lv);
            r.setPayload(payload);
            r.properties=systemPropertyValues;
            rh.handle(r);
        }
        data=null;
    }

    private void setLogVersion(String line, int index) {


        // new group of results for the specified log version
        String logVersion=line.substring(index+1+(GridRunner.LOG4J_VERSION_SEPERATOR.length()));
        lv=c.getLogVersion(logVersion);
        if(lv==null) throw new RuntimeException("unknown log version "+logVersion);
        systemPropertyValues=new HashMap<>();
        activeProperties=new HashSet<>();
        error=null;
        log.info("found log4j record {}",lv.getVersion());
    }




}
