package com.sonatype.demo.log4shell;

public interface ResultsLineHandler {

    public void handle(String line);

    void error(Throwable t);
}
