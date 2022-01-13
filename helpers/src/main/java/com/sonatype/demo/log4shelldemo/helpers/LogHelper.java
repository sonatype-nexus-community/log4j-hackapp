package com.sonatype.demo.log4shelldemo.helpers;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import java.io.File;

public class LogHelper {

    public static final boolean inDockerContainer =(new File("/.dockerenv")).exists();

    public static void configLogging(String route) {
        ConfigurationBuilder<BuiltConfiguration> builder
                = ConfigurationBuilderFactory.newConfigurationBuilder();

        AppenderComponentBuilder console
                = builder.newAppender("stdout", "Console");
        builder.add(console);

        LayoutComponentBuilder standard
                = builder.newLayout("PatternLayout");
        standard.addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable");
        console.add(standard);

        LayoutComponentBuilder json
                = builder.newLayout("JsonLayout");
        json.addAttribute("properties","true");
        AppenderComponentBuilder web
                = builder.newAppender("web", "http");

        String server="http://localhost:8080/"+route;
        if(inDockerContainer) server="http://server.dev:8080/"+route;

        web.addAttribute("url", server);
        web.add(json);
        builder.add(web);

        RootLoggerComponentBuilder rootLogger
                = builder.newRootLogger(Level.INFO);
        rootLogger.add(builder.newAppenderRef("stdout"));
        rootLogger.add(builder.newAppenderRef("web"));

        builder.add(rootLogger);

        Configurator.initialize(builder.build());

    }
}
