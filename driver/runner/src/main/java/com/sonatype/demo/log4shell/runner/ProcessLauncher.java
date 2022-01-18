package com.sonatype.demo.log4shell.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessLauncher {

    public static  int javaLaunch(String classPath,String className,List<String> vmProperties,List<String> appParameters) throws Exception {


        List<String> parameters=new LinkedList<>();
        parameters.add("java");
        parameters.add("-cp");
        parameters.add(classPath);
        if(vmProperties!=null) parameters.addAll(vmProperties);
        parameters.add(className);
        if(appParameters!=null) parameters.addAll(appParameters);

        System.out.println("java cmd "+parameters);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(parameters);

        processBuilder.inheritIO();
        Process process = processBuilder.start();
            boolean r=process.waitFor(60, TimeUnit.SECONDS);
            if(process.isAlive()) {
                process.destroy();

            }

            return process.exitValue();
        }

}
