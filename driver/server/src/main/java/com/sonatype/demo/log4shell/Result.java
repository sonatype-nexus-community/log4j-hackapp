package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shell.config.Attack;
import com.sonatype.demo.log4shell.config.JavaVersion;
import com.sonatype.demo.log4shell.config.LogVersion;
import lombok.Data;

import java.util.*;

@Data
public class Result {

     public List<Record> console;
     private Set<Integer> activeVMProperties=new HashSet<>();
     public boolean mutated;
     int id;
     public JavaVersion jv;
     public LogVersion lv;
     private Attack attack=null;
     public ResultType result;
     private String payload;
     public List<String> data=new LinkedList<>();
     public Map<String,Object> properties;
     public Set<String> vmprops;

     public String getKey() {
         String key=jv.version+"/"+ lv.getVersion();
         if(vmprops!=null && vmprops.isEmpty()==false) {
              for(String s:vmprops){
                   key=key+"/"+s;
              }
         }
         return key;
     }

     public Result(Attack a ) {
          setAttack(a);
     }

     public void setActiveVMProperties(Set<Integer> i) {
          if(i!=null) activeVMProperties.addAll(i);
     }
     public Integer[] getActiveVMProperties() {
          return activeVMProperties.toArray(new Integer[0]);
     }

     public void setAttack(Attack attack) {
          if(attack==null) throw new NullPointerException("Attack is null");
          this.attack=attack;
     }

     public Attack getAttack() {
          return attack;
     }

     public void setPayload(String s) {
          this.payload=s;
     }
     public String getPayload() {
          if(payload==null) return "";
          return payload;
     }
     public String getAttackName() {
          if(attack==null) return "";
          return attack.type.name();
     }
     public String reportedJava() {

          if(properties==null) return "unknown";
          Object o=properties.get("java.version");
          if(o==null) return "not set";
          return o.toString();
     }

     public String getConsoleData() {
          if(console==null || console.isEmpty()) return "";

               StringBuilder sb=new StringBuilder();
               for(Record r:console) {
                    if(sb.length()>0) {
                         sb.append("<br");
                    }
                    sb.append(r.line);
               }

               return new String(sb);
     }
}
