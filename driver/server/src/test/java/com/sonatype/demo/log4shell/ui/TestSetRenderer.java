package com.sonatype.demo.log4shell.ui;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestSetRenderer {


    @Test public void test1() {
        TreeSet<Integer> domain=new TreeSet<>();
        for(int i=1;i<20;i++) domain.add(i);

        TreeSet<Integer> candidates=new TreeSet<>();
        candidates.add(3);
        candidates.add(5);
        candidates.add(7);
        candidates.add(8);
        candidates.add(9);
        candidates.add(12);
        candidates.add(15);
        candidates.add(16);
        candidates.add(18);
        candidates.add(19);
        candidates.add(20);

        SetRenderer sr=new SetRenderer(domain);

        List<Object[]> data=sr.asRangeList(candidates);

        assertEquals(data.size(),6);

        Object[] o=data.get(0);
        assertEquals(3,(Integer)o[0]);
        assertEquals(1,o.length);

        o=data.get(2);
        assertEquals(7,(Integer)o[0]);
        assertEquals(9,(Integer)o[1]);


    }

    @Test public void test2() {
        TreeSet<Integer> domain=new TreeSet<>();
        for(int i=1;i<10;i++) domain.add(i);

        TreeSet<Integer> candidates=new TreeSet<>();
        candidates.add(3);
        candidates.add(5);
        candidates.add(7);
        candidates.add(8);
        candidates.add(9);
        candidates.add(12);

        SetRenderer sr=new SetRenderer(domain);
        List<Object[]> r=sr.asRangeList(candidates);

        String x=sr.asRangeListHtml(candidates);

        assertEquals("[3],[5],[7-12]",x);

    }
}
