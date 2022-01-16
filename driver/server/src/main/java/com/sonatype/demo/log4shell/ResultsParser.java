package com.sonatype.demo.log4shell;

import com.sonatype.demo.Runner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ResultsParser {

    //private static final Logger log= LoggerFactory.getLogger(ResultsParser.class);

    private static final String[] markers=new String[]{

            "Reference Class Name:",   // partial RCE
            "cn=rce", // failed RCE
            "XXX", // sucessful RCE

            "thank you for your data", // sucessful hidden attack
            ":1389/a}", // failed hidden attack

            "gadget-chain",  // sucessfull gadget chain
            "cannot be cast to java.base/java.lang.String", // partially sucessful gadget chain
            "cn=gadget", // failed gadget chain

            "${log4j:",  //  failed getting log4h config
            "log4j2.xml", // sucessful log config

            "${sys:java.version",   // failed to get java version

            "${env:",   // failed get envvar
            "production",  // sucessfull get env

            "driver/runner/runner.jar", // sucessful java.classpath
            "${sys:java.class.path"   // failed get java property

    };
    private static final ResultType[] markertypes=new ResultType[]{
            ResultType.PARTIAL_RCE,
            ResultType.FAILED_RCE,
            ResultType.SUCCESSFUL_RCE,

            ResultType.SUCCESSFUL_HIDDEN_ATTACK,
            ResultType.FAILED_HIDDEN_ATTACK,

            ResultType.SUCCESSFUL_GADGET_CHAIN,
            ResultType.PARTIAL_GADGET_CHAIN,
            ResultType.FAILED_GADGET_CHAIN,

            ResultType.FAILED_DIRECT_LOG4JCONFIG,
            ResultType.SUCESSFUL_DIRECT_LOG4JCONFIG,

            ResultType.FAILED_JAVA_VERSION,

            ResultType.FAILED_DIRECT_ENVVAR,
            ResultType.SUCESSFUL_DIRECT_ENVVAR,

            ResultType.SUCCESSFUL_DIRECT_JAVA_CLASSPATH,
            ResultType.FAILED_DIRECT_JAVA_CLASSPATH

    };

    public static List<Result> parse(DriverConfig dc, String[] lines) {

        List<Result> results=new LinkedList<>();
        Map<String,String> props=new HashMap<>();

       // read any lines until first seperator and process if property values

        int linePos=0;
        for(;linePos<lines.length;linePos++) {
            String line=lines[linePos];
            if(line.contains(Runner.MSG_START_LINE)) break;
            if(line.startsWith("!!=")) {
                // property value reported
                String s=line.substring(3);
                int sep=s.indexOf("/");
                String propName=s.substring(0,sep);
                String value=s.substring(sep+1);
                props.put(propName,value);

            }
        }
        int msgIndex=0;
        while(linePos<lines.length) {
            linePos++;
            List<String> resultLines=new LinkedList<>();
            for(;linePos<lines.length;linePos++) {
                String line=lines[linePos];
                if(line.contains(Runner.MSG_START_LINE)) break;
                resultLines.add(line);
            }

            String message=dc.msgs[msgIndex].trim();

         ResultType rt;

            // cannot be cast to java.base/java.lang.String

            if(resultLines.size()>1) {
                // more tha one line means some sort of failure
                // any clues?
                rt=checkResults(resultLines);
                if(rt==null) rt=ResultType.ERROR;
            } else {
                String resultLine=resultLines.get(0).trim();
                rt=checkResults(resultLine);
                if(rt==null) {
                    // could be a failed java.version check
                    if(message.contains("${sys:java.version") && !resultLine.contains("${sys:java.version")) rt=ResultType.SUCESSFUL_JAVA_VERSION;
                    else if(message.equals(resultLine)) {
                        rt=ResultType.UNCHANGED;
                    } else{
                        rt = ResultType.UNKNOWN;
                    }
                }

            }

            Result r=new Result(rt,message,resultLines,dc.jv,dc.lv,dc.vmargs,props);
            results.add(r);
           msgIndex++;
        }


         //   if(line.startsWith("WARNING: sun.reflect.Reflection.getCallerClass is not supported")) continue;


        return results;
    }

    private static ResultType checkResults(List<String> resultLine) {

       for(String s:resultLine) {
           ResultType rt=checkResults(s.trim());
           if(rt!=null) return rt;
       }
       return null;
    }


    private static ResultType checkResults(String resultLine) {

        for(int i=0;i<markers.length;i++) {
            if(resultLine.contains(markers[i])) return markertypes[i];
        }
        return null;
    }

}
