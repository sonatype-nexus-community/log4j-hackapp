package com.sonatype.demo.log4shell;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Console {

    public String name;
    public String handle;
    public List<String> records=new LinkedList<>();

    public Console(String name) {
        this.name=name;
        this.handle=name.replace(":","_").toLowerCase();
    }

}
