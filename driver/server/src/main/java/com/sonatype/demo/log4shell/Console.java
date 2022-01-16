package com.sonatype.demo.log4shell;

import lombok.Data;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.util.LinkedList;
import java.util.List;

@Data
public class Console {

    public String name;
    public String handle;
    public List<Record> records=new LinkedList<>();
    private DiffMatchPatch dmp = new DiffMatchPatch();

    public Console(String name) {
        this.name=name;
        this.handle=name.replace(":","_")
                .replace(".","_")
                .replace("-","_")
                .toLowerCase();
    }

    public List<Record> toRecords(DriverConfig dc,Result r) {

        List<Record> results=new LinkedList<>();
        String lines[]=r.getLines();
        if(lines!=null) {

            if(lines.length==1) {
                Record rec=new Record();
                rec.version=dc.lv.version;
                rec.propids =dc.getActivePropertyIDs();

                String l=lines[0];
                if(l.equals(r.logMsg)) {
                    rec.line=l;
                }
                else {
                    r.mutated=true;
                    LinkedList<DiffMatchPatch.Diff> diff = dmp.diffMain(r.logMsg, l,false);
                    StringBuilder sb=new StringBuilder();
                    for(DiffMatchPatch.Diff d:diff) {
                        switch(d.operation) {
                            case EQUAL: sb.append(d.text); break;
                            case INSERT: sb.append("<span class=\"text-danger\">"+d.text+"</span>"); break;
                            //   case DELETE: sb.append("??"+d.text+"??"); break;

                        }
                    }
                    rec.line=sb.toString();
                }

               results.add(rec);
            }
            else {
                r.mutated=true;
                for (String l : lines) {
                    Record rec = new Record();
                    rec.version = dc.lv.version;
                    rec.line = "<span class=\"text-danger\">" + l + "</span>";
                    results.add(rec);
                }
            }
        }

        return results;

    }
    public void addResult(DriverConfig dc,Result r) {

        List<Record> results=toRecords(dc,r);
        records.addAll(results);
        r.console=results;

    }
}
