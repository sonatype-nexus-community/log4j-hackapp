package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shell.config.*;
import com.sonatype.demo.log4shell.runner.GridRunner;
import com.sonatype.demo.log4shelldemo.helpers.DockerEnvironment;
import com.sonatype.demo.log4shelldemo.helpers.ProcessHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.sonatype.demo.log4shell.runner.Runner.*;

public class DockerProcessRunner {

    private static final Logger log= LoggerFactory.getLogger(DockerProcessRunner.class);
    private final String runnerPath;

    public DockerProcessRunner(String runnerPath) {

        this.runnerPath=runnerPath;


    }

    /*
       Executes the docker process to run all the tests agains all the specfied log versions
       Returns a dataset with the resultant data parsed into the approproate groups

     */
    public List<String> runLogger(Configuration.RunnerConfig dc) throws Exception {

            List<String> dockerProcessConfig=createConfig(dc);

            log.info("driver setup: {}",String.join(" ",dockerProcessConfig));

           return  ProcessHelper.run(dockerProcessConfig);

    }



    private  List<String> createConfig(Configuration.RunnerConfig config) {


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
            parameters.add("source="+DockerEnvironment.logVolumeName+",target=/driver");
            parameters.add("--network");
            parameters.add("log4shelldemo");
        } else {
            File pwd=new File(System.getProperty("user.dir"));
            File driver=new File(pwd,"driver");
            parameters.add("-v");
            parameters.add(driver.getAbsolutePath()+":/driver");
        }
        // add image name
        parameters.add(config.getImageName());

        // setup java launcher
        parameters.add("java");

        // and any -D's

        for(SystemProperty v: config.getVMProperties()) {
            parameters.add(v.toVMString());
        }

        // add the classpath
        parameters.add("-cp");
        parameters.add(runnerPath);
        parameters.add(GridRunner.class.getCanonicalName());


        // add the properties we want to check the values for
        // these get reported by the runner in the output
        // java.version is always set

        Set<String> props=config.getReportingPropertyNames();
        if(props!=null && !props.isEmpty()) {
            parameters.add(REPORT_CMD);
            parameters.addAll(props);
        }

       if(config.hasVMProperties()) {
           parameters.add(PROPERTIES_CMD);
           for (SystemProperty sp : config.getVMProperties()) {
                parameters.add("-D"+sp.name+"="+sp.value);
           }
       }

       if(!config.hasLogVersions()) {
           throw new RuntimeException("no logversions specified");
       }
        List<LogVersion> versions= config.getLogVersions();
        parameters.add(LOG_CMD);
            for(LogVersion lv:versions) {
                parameters.add(lv.getVersion()+"::"+lv.getLocation()); // TODO make pairs of entries
            }

            if(!config.hasAttacks()) {
                throw new RuntimeException("no attack payloads specified");
            }

        parameters.add(PAYLOAD_CMD);


         for(ConfigElement<Attack> a:config.getAttacks()) {

                 if (a.getBase().payload == null) throw new RuntimeException("attack " + a + " has no payload");
                 parameters.add("" + a.getID());
                 parameters.add(a.getBase().payload);

          }


        return parameters;
    }

    public void  runAttacks(Configuration.RunnerConfig dc,ResultsLineHandler handler) throws Exception {

        List<String> dockerProcessConfig=createConfig(dc);

        log.info("driver setup(2): {}","{"+String.join("}{",dockerProcessConfig)+"}");

        runAttacks(dockerProcessConfig,handler);

    }


   private void runAttacks(List<String> parameters, ResultsLineHandler handler) {

        try {
            new ProcessExecutor().command(parameters)
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            handler.handle(line);
                        }
                    })
                    .timeout(180, TimeUnit.SECONDS)
                    .execute();
        } catch (Exception te) {
                handler.error(te);
        }
    }

}
