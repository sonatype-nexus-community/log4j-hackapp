package com.sonatype.demo.log4shell.config;
import java.util.*;

public class ConfigMap<T> {

    private final Map<Integer, ConfigElement<T>> entries=new TreeMap<>();

    public ConfigElement<T> addEntry(T t) {
        ConfigElement<T> at=new ConfigElement<>(t,entries.size()+1);
        entries.put(at.getID(),at);
        return at;
    }

    public List<T> getActive() {

        List<T> props=new LinkedList<>();
        for(ConfigElement<T> sp:entries.values()) {
            if(sp.isActive()) {
                props.add(sp.getBase());
            }
        }
        return props;
    }

    public Collection<ConfigElement<T>> values() {
        return entries.values();
    }

    public boolean toggle(int id) {
        ConfigElement<T> t=entries.get(id);
        if(t==null) return false;
        return t.toggle();

    }

    public T get(Integer i) {
        return entries.get(i).getBase();
    }


    public Set<Integer> getActiveIDs() {

        Set<Integer> results=new HashSet<>();
        for(ConfigElement<T> sp:entries.values()) {
            if(sp.isActive()) {
               results.add(sp.getID());
            }
        }
        return results;

    }


    public  List<ConfigElement<T>>  getActiveElements() {

        List<ConfigElement<T>> props=new LinkedList<>();
        for(ConfigElement<T> sp:entries.values()) {
            if(sp.isActive()) {
                props.add(sp);
            }
        }
        return props;
    }

    public boolean isActive(int id) {

        ConfigElement<T> e=entries.get(id);
        if(e==null) return false;
        return e.isActive();

    }
}
