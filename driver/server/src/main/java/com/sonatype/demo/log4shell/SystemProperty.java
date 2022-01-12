package com.sonatype.demo.log4shell;

import lombok.Data;

@Data
public class SystemProperty {

    public String name;
    public String value;
    public boolean active;

    public SystemProperty(String s, String value) {
        this.name=s;
        this.value=value;
        this.active=false;

    }
}
