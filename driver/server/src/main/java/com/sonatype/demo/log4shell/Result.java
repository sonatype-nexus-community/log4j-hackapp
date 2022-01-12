package com.sonatype.demo.log4shell;

public class Result {

    public DriverConfig config;
    public String[] console;
    public boolean mutated;

    public Result(DriverConfig dc, String[] console) {
        this.config=dc;
        this.console=console;
    }
}
