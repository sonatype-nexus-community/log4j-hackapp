package com.sonatype.demo.log4shell;



import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;

import static spark.Spark.*;

public class FrontEnd {

    private static final Logger log= LoggerFactory.getLogger(FrontEnd.class);
    private static HtmlRenderer render;

    private static Driver d;

    public static void main(String args[] ) throws Exception {


        log.info("server started");
        d=new Driver();



        render=new HtmlRenderer(d);

        exception(Exception.class, (e, req, res) -> e.printStackTrace());

        webSocket("/ws", WebSocketHandler.class);

        webSocketIdleTimeoutMillis(24*60*60*1000);
        staticFiles.location("/public");

        port(8080);

        before((req, res) -> log.info("request {}",req.pathInfo()));

      after((req, res) -> res.header("Cache-Control","no-cache"));

        get("/views/versions",  (req, res) -> render.renderVersions());

        get("/views/hints",  (req, res) -> {
                 return render.renderHints();
        });


        get("/views/results/raw/:id",  (req, res) -> {

            return render.renderRawResults(Integer.parseInt(req.params("id")));

        });

        get("/views/javalevels",  (req, res) -> {
           return render.renderJavaVersions();
        });


        get("/views/console/:id",  (req, res) -> {
            String consoleID=req.params("id");
            return render.renderConsole(consoleID);

        });


        put("/version/:id/toggle", (req, res) -> ""+ d.toggleVersionStatus(req.params("id")));

        put("/java/:id/toggle", (req, res) -> {

           return ""+ d.toggleJavaVersionStatus(req.params("id"));

        });

        put("/vmproperty/:id/toggle", (req, res) -> {

            String property=req.params("id");
            return ""+d.togglePropertyStatus(property);

        });

        put("/hint/:id/toggle", (req, res) -> {

            String hintID=req.params("id");
            Integer i=Integer.parseInt(hintID);
            return ""+d.toggleHintStatus(i);

        });


        // Render main UI
        get("/", (req, res) -> render.renderIndex());

        // Render grid version
        get("/grid", (req, res) -> render.renderGrid());

        get("/summary", (req, res) -> render.renderSummary());


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
            clearConsoles();
            return "";
        });

        post("/gridtest", (req, res) -> {
            log.info("grid test initiated");
            clearConsoles();
            WebSocketHandler.handler.mute();
            d.runGridTest();
            return "";
        });


        post("/gridcancel", (req, res) -> {
            d.cancel();
            WebSocketHandler.handler.unmute();
            return "";
        });


        post("/cancel", (req, res) -> {
            d.cancel();
            return "";
        });

        // Add new console entry to the console for 'type'
        post("/console/:type", (req, res) -> {
            String type=req.params("type");
            Console cr=d.getSpecialistConsole(type);
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

    private static void clearConsoles() {
        d.rs.clear();
        for (Console cc : d.getSpecialistConsoles()) {
            cc.records.clear();
        }
        WebSocketHandler.handler.sendUpdate("consoles");
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


}
