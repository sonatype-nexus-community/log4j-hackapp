package com.sonatype.demo.log4shell;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Result {

    public DriverConfig config;
    public int id=-1;
    private String[] lines;
    public boolean mutated;
    public Map<String,String> reportingProperties;
    public List<Record> console;


    public Result(DriverConfig dc,String lines[]) {
        this.config=dc;
        this.lines=lines;
        reportingProperties=new HashMap<>();
    }


    public Result(DriverConfig dc,Map<String,String> props,String lines[]) {
        this(dc,lines);
        if(props!=null) this.reportingProperties=props;

    }

    public String reportedJava() {
        return reportingProperties.getOrDefault("java.version","n/a");
    }

    public String getConsole() {
        StringBuilder sb=new StringBuilder();
        if(console!=null) {
            for(Record r: console) {
                sb.append(r.line);
                sb.append("<br>");
            }
        }
        return new String(sb);
    }
}
