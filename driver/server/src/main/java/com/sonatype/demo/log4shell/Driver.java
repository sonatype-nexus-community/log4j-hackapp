package com.sonatype.demo.log4shell;

import com.sonatype.demo.log4shell.config.*;
import com.sonatype.demo.log4shell.ui.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Driver {

    private static final Logger log= LoggerFactory.getLogger(FrontEnd.class);
    private final BlockingQueue<Configuration.RunnerConfig> queue=new LinkedBlockingQueue<>();

    public final ResultsStore rs;

    private final Configuration config;


    public Driver(Configuration c,ResultsStore rs) {

        this.config=c;
        this.rs=rs;

        for(ConfigElement<JavaVersion> jv:config.getAllJavaVersions()) {
            rs.addJavaVersion(jv.getBase());
        }

        rs.addSpecialistConsole("ldap");
      //  rs.addSpecialistConsole("dns");
        launcherRunner();

    }

    /*
     Start the thread that deals with process requests
     */
    private void launcherRunner() {

        Thread t=new Thread(() -> {
            while(true) {
                try {
                    driveJavaImages(queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        t.start();
    }

    /*
    Takes the request group test and converts into an actual
    docker and java command

     */
    private void driveJavaImages(Configuration.RunnerConfig rcfg) {


        try {
            //create a launcher
            DockerProcessRunner r=new DockerProcessRunner(config.getRunnerPath());

            // run and collect results keyd by log id

            RawResultsConverter converter=new RawResultsConverter(rcfg,record -> {

                log.info("raw results returned {} lines",record.data.size());
                if(record.error!=null) {
                    record.result=ResultType.ERROR;
                } else {
                    record.result = record.getAttack().evaluate(record.data);
                }
                if(config.isSilentMode()) record.data=null; // remove unwanted results
                Console c = rs.addResults(record);
                if(c!=null) WebSocketHandler.handler.sendUpdate("console-" + c.handle + "-main");

            });

            r.runAttacks(rcfg,converter);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void drive(String payload) {

        List<ConfigElement<Attack>> attacks=config.getAdhocAttack(payload);
         drive(attacks);
    }

 public  void drive(List<ConfigElement<Attack>> attacks) {


        log.info("drive log4j / jvm combinations");

        config.generateRunnerConfigs(attacks, (c) -> {

            queue.add(c);

        });



    }

    public void cancel() {
        queue.clear();
    }



    public void runGridTest() {

        log.info("run grid test");
        List<ConfigElement<Attack>> attacks=config.getActiveAttacks();
        drive(attacks);

    }

    public int queueSize() {
        return queue.size();
    }




}
