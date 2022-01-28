package com.sonatype.demo.log4shelldemo.helpers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class DosObjectGenerator {

    public Map<String,String> genDoSObject() throws IOException {

        Object[][] o=new Object[][]{ new Object[0]};
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        ObjectOutputStream oos=new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();

        return null;

    }
}
