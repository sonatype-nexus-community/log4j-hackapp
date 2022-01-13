package com.sonatype.demo.log4shell;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;
import spark.utils.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.jar.JarEntry;

import static spark.Spark.*;

public class FrontEnd {

    private static Logger log= LoggerFactory.getLogger(FrontEnd.class);
    private static Map<String,Console> consoles=new HashMap<>();
    private static String ldapaddr;
    // if running local mount the runner and log jar seperately
    public static final boolean inDockerContainer =(new File("/.dockerenv")).exists();

    private static Driver d;

    public static void main(String args[] ) throws Exception {

        log.info("server started");
        Console c=new Console("ldap");
        consoles.put("ldap",c);
        c=new Console("dns");
        consoles.put("dns",c);

        d=new Driver();

        exception(Exception.class, (e, req, res) -> e.printStackTrace());

        webSocket("/ws", WebSocketHandler.class);

        webSocketIdleTimeoutMillis(24*60*60*1000);
        staticFiles.location("/public");

        port(8080);

        before((req, res) -> {
                    log.info("request {}",req.pathInfo());
        });

      after((req, res) -> {
               res.header("Cache-Control","no-cache");
        });

        get("/views/versions",  (req, res) -> {

            Map<String, Object> model = new HashMap<>();
            model.put("versions",d.logVersions.values());
            log.info("version views mode {}",model);
            return renderTemplate("velocity/versions.vm",model);
        });

        get("/views/javalevels",  (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("levels",d.javaVersions.values());
            return renderTemplate("velocity/javalevels.vm",model);
        });


        get("/views/console/:id",  (req, res) -> {
            String consoleID=req.params("id");
            log.info("asking for console {}",consoleID);
            Console cq=null;
            JavaVersion jv=d.javaVersions.get(consoleID);
            if(jv!=null) {
                cq=d.rs.byJavaVersion.get(jv);
            } else {
                cq=consoles.get(consoleID);
            }
            Map<String, Object> model = new HashMap<>();
            model.put("c",cq);
            return renderTemplate("velocity/console.vm",model);
        });


        put("/version/:id/toggle", (req, res) -> {

            String versionID=req.params("id");
            d.toggleVersionStatus(versionID);

            return ""+d.logVersions.get(versionID).active;
        });

        put("/java/:id/toggle", (req, res) -> {

            String javaID=req.params("id");
            d.toggleJavaVersionStatus(javaID);
            return ""+d.javaVersions.get(javaID).active;
        });

        put("/vmproperty/:id/toggle", (req, res) -> {

            String property=req.params("id");
            d.togglePropertyStatus(property);
            return ""+d.vmProperties.get(property).active;
        });


        // Render main UI
        get("/", (req, res) -> renderIndex(req));


        // Render main UI
        get("/code/:id", (req, res) -> {

            String type=req.params("id");
            InputStream in=FrontEnd.class.getResourceAsStream("/ExternalObject.class");
            byte[] data=IOUtils.toByteArray(in);
            HttpServletResponse raw = res.raw();
            raw.getOutputStream().write(data);
            raw.getOutputStream().flush();
            raw.getOutputStream().close();
            return raw;


        });


        post("/clear", (req, res) -> {
            d.rs.clear();
            for(Console cc:consoles.values()) {
                cc.records.clear();
            }
            WebSocketHandler.handler.sendUpdate("consoles");
            return "";
        });

        post("/cancel", (req, res) -> {
            d.cancel();
            return "";
        });

        // Add new console entry to the console for 'type'
        post("/console/:type", (req, res) -> {
            String type=req.params("type");
            Console cr=consoles.get(type);
            if(cr!=null) {

                JsonElement je=JsonParser.parseString(req.body());
                if(je.isJsonObject()){
                    JsonObject jo= (JsonObject) je;
                    String message=jo.get("message").getAsString();
                    log.info("console {} log {}",type,message);
                    Record r=new Record();
                    r.version=null;
                    r.line=message;
                    cr.records.add(r);
                    if(message.startsWith("LDAP Listening")) {
                        triggerLdapServerConfig();
                    }
                    WebSocketHandler.handler.sendUpdate("console-"+type+"-main");
                }
            } else {
                log.info("no console for {}",type);
            }


            return "";
        });

        // log entry to process
        post("/log", (req, res) -> {

            JsonElement je=JsonParser.parseString(req.body());
            if(je.isJsonObject()){
                handleLogRequest((JsonObject)je,req.headers());
            } else {
                return "ERROR";
            }
            return "OK";
            
        });



    }

    private static void triggerLdapServerConfig() {

        log.info("loading objects into ldap server");
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    LdapServerUploader loader=new LdapServerUploader();
                    loader.addObjects();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        t.start();
    }


    private static void handleLogRequest(JsonObject request, Set<String> headers) {


        log.info("log this: {}",request.get("message"));
        d.drive(request.get("message").getAsString());
    }


    private static String renderIndex(Request req) {
        Map<String, Object> model = new HashMap<>();
        Collection<LogVersion> versions=d.logVersions.values();
        model.put("versions",versions);

        if(inDockerContainer) {
            model.put("ldapaddr","ldap.dev");
        } else {
            try {
                model.put("ldapaddr",InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                model.put("ldapaddr","localhost");

            }
        }
        model.put("properties",d.vmProperties.values());
        model.put("levels",d.javaVersions.values());
        model.put("servers",consoles.values());
        model.put("consoles",d.rs.byJavaVersion.values());

        return renderTemplate("velocity/index.vm", model);

    }

    private static String renderTemplate(String template, Map model) {
        return new VelocityTemplateEngine().render(new ModelAndView(model, template));
    }

}
