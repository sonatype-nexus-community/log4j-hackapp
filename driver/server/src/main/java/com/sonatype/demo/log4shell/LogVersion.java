package com.sonatype.demo.log4shell;

import lombok.Data;

@Data
public class LogVersion {

    public String version;
    public String location;
    public boolean active=true;

    public String toString() {
        return version+"::"+location;
    }
}
