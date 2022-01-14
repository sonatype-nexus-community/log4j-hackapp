package com.sonatype.demo.log4shell;

import java.util.HashMap;
import java.util.Map;

public class Result {

    public DriverConfig config;
    public String[] lines;
    public boolean mutated;
    public Map<String,String> reportingProperties;

    public Result(DriverConfig dc) {
        this.config=dc;
        reportingProperties=new HashMap<>();
    }
}
