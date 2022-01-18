package com.sonatype.demo.log4shell;

import java.util.List;

public class ResultTypeChecker {

    //private static final Logger log= LoggerFactory.getLogger(ResultsParser.class);

    private static final String[] markers=new String[]{

            "Reference Class Name:",   // partial RCE
            "cn=rce", // failed RCE
            "XXX", // sucessful RCE

            "thank you for your data", // sucessful hidden attack
            ":1389/a}", // failed hidden attack

            "gadget-chain",  // sucessfull gadget chain
            "cannot be cast to java.base/java.lang.String", // partially sucessful gadget chain
            "cannot be cast to java.lang.String", // partially sucessful gadget chain
            "cannot be cast to class java.lang.String", // partially sucessful gadget chain
            "cn=gadget", // failed gadget chain

            "${log4j:",  //  failed getting log4h config
            "log4j2.xml", // sucessful log config

            "sent us ${sys:java.version}" , // partial transmiy
            " sent us ", // sucessful transmit of java version
            "cn=version", // failed transmit java version

            "${sys:java.version}",   // failed to get java version

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
            ResultType.PARTIAL_GADGET_CHAIN,
            ResultType.FAILED_GADGET_CHAIN,
            ResultType.FAILED_GADGET_CHAIN,

            ResultType.FAILED_LOG_LOG4JCONFIG,
            ResultType.SUCCESSFUL_LOG_LOG4JCONFIG,

            ResultType.PARTIAL_TRANSMIT_JAVA_VERSION,
            ResultType.SUCCESSFUL_TRANSMIT_JAVA_VERSION,
            ResultType.FAILED_TRANSMIT_JAVA_VERSION,


            ResultType.FAILED_LOG_JAVA_VERSION,

            ResultType.FAILED_LOG_ENVVAR,
            ResultType.SUCCESSFUL_LOG_ENVVAR,

            ResultType.SUCCESSFUL_LOG_JAVA_CLASSPATH,
            ResultType.FAILED_LOG_JAVA_CLASSPATH

    };

    public static ResultType getResponseType(String message,List<String> resultLines) {
         return getResponseType(message,resultLines.toArray(new String[0]));
    }
   public static ResultType getResponseType(String message,String[] resultLines) {

         ResultType rt;

            // cannot be cast to java.base/java.lang.String

            if(resultLines.length>1) {
                // more tha one line means some sort of failure
                // any clues?
                rt=checkResults(resultLines);
                if(rt==null) rt=ResultType.ERROR;
            } else {
                String resultLine=resultLines[0].trim();
                rt=checkResults(resultLine);
                if(rt==null) {
                    if(message.trim().equals(resultLine)) {
                        rt=ResultType.UNCHANGED;
                    } else{
                        rt = ResultType.UNKNOWN;
                    }
                }

            }
            if(rt==ResultType.UNKNOWN) {
                // check the message for signs of intent
                if(message.contains("${sys:java.version}")) return ResultType.SUCCESSFUL_LOG_JAVA_VERSION;
                if(message.contains("${sys:java.class.path}")) return ResultType.SUCCESSFUL_LOG_JAVA_CLASSPATH;
            }

         return rt;






    }

    private static ResultType checkResults(String[] resultLine) {

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
