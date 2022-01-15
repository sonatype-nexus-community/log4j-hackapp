package com.sonatype.demo.log4shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ResultsParser {

    private static Logger log= LoggerFactory.getLogger(ResultsParser.class);

    public static Result parse(DriverConfig dc, List<String> lines) {




        List<String> results=new LinkedList<>();
        Map<String,String> props=new HashMap<>();
        for(String line:lines) {
            line=line.trim();
            if(line.startsWith("WARNING: sun.reflect.Reflection.getCallerClass is not supported")) continue;
            if(line.startsWith("!!=")) {
                // property value reported
                String s=line.substring(3);
                int sep=s.indexOf("/");
                String propName=s.substring(0,sep);
                String value=s.substring(sep+1);
                props.put(propName,value);
                continue;
            }
            results.add(line);

        }

        return new Result(dc,props,results.toArray(new String[0]));
    }
}
