package com.sonatype.demo.log4shell;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GridTestConfiguration {

    private JavaVersion jv;
    private List<LogVersion> logVersions;
    private List<SystemProperty> vmargs;
    private Set<String> reportingProperties=new HashSet<>();
    private List<Attack> attacks;

    public GridTestConfiguration(JavaVersion jv, List<LogVersion> active, List<SystemProperty> props, List<Attack> attacks) {
        this.jv=jv;
        this.logVersions=active;
        this.vmargs=props;
        this.attacks =attacks;

       if(this.logVersions==null || this.logVersions.isEmpty()) {
           throw new RuntimeException("no log versions specified");
       }


        if(this.attacks==null || this.attacks.isEmpty()) {
            throw new RuntimeException("no payloads specified");
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

    public List<Attack> getAttacks() {
        return attacks;
    }

    public JavaVersion getJavaVersion() {
        return jv;
    }
}
