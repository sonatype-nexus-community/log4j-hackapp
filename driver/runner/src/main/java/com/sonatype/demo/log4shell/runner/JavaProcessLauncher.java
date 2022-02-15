package com.sonatype.demo.log4shell.runner;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class JavaProcessLauncher {

    public static  int javaLaunch(String classPath, String className, Set<VMProperty> vmProperties, List<String> appParameters) throws Exception {

        List<String> parameters=new LinkedList<>();
        parameters.add("java");
        parameters.add("-cp");
        parameters.add(classPath);

        for(VMProperty p:vmProperties) {
            parameters.add(p.toVMValue());
        }

        parameters.add(className);
        if(appParameters!=null) parameters.addAll(appParameters);

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
