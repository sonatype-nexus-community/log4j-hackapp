package com.sonatype.demo.log4shell.config;

import lombok.Data;

@Data
public class JavaVersion implements Comparable{

    public String version;
    public boolean active=false;

    public JavaVersion(String version) {
        this.version=version;
    }
    public String toString() {
        return version;
    }

    @Override
    public int compareTo(Object o) {

        if(o==null) return -1;
        if(o instanceof JavaVersion ==false) return -1;
        JavaVersion jo= (JavaVersion) o;
        if(jo==this) return 0;
       return Configuration.DockerImageNameComparitor.compareVersions(version,jo.version);

    }

}
