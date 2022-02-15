package com.sonatype.demo.log4shell.runner;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ComboBuilder {


    public static void main(String[] args) {
        List<VMProperty> in=new LinkedList<>();
        for(int i=0;i<10;i++) {
            Set<Set<VMProperty>> combos=buildCombos(in);
            System.out.println("i="+i+" s="+combos.size()+" // "+Math.pow(2,i));
            in.add(new VMProperty());
        }


    }
  public static Set<Set<VMProperty>> buildCombos(  List<VMProperty> in) {


        Set<Set<VMProperty>> results=new HashSet<>();

        results.add(new HashSet<>());

        for(int i=0;i<in.size();i++) {

            Set<VMProperty>[] sets=results.toArray(new Set[0]);

            for(Set e:sets) {
                HashSet s = new HashSet();
                s.addAll(e);
                s.add(in.get(i));
                results.add(s);
            }
        }


      return results;
    }
}
