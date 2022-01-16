package com.sonatype.demo.log4shell;

import lombok.Data;


import java.util.List;
import java.util.Map;

@Data
public class Result {

    ResultType type;
    List<SystemProperty> env;
    JavaVersion jv;
    LogVersion  lv;
    String logMsg;
    String[] rawLines;

    public int id=-1;
    public boolean mutated;
    public Map<String,String> reportingProperties;
    public List<Record> console;

    public Result(ResultType type,String msg, List<String> resultLines, JavaVersion jv, LogVersion lv,List<SystemProperty> envprops,Map<String,String> foundProps) {
        this.logMsg=msg;
        this.rawLines=resultLines.toArray(new String[0]);
        this.jv=jv;
        this.lv=lv;
        this.env=envprops;
        this.reportingProperties=foundProps;
        if(type==null) type=ResultType.UNKNOWN;
        this.type=type;

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

    public String[] getLines() {
        return rawLines;
    }
}
