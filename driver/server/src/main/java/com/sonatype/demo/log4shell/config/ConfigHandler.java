package com.sonatype.demo.log4shell.config;

import com.sonatype.demo.log4shell.config.Configuration;

public interface ConfigHandler {

    public void handle(Configuration.RunnerConfig c);
}
