package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shell.config.AttackType;
import lombok.Data;

import java.util.*;

@Data
public class SummaryRecord {

    String jv;
    String lv;
    Map<String,Count>scores=new HashMap<>();

    public SummaryRecord(Result r) {
        this.jv=r.jv.version;
        this.lv=r.lv.getVersion();

    }

    public String getResultClass(AttackType t,ResultType rt) {
        int c=getResult(t,rt);
        if(c==0) return "bg-light";

        switch( rt ) {
            case SUCCESS:   return "bg-danger";
            case PARTIAL:   return "bg-warning";
            case FAIL:      return "bg-success";
            case ERROR:     return "bg-secondary";

            default: return "bg-info";

        }

    }
    public int getResult(AttackType t,ResultType rt) {
        String key=t.name()+"/"+rt.name();
        if(scores.containsKey(key)==false) return 0;
        return scores.get(key).count;
    }

    public void record(Result dc) {

        AttackType t=dc.getAttack().type;
        ResultType rt=dc.result;
        String key=t.name()+"/"+rt.name();
        Count c=scores.get(key);
        if(c==null) {
            c=new Count();
            scores.put(key,c);
        } else {
            throw new RuntimeException("dup key?"+key);
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
