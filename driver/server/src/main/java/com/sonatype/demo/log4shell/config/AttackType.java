package com.sonatype.demo.log4shell.config;

public enum AttackType {

    ADHOC,
    EXPOSE_JAVA_VERSION,
    EXPOSE_JAVA_CLASSPATH,
    EXPOSE_LOG4J_CONFIG,
    EXPOSE_ENVVAR,
    TRANSMIT_JAVA_VERSION,
    GADGET_CHAIN,
    RCE,
    HIDDEN_ATTACK

    }

