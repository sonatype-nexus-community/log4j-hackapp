package com.sonatype.demo.log4shell;

import java.util.Map;
import java.util.TreeMap;

public enum ResultType {

    UNKNOWN,
    ERROR,
    UNCHANGED,

    FAILED_LOG_ENVVAR,
    SUCCESSFUL_LOG_ENVVAR,

    FAILED_RCE,
    SUCCESSFUL_RCE,
    PARTIAL_RCE,

    SUCCESSFUL_GADGET_CHAIN,
    FAILED_GADGET_CHAIN,
    PARTIAL_GADGET_CHAIN,

    FAILED_LOG_JAVA_VERSION,
    SUCCESSFUL_LOG_JAVA_VERSION,

    FAILED_TRANSMIT_JAVA_VERSION,
    PARTIAL_TRANSMIT_JAVA_VERSION,
    SUCCESSFUL_TRANSMIT_JAVA_VERSION,

    FAILED_LOG_LOG4JCONFIG,
    SUCCESSFUL_LOG_LOG4JCONFIG,

    SUCCESSFUL_LOG_JAVA_CLASSPATH,
    FAILED_LOG_JAVA_CLASSPATH,

    SUCCESSFUL_HIDDEN_ATTACK,
    FAILED_HIDDEN_ATTACK;



    public String columnName() {
        return this.name().replace("_"," ").toLowerCase();
    }

    public String status() {
        String[] bits=columnName().split(" ");
        if(bits.length==1) return "light";

        String fix= bits[0].trim().toLowerCase();
        switch(fix) {
            case "successful" : return "danger";
            case "partial"   : return "warning";
            case "failed"   : return "success";
        }
        return "body";

    }
}
