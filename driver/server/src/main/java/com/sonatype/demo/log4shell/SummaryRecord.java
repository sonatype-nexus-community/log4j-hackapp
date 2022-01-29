package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shell.config.AttackType;
import lombok.Data;

import java.util.*;

@Data
public class SummaryRecord {

    String jv;
    String lv;
    String active;
    Map<String,Count>scores=new HashMap<>();

    public SummaryRecord(Result r) {
        this.jv=r.getJavaVersionName();
        this.lv=r.getLogVersionName();
        this.active=r.activePropsLabels();

    }

    public int getResult(AttackType t,ResultType r) {
        String cellKey= t.name()+"/"+r.name();
        if(scores.containsKey(cellKey)) return scores.get(cellKey).count;
        return 0;
    }

    public void record(Result dc) {


        String cellKey= dc.getAttack().type.name()+"/"+dc.getResultName();
        Count c=scores.get(cellKey);
        if(c==null) {
            c = new Count();
            scores.put(cellKey, c);
        }
        c.count++;


    }

    @Data
    static class Count {
        private int count=0;

        public int getCount() {
            return count;
        }
    }
}
