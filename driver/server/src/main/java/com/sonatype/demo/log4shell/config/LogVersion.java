package com.sonatype.demo.log4shell.config;

import lombok.Data;

@Data
public class LogVersion implements Comparable{

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



    @Override
    public int compareTo(Object o) {

        if(o==null) return -1;
        if(o instanceof LogVersion ==false) return -1;
        LogVersion lo= (LogVersion) o;
        if(lo==this) return 0;
        return Configuration.DockerImageNameComparitor.compareVersions(version,lo.version);

    }
}
