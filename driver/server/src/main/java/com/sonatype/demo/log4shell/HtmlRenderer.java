package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shelldemo.helpers.DockerEnvironment;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static com.sonatype.demo.log4shell.ResultType.*;

public class HtmlRenderer {

    public static final String LDAPADDR = "ldapaddr";
    private final Driver d;
    static Map<String,ResultType[]> summaryGroups=summaryHeadings();
    static List<ResultType> summaryColumns=genSummaryColumns(summaryGroups);


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
        model.put("raw",d.rs.getEntry(resultID).getResults());
        return renderTemplate("velocity/raw.vm",model);
    }


    public  String renderSummary() {

        Map<String, Object> model = new HashMap<>();
        model.put("types",ResultType.values());
        model.put("summary",d.rs.getSummary());
        model.put("groups",summaryGroups);
        model.put("columns",summaryColumns);
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


    private static Map<String,ResultType[]> summaryHeadings() {

        Map<String,ResultType[]> r=new LinkedHashMap<>();
        r.put("general",new ResultType[]{UNKNOWN,ERROR,UNCHANGED});
        r.put("prevent force log of envar",new ResultType[]{FAILED_LOG_ENVVAR, SUCCESSFUL_LOG_ENVVAR});
        r.put("prevent force log Java version",new ResultType[]{FAILED_LOG_JAVA_VERSION, SUCCESSFUL_LOG_ENVVAR});
        r.put("prevent force log of Java classpath",new ResultType[]{FAILED_LOG_JAVA_CLASSPATH, SUCCESSFUL_LOG_JAVA_CLASSPATH});
        r.put("prevent force log of log4j config",new ResultType[]{FAILED_LOG_LOG4JCONFIG, SUCCESSFUL_LOG_LOG4JCONFIG});
        r.put("prevent transmit Java version",new ResultType[]{FAILED_TRANSMIT_JAVA_VERSION,PARTIAL_TRANSMIT_JAVA_VERSION,SUCCESSFUL_TRANSMIT_JAVA_VERSION});
        r.put("prevent gadget chain attack",new ResultType[]{FAILED_GADGET_CHAIN,PARTIAL_GADGET_CHAIN,SUCCESSFUL_GADGET_CHAIN});
        r.put("prevent RCE attack",new ResultType[]{FAILED_RCE,PARTIAL_RCE,SUCCESSFUL_RCE});
        r.put("prevent hidden attack",new ResultType[]{FAILED_HIDDEN_ATTACK,FAILED_HIDDEN_ATTACK});

        return r;


    }


    private static List<ResultType> genSummaryColumns(Map<String, ResultType[]> summaryGroups) {
        List<ResultType> results=new LinkedList<>();
        for(ResultType[] r:summaryGroups.values()) {
            for(ResultType rt:r) {
                results.add(rt);
            }
        }
        return results;
    }

}
