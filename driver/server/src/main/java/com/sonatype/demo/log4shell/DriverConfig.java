package com.sonatype.demo.log4shell;

import lombok.extern.java.Log;

import java.util.LinkedList;
import java.util.List;

public class DriverConfig {

    JavaVersion jv;
    LogVersion lv;
    List<SystemProperty> vmargs;
    String[] props;
    String msg;


    public DriverConfig(JavaVersion jv, LogVersion lv,List<SystemProperty> vmprops,String msg) {
        this.jv=jv;
        this.lv=lv;
        this.msg=msg;
        this.vmargs=vmprops;
    }

    public Integer[] getActivePropertyIDs() {
        List<Integer> li=new LinkedList<>();

        for(SystemProperty s:vmargs) {
            li.add(s.id);
        }
        return li.toArray(new Integer[0]);
    }
}
