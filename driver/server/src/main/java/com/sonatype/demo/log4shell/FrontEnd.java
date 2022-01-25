package com.sonatype.demo.log4shell;



import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sonatype.demo.log4shell.config.Configuration;
import com.sonatype.demo.log4shell.ui.Console;
import com.sonatype.demo.log4shell.ui.HtmlRenderer;
import com.sonatype.demo.log4shelldemo.helpers.LdapServerUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.util.*;

import static spark.Spark.*;

public class FrontEnd {

    private static final Logger log= LoggerFactory.getLogger(FrontEnd.class);
    private static HtmlRenderer render;
    private static ResultsStore rs;

    private static Driver d;

    public static void main(String[] args ) throws Exception {


        File configfile=new File("config");
        Configuration config=Configuration.loadConfig(configfile);
        rs=new ResultsStore(config);

        log.info("server started");

        d=new Driver(config,rs);
        render=new HtmlRenderer(config,rs);

        exception(Exception.class, (e, req, res) -> e.printStackTrace());

        webSocket("/ws", WebSocketHandler.class);

        webSocketIdleTimeoutMillis(24*60*60*1000);
        staticFiles.location("/public");

        port(8080);

        before((req, res) -> log.info("request {}",req.pathInfo()));

      after((req, res) -> res.header("Cache-Control","no-cache"));

        get("/views/versions",  (req, res) -> render.renderVersions());

        get("/views/hints",  (req, res) -> render.renderHints());


        get("/views/results/raw/:id",  (req, res) -> render.renderRawResults(Integer.parseInt(req.params("id"))));

        get("/views/javalevels",  (req, res) -> render.renderJavaVersions());

        get("/views/gridtable",  (req, res) -> render.renderGridTable());


        get("/views/console/:id",  (req, res) -> {
            String consoleID=req.params("id");
            return render.renderConsole(consoleID);

        });


        put("/version/:id/toggle", (req, res) -> ""+ config.toggleVersion(Integer.parseInt(req.params("id"))));
        put("/attack/:id/toggle", (req, res) -> ""+ config.toggleAttack(Integer.parseInt(req.params("id"))));
        put("/java/:id/toggle", (req, res) -> ""+ config.toggleJavaVersion(Integer.parseInt(req.params("id"))));
        put("/vmproperty/:id/toggle", (req, res) -> ""+config.toggleProperty(Integer.parseInt(req.params("id"))));



        // Render main UI
        get("/", (req, res) -> render.renderIndex());

        // Render grid version
        get("/grid", (req, res) -> render.renderGrid(d));

        get("/summary", (req, res) -> render.renderSummary(d.rs));


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
            rs.clear();
            return "";
        });

        post("/gridtest", (req, res) -> {
            log.info("grid test initiated");
            rs.clear();
            d.runGridTest();
            return "";
        });


        post("/gridcancel", (req, res) -> {
            d.cancel();
            return "";
        });


        post("/cancel", (req, res) -> {
            d.cancel();
            return "";
        });

        // Add new console entry to the console for 'type'
        post("/console/:type", (req, res) -> {
            String type=req.params("type");
            Console cr=rs.getSpecialistConsole(type);
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

    private static void clearConsoles1() {

        rs.clear();
        WebSocketHandler.handler.sendUpdate("consoles");
    }

    private static void triggerLdapServerConfig() {

        log.info("loading objects into ldap server");
        Thread t=new Thread(() -> {
            try {
                Thread.sleep(1000);
                LdapServerUploader loader=new LdapServerUploader();
                loader.addObjects();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        t.start();
    }


    private static void handleLogRequest(JsonObject request, Set<String> headers) {


        log.info("log this: {}",request.get("message"));
        d.drive(request.get("message").getAsString());
    }


}
