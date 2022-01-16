package com.sonatype.demo.log4shelldemo.helpers;

import java.io.File;

public class DockerEnvironment {
    // if running local mount the runner and log jar seperately
    public static final boolean inDockerContainer =(new File("/.dockerenv")).exists();
}
