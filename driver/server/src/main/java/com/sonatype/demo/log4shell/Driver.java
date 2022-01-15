package com.sonatype.demo.log4shell;

import com.sonatype.demo.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Driver {

    private static final String fatjars ="-jar-with-dependencies.jar";
    public static final String ANYJAR = ".jar";


    private static Logger log= LoggerFactory.getLogger(FrontEnd.class);

    private String runnerPath;

    public Map<String, LogVersion> logVersions =new HashMap<>();
    public Map<String, JavaVersion> javaVersions =new HashMap<>();
    public Map<String, SystemProperty> vmProperties =new HashMap<>();
    private Set<String> localImages=null;
    public Map<Integer,Hint> hints=new HashMap<>();
    private BlockingQueue<DriverConfig> queue=new LinkedBlockingQueue<>();

    public ResultsStore rs=new ResultsStore(this);


    public Driver() throws Exception {


        localImages=DockerProcessRunner.getLocalDockerImages();
        File config=new File("config");
        log.info(config.getAbsolutePath()+" == "+config.exists());
        loadJarPaths();
        loadJavaLevels(config);
        loadHints(config);
        loadVMProperties();
        launcherRUnner();
    }

    private void loadVMProperties() {
        SystemProperty p=new SystemProperty("com.sun.jndi.ldap.object.trustURLCodebase","true");
        vmProperties.put(p.name,p);
        p.id=vmProperties.size();
        p=new SystemProperty("com.sun.jndi.ldap.object.trustSerialData","true");
        vmProperties.put(p.name,p);
        p.id=vmProperties.size();
    }

    private void launcherRUnner() {

        Thread t=new Thread(new Runnable() {

            @Override
            public void run() {
                while(true) {
                    try {
                        DriverConfig dc= queue.take();

                        if(dc.msg.startsWith(":::")) {
                            dc.msg=dc.msg.substring(3);
                            driveJavaImages(dc);
                        } else {
                            Console c=rs.addResults(dc,dc.msg);
                            WebSocketHandler.handler.sendUpdate("console-"+c.handle+"-main");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(queue.isEmpty()) {
                        WebSocketHandler.handler.unmute();
                    }
                }
            }
        });
        t.start();
    }

    private void driveJavaImages(DriverConfig dc) {


        try {
            DockerProcessRunner r=new DockerProcessRunner(runnerPath);
            List<String> results=r.runLogger(dc);
            Console c=rs.addResults(dc,results);
            WebSocketHandler.handler.sendUpdate("console-"+c.handle+"-main");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadJavaLevels(File c) throws IOException {

        File config=new File(c,"javalevels.txt");
        List<String> lines=Files.readAllLines(config.toPath());
        boolean oneReady=false;
        for(String s:lines) {
            s=s.trim();
            JavaVersion jv=new JavaVersion(s);
            boolean present=localImages.contains(s);
            if(present) {
                if (javaVersions.isEmpty()) {
                    jv.active = true;
                    oneReady = true;
                }
                javaVersions.put(jv.version, jv);
                rs.addJavaVersion(jv);
            } else{
                log.warn("Specified Java Image {} is not present in local cache ",s);
            }
        }


    }



    private void loadHints(File c) throws IOException {

        File config=new File(c,"hints.txt");
        List<String> lines=Files.readAllLines(config.toPath());
        for(String s:lines) {
            s=s.trim();
            Hint h=new Hint();
            h.hint=s;
            h.id=hints.size()+1;
            hints.put(h.id,h);
        }
        log.info("loaded {} hints",hints.size());


    }
 public  int drive(String logMsg) {

        int scheduled=0;

        log.info("drive log4j / jvm combinations");
        List<SystemProperty> props=new LinkedList<>();
        for(SystemProperty sp:vmProperties.values()) {
            if(sp.active) {
                props.add(sp);
            }
        }

        for(LogVersion lv:logVersions.values()) {
            if(lv.active) {
                for (JavaVersion jv : javaVersions.values()) {
                    if(jv.active){
                        DriverConfig dc=new DriverConfig(jv,lv,props,logMsg);
                        scheduled++;
                        queue.add(dc);

                    }

                }
            }
        }
        return scheduled;

    }





    private void loadJarPaths() throws IOException {

        File current=new File(System.getProperty("user.dir"));

        log.info("searching for log jars in {}",current.getAbsolutePath());
        File driver=new File(current,"driver");

        File log4jversions=new File(driver,"log4jversions");
        List<Path> candidates = getCandidates(log4jversions, fatjars);
        registerLog4JJars(current,candidates);


        for(File k:driver.listFiles()) {
            log.info("child {}",k.getAbsolutePath()) ;
        }
        File runner=new File(driver,"runner");
        candidates = getCandidates(runner, ANYJAR);
        Path r=candidates.get(0);
        log.info("candidate runner path {}",r);
        runnerPath=relLoc(current,r.toFile());
        log.info("runner path {}",runnerPath);

    }

    private String relLoc(File first,File second) {
        String a=first.getAbsolutePath().toString();
        String b=second.getAbsolutePath().toString();
        String rel=b.substring(a.length());
        if(rel.startsWith("/")) rel=rel.substring(1);
        return rel;
    }
    private void registerLog4JJars(File current,List<Path> candidates) {

        for(Path p:candidates) {
            p=p.toAbsolutePath();
            File f=p.toFile();
            String name=f.getName();
            String version=name.substring(0,name.length()- fatjars.length());
            LogVersion lv=new LogVersion();
            if(logVersions.isEmpty()) lv.active=true;  // activate the first one only
            lv.location=relLoc(current,f);
            lv.version=version;
            logVersions.put(version,lv);

            log.info("log4j version {} = jar {}",version,lv.location);

        }
    }

    private List<Path> getCandidates(File local,String suffix) throws IOException {
        List<Path> candidates;
        Path path= local.toPath();
        try (Stream<Path> pathStream = Files.find(path,
                Integer.MAX_VALUE,
                (p, basicFileAttributes) ->
                        p.getFileName().toString().toLowerCase().endsWith(suffix))
        ) {
            candidates = pathStream.collect(Collectors.toList());
        }
        return candidates;
    }

    public Set<String> getVersions() {
        return logVersions.keySet();
    }

    public void toggleVersionStatus(String versionID) {

        if(logVersions.containsKey(versionID)) {
            LogVersion lv=logVersions.get(versionID);
            lv.active=!lv.active;
            log.error("version id {} switched to {} ",versionID,lv.active);
        } else {
            log.error("version id {} does not exist",versionID);
        }
    }

    public void toggleJavaVersionStatus(String javaID) {

        if(javaVersions.containsKey(javaID)) {
           JavaVersion jv=javaVersions.get(javaID);
            jv.active=!jv.active;
            log.error("version id {} switched to {} ",javaID,jv.active);
        } else {
            log.error("version id {} does not exist",javaID);
        }
    }


    public void toggleHintStatus(int hintID) {
        if(hints.containsKey(hintID)) {
            Hint h=hints.get(hintID);
            h.active=!h.active;
        }
    }
    public void togglePropertyStatus(String key) {

        if(vmProperties.containsKey(key)) {
          SystemProperty p=vmProperties.get(key);
          p.active=!p.active;
            log.error("prop id {} switched to {} ",key,p.active);
        } else {
            log.error("prop id {} does not exist",key);
        }
    }

    public void cancel() {

        queue.clear();

    }

    /*
    Grid Test runs all active log / java / hint combos
    The application is not involved;
     */


    public void runGridTest() {

        log.info("run grid test");
        int scheduled=0;

        for(Hint h:hints.values()) {
            if(h.active) {
                log.info("hint scheduled {}",h.hint);
              scheduled+=drive(":::"+h.hint);
            }
        }

       log.info("Scheduled {} grid tests",scheduled);
    }

    public int queueSize() {
        return queue.size();
    }

}
