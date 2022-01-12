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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Driver {

    private static final String suffix="-jar-with-dependencies.jar";

    private static Logger log= LoggerFactory.getLogger(FrontEnd.class);
    public Map<String, LogVersion> logVersions =new HashMap<>();
    public Map<String, JavaVersion> javaVersions =new HashMap<>();
    public Map<String, SystemProperty> vmProperties =new HashMap<>();
    private BlockingQueue<DriverConfig> queue=new LinkedBlockingQueue<>();
    public ResultsStore rs=new ResultsStore();
    private static boolean local=true;

    public Driver() throws IOException {
        loadJarPaths();
        loadJavaLevels();
        loadVMProperties();
        launcherRUnner();
    }

    private void loadVMProperties() {
        SystemProperty p=new SystemProperty("com.sun.jndi.ldap.object.trustURLCodebase","true");
        vmProperties.put(p.name,p);
        p=new SystemProperty("com.sun.jndi.ldap.object.trustSerialData","true");
        vmProperties.put(p.name,p);

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
                            Console c=rs.addResults(dc,new String[]{dc.msg});
                            WebSocketHandler.handler.sendUpdate("console-"+c.handle+"-main");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    private void driveJavaImages(DriverConfig dc) {
        List<String> dockerProcessConfig=createConfig(dc);
        log.info("driver setup: {}",String.join(" ",dockerProcessConfig));

        try {
            String[] results=execute(dockerProcessConfig);
            Console c=rs.addResults(dc,results);
            WebSocketHandler.handler.sendUpdate("console-"+c.handle+"-main");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadJavaLevels() throws IOException {
        File config=new File("javalevels.txt");
        List<String> lines=Files.readAllLines(config.toPath());
        for(String s:lines) {
            s=s.trim();
            JavaVersion jv=new JavaVersion(s);
            javaVersions.put(jv.version,jv);
            rs.addJavaVersion(jv);
        }


    }

 public  void drive(String logMsg) {
        log.info("drive log4j / jvm combinations");
        List<String> props=new LinkedList<>();
        for(SystemProperty sp:vmProperties.values()) {
            if(sp.active) {
                String v="-D"+sp.name+"="+sp.value;
                props.add(v);
            }
        }
        String[] vmargs=props.toArray(new String[0]);

        for(LogVersion lv:logVersions.values()) {
            if(lv.active) {
                for (JavaVersion jv : javaVersions.values()) {
                    if(jv.active){
                        DriverConfig dc=new DriverConfig(jv,lv,vmargs,logMsg);
                        queue.add(dc);

                    }

                }
            }
        }

    }

    /**

     * @param parameters
     * @return
     * @throws Exception
     */
    private String[] execute(List<String> parameters) throws Exception {


        File logger=new File("/tmp/log.log");
        logger.delete();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(parameters);
        processBuilder.redirectError(logger);
        processBuilder.redirectOutput(logger);
        Process process = processBuilder.start();
        boolean r=process.waitFor(3, TimeUnit.SECONDS);

        log.info("process timeout = {} ",r);

        List<String> data=Files.readAllLines(logger.toPath());

        log.info("resp {}",data);

        return data.toArray(new String[0]);
    }

    private List<String> createConfig(DriverConfig dc) {


        List<String> parameters=new LinkedList<>();
        parameters.add("docker");
        parameters.add("run");
        parameters.add("--rm");
        parameters.add("-e");
        parameters.add("MODE=production");
        parameters.add("-e");
        try {
            parameters.add("ADDR="+ InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        parameters.add("-t");

        // if running local mount the runner and log jar seperately
        if(local) {
            File pwd=new File(System.getProperty("user.dir"));
            parameters.add("-v");
            parameters.add(pwd.getAbsolutePath()+":/app");
        } else {
            parameters.add("--mount");
            parameters.add("source=log4shellexplorer_logjars,target=/app");
            parameters.add("--network");
            parameters.add("log4shellexplorer_default");
        }
        // add image name
        parameters.add(dc.jv.version);
        // setup java launcher
        parameters.add("java");
        if(dc.vmargs!=null) for(String v: dc.vmargs) parameters.add(v);

        String classpath="/app/runner/target/runner-1.0-SNAPSHOT.jar:/app/driver/log4jversions/"+dc.lv.version+"/target/"+dc.lv.version+"-jar-with-dependencies.jar";

        parameters.add("-cp");
        parameters.add(classpath);
        parameters.add(Runner.class.getCanonicalName());
        parameters.add(dc.msg);

        if(dc.props!=null) for(String p: dc.props) parameters.add(p);

        return parameters;
    }


    private void loadJarPaths() throws IOException {

        File local=new File(System.getProperty("user.dir"));

        log.info("searching for log jars in {}",local.getAbsolutePath());
        if(local.getAbsolutePath().equals("/")) {
            local = new File("/app");
            log.info("in root - switching to {}", local.getAbsolutePath());
        }
        else {
            local=new File(local,"driver");
            local=new File(local,"log4jversions");
            log.info("in dev mode - switching to {}", local.getAbsolutePath());
        }

        List<Path> candidates;
            Path path=local.toPath();
        try (Stream<Path> pathStream = Files.find(path,
                Integer.MAX_VALUE,
                (p, basicFileAttributes) ->
                        p.getFileName().toString().toLowerCase().endsWith(suffix))
        ) {
            candidates = pathStream.collect(Collectors.toList());
        }

        for(Path p:candidates) {
                p=p.toAbsolutePath();
                File f=p.toFile();
                log.info("eval {}",f);
                String name=f.getName();
                String location= f.getAbsolutePath();
                String version=name.substring(0,name.length()-suffix.length());
                LogVersion lv=new LogVersion();
                lv.active=true;
                lv.location=location;
                lv.version=version;
                logVersions.put(version,lv);

        }
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

    public void togglePropertyStatus(String key) {

        if(vmProperties.containsKey(key)) {
          SystemProperty p=vmProperties.get(key);
          p.active=!p.active;
            log.error("prop id {} switched to {} ",key,p.active);
        } else {
            log.error("prop id {} does not exist",key);
        }
    }
}
