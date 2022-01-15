package com.sonatype.demo.log4shell;

import com.sonatype.demo.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DockerProcessRunner {

    private static Logger log= LoggerFactory.getLogger(DockerProcessRunner.class);
    private String runnerPath;
    public DockerProcessRunner(String runnerPath) {
        this.runnerPath=runnerPath;
    }

    private static List<String> run(List<String> parameters) throws Exception {
        File logger=new File("/tmp/log.log");
        logger.delete();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(parameters);
        processBuilder.redirectError(logger);
        processBuilder.redirectOutput(logger);
        Process process = processBuilder.start();
        boolean r=process.waitFor(30, TimeUnit.SECONDS);
        if(process.isAlive()) {
            process.destroy();
            log.info("process continued beyond 10 seconds");
        }


        List<String> data= Files.readAllLines(logger.toPath());

        log.info("{} lines gathered from output",data.size());

        return data;
    }


    public  List<String> runLogger(DriverConfig dc) throws Exception {
        List<String> dockerProcessConfig=createLogConfig(dc);
        log.info("driver setup: {}",String.join(" ",dockerProcessConfig));
        return run(dockerProcessConfig);
    }

    public static Set<String> getLocalDockerImages() throws Exception {

        List<String> parameters=new LinkedList<>();
        parameters.add("docker");
        parameters.add("image");
        parameters.add("ls");
        parameters.add("--format");
        parameters.add("{{.Repository}}:{{.Tag}}");

        List<String> rawList=DockerProcessRunner.run(parameters);

        Set<String> images=new HashSet<>();
        images.addAll(rawList);

        return images;

    }



    private  List<String> createLogConfig(DriverConfig dc) {


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
            parameters.add("log4shelldemo");
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

        // add the properties we want to check the values for
        // these get reported by the runner in the output
        // java.version is always set
        if(dc.reportingProperties!=null) for(String p: dc.reportingProperties) parameters.add(p);

        return parameters;
    }

}
