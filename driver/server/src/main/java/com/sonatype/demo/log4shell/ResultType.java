package com.sonatype.demo.log4shell;

import java.util.Map;
import java.util.TreeMap;

public enum ResultType {

    UNKNOWN,
    ERROR,
    PARTIAL,
    SUCCESS,
    FAIL;


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
