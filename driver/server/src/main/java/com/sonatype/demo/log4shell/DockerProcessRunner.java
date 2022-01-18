package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shell.runner.GroupRunner;
import com.sonatype.demo.log4shelldemo.helpers.DockerEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.sonatype.demo.log4shell.runner.Runner.*;

public class DockerProcessRunner {

    private static Logger log= LoggerFactory.getLogger(DockerProcessRunner.class);
    private String runnerPath;
    public DockerProcessRunner(String runnerPath) {
        this.runnerPath=runnerPath;
    }

    private static List<String> run(List<String> parameters) throws Exception {
      File logger = Files.createTempFile("logshell", ".log").toFile();

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
        logger.delete();

        log.info("{} lines gathered from output",data.size());

        return data;
    }

    /*
       Executes the docker process to run all the tests agains all the specfied log versions
       Returns a dataset with the resultant data parsed into the approproate groups

     */
    public ResultSet runLogger(JavaVersionTestConfig dc) throws Exception {


            List<String> dockerProcessConfig=createConfig(dc);
            log.info("driver setup: {}",String.join(" ",dockerProcessConfig));
            List<String> data= run(dockerProcessConfig);
            for(String s:data) {
                System.out.println(">"+s);
            }
            ResultSet rs=new ResultSet();
            String logVersion=null;
            String preMsg=null;

            for(String line:data) {
                int index=line.indexOf(GroupRunner.RUNNER_GROUP_SEPERATOR);
                if(index>=0) {
                    String suffix=line.substring(index+1+(GroupRunner.RUNNER_GROUP_SEPERATOR.length()));
                    logVersion=suffix;
                    preMsg=null;
                    continue;
                }
                index=line.indexOf(RUNNER_MESSAGE_SEPERATOR);
                if(index>=0) {
                    preMsg = line.substring(index + 1+(RUNNER_MESSAGE_SEPERATOR.length()));
                    continue;
                }
                index=line.indexOf(RUNNER_PROPERTY);
                if(index>=0) {
                    String l=line.substring(index + 1 +(RUNNER_PROPERTY.length()));
                    index=l.indexOf("!!");
                    String key=line.substring(0,index);
                    String suffix=line.substring(index+2);
                    char a=suffix.charAt(0);
                    switch(a) {
                        case '?' : //missing
                            rs.addMissingProperty(logVersion,preMsg,key);
                          break;
                        case '=' : // got a value
                            rs.addPropertyValue(logVersion,preMsg,key,suffix.substring(1));
                          break;
                    }
                    continue;

                }

                System.out.println("lv="+logVersion+",pm="+preMsg+",r="+line);
                rs.add(logVersion,preMsg,line);


            }

            return rs;
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



    private  List<String> createConfig(JavaVersionTestConfig dc) {


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



        if(DockerEnvironment.inDockerContainer) {
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
        parameters.add(dc.getImageName());

        // setup java launcher
        parameters.add("java");

        // and any -D's

        for(SystemProperty v: dc.getVMProperties()) {
            parameters.add(v.toVMString());
        }

        // add the classpath
        parameters.add("-cp");
        parameters.add(runnerPath);
        parameters.add(GroupRunner.class.getCanonicalName());


        // add the properties we want to check the values for
        // these get reported by the runner in the output
        // java.version is always set

        Set<String> props=dc.getReportingProperties();
        if(props!=null && props.isEmpty()==false ) {
            parameters.add(REPORT_CMD);
            parameters.addAll(props);
        }

        List<LogVersion> versions= dc.getLogVersions();
        if(versions==null || versions.isEmpty()) {
            throw new RuntimeException("no logversions specified");
        }

            parameters.add(LOG_CMD);
            for(LogVersion lv:versions) {
                parameters.add(lv.toString());
            }

        Collection<String> msgs=dc.getMessages();
            if(msgs==null || msgs.isEmpty()) {
                throw new RuntimeException("no messages specified");
            }
        parameters.add(MSG_CMD);
        parameters.addAll(dc.getMessages());


        return parameters;
    }

}
