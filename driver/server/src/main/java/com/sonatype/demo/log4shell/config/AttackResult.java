package com.sonatype.demo.log4shell.config;

import com.sonatype.demo.log4shell.ResultType;

public class AttackResult {


    public static final AttackResult FAILED =  new AttackResult(ResultType.FAIL);
    public static final AttackResult SUCCEDED =  new AttackResult(ResultType.SUCCESS);

    public  ResultType type;
      private   String data;
      private ComparisionType check;
    public enum ComparisionType { CONTAINS, NOT_CONTAINS };

        public AttackResult(ResultType rt, ComparisionType type, String data) {
            this.type=rt;
            this.data=data;
            this.check=type;
        }

        public AttackResult(ResultType rt) {
            this.type=rt;
        }

        public boolean match(String line) {

            System.out.println("line ["+line+"] check="+check+" d="+data+" c="+line.contains(data));

            switch(check) {
                case CONTAINS:
                    return line.contains(data);

                case NOT_CONTAINS:
                    return line.contains(data)==false;

                default: throw new RuntimeException("wtf");
            }

        }
}
