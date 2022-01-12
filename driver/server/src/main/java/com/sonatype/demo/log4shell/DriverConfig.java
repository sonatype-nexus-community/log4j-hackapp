package com.sonatype.demo.log4shell;

import lombok.extern.java.Log;

public class DriverConfig {

    JavaVersion jv;
    LogVersion lv;
    String[] vmargs;
    String[] props;
    String msg;


    public DriverConfig(JavaVersion jv, LogVersion lv,String[] vmprops,String msg) {
        this.jv=jv;
        this.lv=lv;
        this.msg=msg;
        this.vmargs=vmprops;
    }
}
