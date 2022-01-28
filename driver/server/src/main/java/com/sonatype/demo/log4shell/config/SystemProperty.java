package com.sonatype.demo.log4shell.config;

import lombok.Data;

@Data
public class SystemProperty  {

    public String name;
    public String value;


    public SystemProperty(String s, String value) {
        this.name=s.trim();
        this.value=value;

    }

    public String toString() { return name; }
    public String toVMString() {
        return "-D"+name+"="+value;
    }

}
