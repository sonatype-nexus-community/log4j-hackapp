package com.sonatype.demo.log4shell;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaVersionTestConfig {

    private JavaVersion jv;
    private List<LogVersion> logVersions;
    private List<SystemProperty> vmargs;
    private Set<String> reportingProperties=new HashSet<>();
    private List<String> msgs;

    public JavaVersionTestConfig(JavaVersion jv, List<LogVersion> active, List<SystemProperty> props, List<String> logMsgs) {
        this.jv=jv;
        this.logVersions=active;
        this.vmargs=props;
        this.msgs=logMsgs;

       if(this.logVersions==null || this.logVersions.isEmpty()) {
           throw new RuntimeException("no log versions specified");
       }
    }


    public Set<String> getReportingProperties() {
        return reportingProperties;
    }

    public String getImageName() {
        return jv.version;
    }

    public List<SystemProperty> getVMProperties() {
        return vmargs;
    }

    public List<LogVersion> getLogVersions() {
        return logVersions;
    }

    public Collection<String> getMessages() {
        return msgs;
    }

    public JavaVersion getJavaVersion() {
        return jv;
    }
}
