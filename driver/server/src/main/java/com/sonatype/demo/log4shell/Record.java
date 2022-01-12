package com.sonatype.demo.log4shell;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Record {

    public String version;
    public String line;
    public Integer[] propids =new Integer[0];
    public boolean mutated;
}
