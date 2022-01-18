package com.sonatype.demo.log4shell;

import lombok.Data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Data
public class SummaryRecord {

    String jv;
    String lv;
    int[] score;

    public SummaryRecord(String jv, String lv) {
        this.jv=jv;
        this.lv=lv;
        score=new int[ResultType.values().length];

    }

    public String getResult(ResultType[] cols) {

            Map<String,Integer> results=new TreeMap<>();

            // sum the results by status

            for(ResultType rt:cols) {
                int value=score[rt.ordinal()];
                if(value>0) {
                    String result=rt.status();
                    if(results.containsKey(result)) {
                        value=value+results.get(result);
                    }
                    results.put(result,value);
                }

            }

            StringBuilder sb=new StringBuilder();

            append(sb,results,"danger");
            append(sb,results,"warning");
            append(sb,results,"success");
            append(sb,results,"body");

            return sb.toString();



    }

    private void append(StringBuilder sb, Map<String, Integer> results, String key) {

        if(results.containsKey(key)) {
            if(sb.length()>0) {
                sb.append(",");
            }
            sb.append(results.get(key).toString());
        }
    }

    public String getStatus(ResultType[] cols) {

        Set<String> results=new HashSet<>();

        // sum the results by status

        for(ResultType rt:cols) {
            int value=(score[rt.ordinal()]);
            if(value>0) {
                String result=rt.status();
                results.add(result);
            }
        }

        if(results.contains("danger")) return "danger";
        if(results.contains("warning")) return "warning";
        if(results.contains("success")) return "success";
        return "body";

    }
}
