package com.sonatype.demo.log4shell.config;

import lombok.Data;

@Data
public class LogVersion{

    private int id;
   private String version;
   private String location;
    public boolean active=false;

    public LogVersion(String version,String location) {
        this.version=version;
        this.location=location;
        calcSortOrder();
    }

    private void calcSortOrder() {
        String[] bits=version.split("\\.");
        int[] vs=new int[3];
        for(int i=0;i<bits.length;i++) vs[i]=Integer.parseInt(bits[i]);

        id =vs[0]*100*100;
        id = id +(vs[1]*100);
        id = id +(vs[2]);


    }

    public String toString() {
        return version; //+"::"+location;
    }

	public String getVersion() {
		return version;
	}

	public String getLocation() {
		return location;
	}
}
