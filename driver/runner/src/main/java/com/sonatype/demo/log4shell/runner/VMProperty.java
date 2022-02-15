package com.sonatype.demo.log4shell.runner;

class VMProperty {
    String id;
    String name;
    String value;

    public String toVMValue() {
        return "-D" + name + "=" + value;

    }
    public String toString() {
        return ""+id+"["+name+"]"+value;
    }
}
