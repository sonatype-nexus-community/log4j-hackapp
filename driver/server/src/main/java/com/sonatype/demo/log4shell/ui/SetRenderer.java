package com.sonatype.demo.log4shell.ui;

import java.util.*;

/**
 * Given an ordered set of all possile entries
 * render a second, ordered subset as
 *  a,b,c,d-g,h,k-m,n,o etc
 */
public class SetRenderer {

    public static final List<Object[]> ALL= Collections.unmodifiableList(new LinkedList<>());
    public static final List<Object[]> NONE= Collections.unmodifiableList(new LinkedList<>());

    Object[] domain;
    TreeSet all=new TreeSet();
    public SetRenderer(Set fullSet) {
        this.all.addAll(fullSet);
        domain=this.all.toArray();
    }

    public String asRangeListHtml(Set candidates) {
        TreeSet c=new TreeSet();
        c.addAll(candidates);
        System.out.println("in==>"+c);
        List<Object[]> list=asRangeList(c);

        if(list==ALL) return "all";
        if(list==NONE || list.isEmpty()) return "none";

        StringBuilder sb=new StringBuilder();
        for(Object[] o:list) {
            if(sb.length()>0) {
                sb.append(",");
            }
            if(o.length==1) {
                sb.append("["+o[0]+"]");
            } else {
                sb.append("["+o[0]+"-"+o[1]+"]");
            }
        }
        return sb.toString();
    }

    public List<Object[]> asRangeList(Set c) {


        if (c.containsAll(all)) return ALL;
        if (c.isEmpty()) return NONE;

        List<Object[]> results=new LinkedList<>();

        Object[] candidates = c.toArray();

        int ci=0;
        int di=0;
        int start=-1;
        int end=-1;

        while(true) {

            if(start<0) {
                if(domain[di]==candidates[ci]) {
                    start=ci;
                    end=start;
                } else {
                   if(di<domain.length) {
                       di++;
                   } else {
                       // no more domains.
                       while(ci<candidates.length) {
                           results.add(new Object[]{candidates[ci]});
                           ci++;
                       }
                       break;
                   }

                }
            } else {
                // got a start.  how long is the chain
                ci++;
                di++;
                if(ci<candidates.length==false) {
                   // run out of candidates..

                    end=ci-1;
                    if(start==end) {
                        results.add(new Object[]{candidates[start]});
                    } else {
                        results.add(new Object[]{candidates[start],candidates[end]});
                    }
                    break;
                }
                if(di<domain.length==false) {
                    // run out of domains ..

                    end=ci;
                    if(start==end) {
                        results.add(new Object[]{candidates[start]});
                    } else {
                        results.add(new Object[]{candidates[start],candidates[end]});
                    }
                    break;
                }
                if(domain[di]==candidates[ci]) {
                    end=ci;
                } else {
                    // go back one
                 //   ci--;
                   // di--;
                    if(start==end) {
                        results.add(new Object[]{candidates[start]});
                    } else {
                        results.add(new Object[]{candidates[start],candidates[end]});
                    }
                    start=-1;
                    end=-1;

                }
            }

        }

      return results;

    }

}
