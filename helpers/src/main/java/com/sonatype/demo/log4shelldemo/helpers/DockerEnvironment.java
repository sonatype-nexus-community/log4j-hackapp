package com.sonatype.demo.log4shelldemo.helpers;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DockerEnvironment {
    // if running local mount the runner and log jar seperately
    public static final boolean inDockerContainer =(new File("/.dockerenv")).exists();

    public static String logVolumeName;

    static {
        if(inDockerContainer) {
            try {
                logVolumeName=  getLogVolumeName();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static Set<String> getLocalDockerImages() throws Exception {

        List<String> parameters=new LinkedList<>();
        parameters.add("docker");
        parameters.add("image");
        parameters.add("ls");
        parameters.add("--format");
        parameters.add("{{.Repository}}:{{.Tag}}");

        List<String> rawList=ProcessHelper.run(parameters);

        Set<String> images=new HashSet<>();
        images.addAll(rawList);

        return images;

    }


   public static List<String> getDockerLog4JVolumes() {

        List<String> parameters=new LinkedList<>();
        parameters.add("docker");
        parameters.add("volume");
        parameters.add("ls");
        parameters.add("--format");
        parameters.add("{{.Name}}");
        parameters.add("--filter");
        parameters.add("name=logjars");

       List<String> rawList =new LinkedList<>();
       try {
            rawList = ProcessHelper.run(parameters);

         } catch(Exception e) {
            e.printStackTrace();
        }
      return rawList;

    }


    public static String getLogVolumeName() throws Exception {

        if(DockerEnvironment.inDockerContainer) {
            List<String> names=getDockerLog4JVolumes();

            if(names==null) {
                throw new RuntimeException("unable to locate any volumnes");
            }
            if(names.isEmpty()) {
                throw new RuntimeException("unable to locate runtime log4j volume with suffix logjars");
            }
            if(names.size()>1) {
                for(String s:names) {
                    System.out.println(s);
                }
                throw new RuntimeException("Found more than one candidate log volume with suffix logjars");
            }

            return names.toArray(new String[0])[0];
        }
        return "<n/a>";
    }
}
