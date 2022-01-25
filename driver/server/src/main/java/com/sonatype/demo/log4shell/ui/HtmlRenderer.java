package com.sonatype.demo.log4shell.ui;

import com.sonatype.demo.log4shell.Driver;
import com.sonatype.demo.log4shell.Result;
import com.sonatype.demo.log4shell.ResultType;
import com.sonatype.demo.log4shell.ResultsStore;
import com.sonatype.demo.log4shell.config.*;
import com.sonatype.demo.log4shelldemo.helpers.DockerEnvironment;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


public class HtmlRenderer {

    public static final String LDAPADDR = "ldapaddr";
    private final Configuration config;
    private ResultsStore rs;


    public HtmlRenderer(Configuration c,ResultsStore rs) {
        this.config=c;
        this.rs=rs;

    }
    public  String renderIndex() {

        Map<String, Object> model = new HashMap<>();
        Collection<ConfigElement<LogVersion>> versions=config.getAllLogVersions();
        model.put("versions",versions);

        if(DockerEnvironment.inDockerContainer) {
            model.put(LDAPADDR,"ldap.dev");
        } else {
            try {
                model.put(LDAPADDR, InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                model.put(LDAPADDR,"localhost");

            }
        }
        model.put("properties",config.getAllVMProperties());
        model.put("levels",config.getAllJavaVersions());
        model.put("servers",config.getSpecialistConsoleNames());
        model.put("hints",config.getHints());
        model.put("consoles",rs.getConsoles());
        model.put("special",rs.getSpecialistConsoles());

        return renderTemplate("velocity/index.vm", model);

    }


    public String renderRawResults( int resultID) {

        Map<String, Object> model = new HashMap<>();
        Result r=rs.getEntry(resultID);
        if(r==null) return "no results available ("+rs.resultsCount()+")";
        model.put("raw",r.data);
        return renderTemplate("velocity/raw.vm",model);
    }


    public  String renderSummary(ResultsStore rs) {

        Map<String, Object> model = new HashMap<>();
        model.put("types",ResultType.values());
        model.put("summary",rs.getSummary());
        model.put("attacks", AttackType.values());
        model.put("results", ResultType.values());

        return renderTemplate("velocity/summary.vm",model);
    }

    public  String renderGrid(Driver d) {
        Map<String, Object> model = new HashMap<>();
        Collection<ConfigElement<LogVersion>> versions=config.getAllLogVersions();
        model.put("versions",versions);

        if(DockerEnvironment.inDockerContainer) {
            model.put(LDAPADDR,"ldap.dev");
        } else {
            try {
                model.put(LDAPADDR,InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                model.put(LDAPADDR,"localhost");

            }
        }
        model.put("properties",config.getAllVMProperties());
        model.put("levels",config.getAllJavaVersions());
        model.put("servers",config.getSpecialistConsoleNames());
        model.put("attacks",config.getAllAttacks());
        model.put("queuesize",d.queueSize());
        model.put("results",d.rs.getResults());


        return renderTemplate("velocity/grid.vm", model);

    }
    private String renderTemplate(String template, Map model) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, template));
    }

    public String renderVersions() {

        Map<String, Object> model = new HashMap<>();
        model.put("versions",config.getAllLogVersions());
        return renderTemplate("velocity/versions.vm",model);

    }

    public String renderHints() {
        Map<String, Object> model = new HashMap<>();
        model.put("hints",config.getHints());
        return renderTemplate("velocity/hints.vm",model);
    }

    public String renderJavaVersions() {
        Map<String, Object> model = new HashMap<>();
        model.put("levels",config.getAllJavaVersions());
        return renderTemplate("velocity/javalevels.vm",model);
    }

    public String renderConsole(String consoleID) {
        Console cq;
        JavaVersion jv=config.getJavaVersion(consoleID);
        if(jv!=null) {
            cq=rs.getConsole(jv);
        } else {
            cq=rs.getSpecialistConsole(consoleID);
        }
        Map<String, Object> model = new HashMap<>();
        model.put("c",cq);
        return renderTemplate("velocity/console.vm",model);
    }


    public  String renderGridTable() {
        Map<String, Object> model = new HashMap<>();
        model.put("results",rs.getResults());
          return renderTemplate("velocity/gridtable.vm", model);

    }

}
