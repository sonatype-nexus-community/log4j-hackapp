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

    public List<Record> toRecords(TestResult dc) {

        List<Record> results=new LinkedList<>();
        List<String> lines=dc.getResults();
        if(lines!=null) {

            if(lines.size()==1) {
                Record rec=new Record();
                rec.version=dc.lv.getVersion();
                rec.propids =dc.getActivePropertyIDs();

                String l=lines.get(0).trim();
                if(l.equals(dc.getLogMsg().trim())) {
                    rec.line=l;
                }
                else {
                    dc.mutated=true;
                    LinkedList<DiffMatchPatch.Diff> diff = dmp.diffMain(dc.getLogMsg(), l,false);
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
                dc.mutated=true;
                for (String l : lines) {
                    Record rec = new Record();
                    rec.version = dc.lv.getVersion();
                    rec.line = "<span class=\"text-danger\">" + l + "</span>";
                    results.add(rec);
                }
            }
        }

        return results;

    }
    public void addResult(TestResult dc) {

        List<Record> results=toRecords(dc);
        records.addAll(results);
         dc.console=results;

    }
}
