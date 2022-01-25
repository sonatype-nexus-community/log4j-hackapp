package com.sonatype.demo.log4shell.config;

import lombok.Data;

public class ConfigElement<T>{

    private boolean active;
   private int id;
    private T base;

    public ConfigElement(T t, int id) {
        this.base = t;
        this.id=id;
        active = false;
    }

    public boolean toggle() {
        active = !active;
        return active;
    }

    public boolean isActive() {
        return active;
    }

    public T getBase() {
        return base;
    }
    public String label() {
        return base.toString();
    }

    public int getID() {
        return id;
    }
}
