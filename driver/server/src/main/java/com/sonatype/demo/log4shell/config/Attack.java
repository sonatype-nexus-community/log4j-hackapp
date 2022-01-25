package com.sonatype.demo.log4shell.config;

import com.sonatype.demo.log4shell.ResultType;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Attack  {

   public AttackType type;
   private int evalMode=0;
   public String payload;
   private List<AttackResult> checks=new LinkedList<>();


    private Attack(AttackType type,String payload) {
        this.type=type;
        this.payload=payload;

    }

    // default attack type
    // fails if the payload is still in the output
    public static Attack buildSimpleMutatedAttack(AttackType type,String payload) {
       Attack a=new Attack(type,payload);
        a.addCheck(new AttackResult(ResultType.FAIL,AttackResult.ComparisionType.CONTAINS,payload));
        a.addCheck(AttackResult.FAILED );
        return a;
    }

    public static Attack buildAttack(AttackType t, String s) {
        return new Attack(t,s);
    }

    public void addCheck(AttackResult attackResult) {
        checks.add(attackResult);
    }


    public ResultType evaluate(List<String> data) {

        if (data == null || data.size() == 0) return ResultType.ERROR;

        if(data.size()>1) {
            ResultType rt= evalList(data);
            if(rt==ResultType.SUCCESS) rt=ResultType.PARTIAL;
            return rt;
        }

        String line=data.get(0);

        return evalLine(line);

    }

    private ResultType evalLine(String line) {

        for(AttackResult ar:checks) {


                if(ar==AttackResult.FAILED) return ar.type;
                if(ar==AttackResult.SUCCEDED) return ar.type;

                if(ar.match(line)) return ar.type;
        }

        return ResultType.UNKNOWN;
    }

    private ResultType evalList(List<String> data) {


       // a list means something went wrong
            for(String line:data) {
                for(AttackResult ar:checks) {
                    if(ar.match(line)) {
                        return ar.type;
                    }
                }
            }


        return ResultType.UNKNOWN;
    }

    public String toString() {
       return type.name();
    }

    private boolean contains(String[] a, String d) {
        if(a!=null && a.length>0) {
            for(String s:a) {
                if(d.contains(s)) return true;
            }
        }
        return false;
    }


}
