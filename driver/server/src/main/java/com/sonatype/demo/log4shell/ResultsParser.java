package com.sonatype.demo.log4shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class ResultsParser {

    private static Logger log= LoggerFactory.getLogger(ResultsParser.class);

    public static Result parse(DriverConfig dc, List<String> lines) {


        Result r=new Result(dc);

        List<String> results=new LinkedList<>();

        for(String line:lines) {
            System.out.println("[["+line);
            line=line.trim();
            if(line.startsWith("WARNING: sun.reflect.Reflection.getCallerClass is not supported")) continue;
            if(line.startsWith("!!=")) {
                // property value reported
                String s=line.substring(3);
                int sep=s.indexOf("/");
                String propName=s.substring(0,sep);
                String value=s.substring(sep+1);
                r.reportingProperties.put(propName,value);
                continue;
            }
            results.add("?"+line);

        }

        r.lines=results.toArray(new String[0]);

        return r;
    }
}
