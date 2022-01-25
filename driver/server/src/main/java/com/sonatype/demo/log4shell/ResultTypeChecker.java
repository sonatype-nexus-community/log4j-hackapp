package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shell.config.Attack;
import com.sonatype.demo.log4shell.config.AttackType;
import com.sonatype.demo.log4shell.config.ConfigElement;
import com.sonatype.demo.log4shell.config.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultTypeChecker {

    private Configuration.RunnerConfig c;
    public ResultTypeChecker(Configuration.RunnerConfig c) {
        this.c=c;
    }

    public  void setResult1(Result dc) {

        if(dc==null) throw new NullPointerException("result is null");
        if(dc.getAttack()==null) throw new NullPointerException("result attack is nulll");
            if(dc.getAttack().type==AttackType.ADHOC) {
            for (ConfigElement<Attack> ce : c.getAllAttacks()) {
                Attack a = ce.getBase();
                ResultType rt = a.evaluate(dc.data);
                if (rt != ResultType.UNKNOWN) {
                    dc.result = rt;
                    dc.setAttack(a);
                    return;
                }
            }
            dc.result=ResultType.UNKNOWN;
            return;
        }

        dc.result=dc.getAttack().evaluate(dc.data);

    }

}
