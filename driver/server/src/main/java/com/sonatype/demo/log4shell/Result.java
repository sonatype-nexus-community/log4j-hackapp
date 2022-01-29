package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shell.config.Attack;
import com.sonatype.demo.log4shell.config.JavaVersion;
import com.sonatype.demo.log4shell.config.LogVersion;
import lombok.Data;

import java.util.*;

@Data
public class Result {

     private List<Record> console;
     private Throwable error;
     private Set<Integer> activeVMProperties=new HashSet<>();
     private boolean mutated;
     private int id;
     private JavaVersion jv;
     private LogVersion lv;
     private Attack attack=null;
     private ResultType result;
     private String payload;
     public List<String> data=new LinkedList<>();
     public Map<String,Object> properties;
     public Set<String> vmprops;

     public String getPrimaryKey() {

         String key=jv.version
                 +"/"
                 +lv.getVersion();

         if(activeVMProperties!=null) {
              for(Integer s:activeVMProperties){
                   key=key+"/"+s;
              }
         }
         return key;
     }

     public String activePropsLabels() {
          StringBuilder sb=new StringBuilder();
          if(activeVMProperties!=null) {
               for(int p:activeVMProperties) {
                    sb.append("<span class=\"badge rounded-pill bg-info\">"+p+"</span>");
               }
          }
          return new String(sb);
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

	public List<Record> getConsole() {
		return console;
	}

	public void setConsole(List<Record> console) {
		this.console = console;
	}

	public Throwable getError() {
		return error;
	}

	public void setError(Throwable error) {
		this.error = error;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public JavaVersion getJv() {
		return jv;
	}

	public void setJv(JavaVersion jv) {
		this.jv = jv;
	}

	public LogVersion getLv() {
		return lv;
	}

	public void setLv(LogVersion lv) {
		this.lv = lv;
	}

	public ResultType getResult() {
		return result;
	}

	public void setResult(ResultType result) {
		this.result = result;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Set<String> getVmprops() {
		return vmprops;
	}

	public void setVmprops(Set<String> vmprops) {
		this.vmprops = vmprops;
	}

	public String getJavaVersionName() {
		return jv.version;
	}

	public String getLogVersionName() {
		return lv.getVersion();
	}

	public String getResultName() {
		return result.name();
	}

	public void setMutated() {
		mutated=true;
		
	}

}
