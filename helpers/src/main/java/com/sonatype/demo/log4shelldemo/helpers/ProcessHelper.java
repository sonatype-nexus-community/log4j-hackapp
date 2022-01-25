package com.sonatype.demo.log4shelldemo.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProcessHelper {

    private static Logger log= LoggerFactory.getLogger(ProcessHelper.class);

    public static List<String> run(List<String> parameters) throws Exception {
        File logger = Files.createTempFile("logshell", ".log").toFile();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(parameters);
        processBuilder.redirectError(logger);
        processBuilder.redirectOutput(logger);
        Process process = processBuilder.start();

        boolean r=process.waitFor(60, TimeUnit.SECONDS);
        if(process.isAlive()) {
            process.destroy();
            log.info("process continued beyond 60 seconds");
        }


        List<String> data= Files.readAllLines(logger.toPath());
        if(data==null) data=new LinkedList<>();
        logger.delete();

        log.info("{} lines gathered from output",data.size());

        return data;
    }


}
