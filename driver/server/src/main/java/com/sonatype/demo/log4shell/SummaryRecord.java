package com.sonatype.demo.log4shell;

import lombok.Data;

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
}
