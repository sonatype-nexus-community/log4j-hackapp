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

    private static final String fatjars ="-jar-with-dependencies.jar";
    public static final String ANYJAR = ".jar";

    private static Logger log= LoggerFactory.getLogger(FrontEnd.class);

    private String runnerPath;

    public Map<String, LogVersion> logVersions =new HashMap<>();
    public Map<String, JavaVersion> javaVersions =new HashMap<>();
    public Map<String, SystemProperty> vmProperties =new HashMap<>();
    private Set<String> localImages=null;
    private BlockingQueue<DriverConfig> queue=new LinkedBlockingQueue<>();

    public ResultsStore rs=new ResultsStore(this);


    public Driver() throws Exception {


        localImages=getLocalDockerImages();

        loadJarPaths();
        loadJavaLevels();
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
        boolean oneReady=false;
        for(String s:lines) {
            s=s.trim();
            JavaVersion jv=new JavaVersion(s);
            jv.present=localImages.contains(s);
            if(jv.present && javaVersions.isEmpty()) {
                jv.active=true;
                oneReady=true;
            }
            javaVersions.put(jv.version,jv);
            rs.addJavaVersion(jv);
        }


    }

 public  void drive(String logMsg) {
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


        List<String> data = runProcess(parameters);

        Iterator<String> is=data.iterator();
        while(is.hasNext()) {
            String line=is.next();
            if(line.startsWith("WARNING: sun.reflect.Reflection.getCallerClass is not supported")) {
                is.remove();
            }
        }

        log.info("resp {}",data);

        return data.toArray(new String[0]);
    }

    private List<String> runProcess(List<String> parameters) throws IOException, InterruptedException {
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
        return data;
    }

    private Set<String> getLocalDockerImages() throws Exception {

        List<String> parameters=new LinkedList<>();
        parameters.add("docker");
        parameters.add("image");
        parameters.add("ls");
        parameters.add("--format");
        parameters.add("{{.Repository}}:{{.Tag}}");

        List<String> rawList=runProcess(parameters);

        Set<String> images=new HashSet<>();
        images.addAll(rawList);

        log.info("loaded local image list {}",images);
        return images;

    }

    private List<String> createConfig(DriverConfig dc) {


        List<String> parameters=new LinkedList<>();
        parameters.add("docker");
        parameters.add("run");
        parameters.add("--pull");
        parameters.add("never");
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



        if(FrontEnd.inDockerContainer) {
            parameters.add("--mount");
            parameters.add("source=log4shelldemo_logjars,target=/driver");
            parameters.add("--network");
            parameters.add("log4shelldemo_default");
        } else {
            File pwd=new File(System.getProperty("user.dir"));
            File driver=new File(pwd,"driver");
            parameters.add("-v");
            parameters.add(driver.getAbsolutePath()+":/driver");
        }
        // add image name
        parameters.add(dc.jv.version);
        // setup java launcher
        parameters.add("java");
        if(dc.vmargs!=null) for(SystemProperty v: dc.vmargs) parameters.add(v.toVMString());

        String classpath=runnerPath+":"+dc.lv.location;

        parameters.add("-cp");
        parameters.add(classpath);
        parameters.add(Runner.class.getCanonicalName());
        parameters.add(dc.msg);

        if(dc.props!=null) for(String p: dc.props) parameters.add(p);

        return parameters;
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
            if(jv.present)  jv.active=!jv.active;
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

    public void cancel() {

        queue.clear();

    }
}
