package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shelldemo.helpers.DockerEnvironment;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HtmlRenderer {

    public static final String LDAPADDR = "ldapaddr";
    private final Driver d;
    public HtmlRenderer(Driver d) {
        this.d=d;
    }
    public  String renderIndex() {

        Map<String, Object> model = new HashMap<>();
        Collection<LogVersion> versions=d.getLogVersions();
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
        model.put("properties",d.getVMProperties());
        model.put("levels",d.getJavaVersions());
        model.put("servers",d.getSpecialistConsoles());
        model.put("hints",d.getHints());
        model.put("consoles",d.rs.getConsoles());

        return renderTemplate("velocity/index.vm", model);

    }


    public String renderRawResults(int resultID) {

        Map<String, Object> model = new HashMap<>();
        model.put("raw",d.rs.getEntry(resultID).getLines());
        return renderTemplate("velocity/raw.vm",model);
    }


    public  String renderSummary() {

        Map<String, Object> model = new HashMap<>();
        model.put("types",ResultType.values());
        model.put("summary",d.rs.getSummary());
        return renderTemplate("velocity/summary.vm",model);
    }

    public  String renderGrid() {
        Map<String, Object> model = new HashMap<>();
        Collection<LogVersion> versions=d.getLogVersions();
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
        model.put("properties",d.getVMProperties());
        model.put("levels",d.getJavaVersions());
        model.put("servers",d.getSpecialistConsoles());
        model.put("hints",d.getHints());
        model.put("queuesize",d.queueSize());
        model.put("results",d.rs.getResults());


        return renderTemplate("velocity/grid.vm", model);

    }
    private String renderTemplate(String template, Map model) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, template));
    }

    public String renderVersions() {

        Map<String, Object> model = new HashMap<>();
        model.put("versions",d.getLogVersions());
        return renderTemplate("velocity/versions.vm",model);

    }

    public String renderHints() {
        Map<String, Object> model = new HashMap<>();
        model.put("hints",d.getHints());
        return renderTemplate("velocity/hints.vm",model);
    }

    public String renderJavaVersions() {
        Map<String, Object> model = new HashMap<>();
        model.put("levels",d.getJavaVersions());
        return renderTemplate("velocity/javalevels.vm",model);
    }

    public String renderConsole(String consoleID) {
        Console cq;
        JavaVersion jv=d.getJavaVersion(consoleID);
        if(jv!=null) {
            cq=d.rs.getConsole(jv);
        } else {
            cq=d.getSpecialistConsole(consoleID);
        }
        Map<String, Object> model = new HashMap<>();
        model.put("c",cq);
        return renderTemplate("velocity/console.vm",model);
    }
}
