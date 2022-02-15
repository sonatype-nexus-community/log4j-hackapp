package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shell.config.*;
import lombok.Data;

import java.util.*;

public class ResultsByAttackResult {

    private final Map<String,Group> results=new TreeMap<>();
    private final Set<JavaVersion> jvs=new HashSet<>();
    private final Set<LogVersion>  lvs=new HashSet<>();
    private final Set<Set<Integer>> props=new HashSet<>();


    public  void addResult(Result dc) {

        AttackType type=dc.getAttack().type;
        ResultType rt=dc.getResult();
        String key=type.name()+"/"+rt.name();

        synchronized (results) {

            Group g=results.get(key);
            if(g==null) {
                g=new Group();
                g.rt=rt;
                g.at=type;
                results.put(key,g);
            }
            jvs.add(dc.getJv());
            lvs.add(dc.getLv());

            g.jvs.add(dc.getJv());
            g.lvs.add(dc.getLv());
            Set<Integer> si=toSet(dc.getActiveVMProperties());
            g.props.add(si);
            props.add(si);

        }

    }

    private Set<Integer> toSet(Integer[] ints) {
        Set<Integer> i=new HashSet<>();
        if(ints!=null) {
            for (int x = 0; x < ints.length; x++) {
                i.add(ints[x]);
            }
        }
        return i;
    }

    public   List<Group>  summary() {

        List<Group> summary=new LinkedList<>();
        for(Group g:results.values()) {
            g.allJVMS=g.jvs.containsAll(jvs);
            g.allLVS=g.lvs.containsAll(lvs);
            g.allProps=g.props.containsAll(props);
            summary.add(g);
        }

        return summary;
    }


    @Data
   public static class Group {

       public boolean allJVMS=false;
       public boolean allLVS=false;
       public boolean allProps=false;
       public AttackType at;
       public ResultType rt;
       public Set<JavaVersion> jvs=new HashSet<>();
       public Set<LogVersion>  lvs=new HashSet<>();
       public Set<Set<Integer>> props=new HashSet<>();


    }
}

