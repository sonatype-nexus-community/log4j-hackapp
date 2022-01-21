package com.sonatype.demo.log4shell;

import lombok.Data;

@Data
public class Attack {

    public int id;
    public boolean active;
    public String type;
    public String payload;
   public String successIndicators[];
   public String failureIndicators[];
    public String partialIndicators[];

   // default attack type
    // fails if the payload is still in the output
   public Attack(String type,String payload) {
       this.type=type;
       this.payload=payload;
       this.failureIndicators=new String[]{payload};
   }

   // simple pass uf sucess string seen
    public Attack(String type,String payload,String success) {
        this.type=type;
        this.payload=payload;
       this.successIndicators=new String[]{success};
    }
    // sucess if string seen.  Partial sucess if partial seen
    public Attack(String type,String payload,String success,String partial) {
        this.type=type;
        this.payload=payload;
        this.failureIndicators=new String[]{payload};
        this.successIndicators=new String[]{success};
        this.partialIndicators=new String[]{partial};
    }

    public Attack(String type,String payload,String success,String partial[]) {
        this.type=type;
        this.payload=payload;
        this.failureIndicators=new String[]{payload};
        this.successIndicators=new String[]{success};
        this.partialIndicators=partial;
    }


}
