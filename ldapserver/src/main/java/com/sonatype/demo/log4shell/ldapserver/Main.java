package com.sonatype.demo.log4shell.ldapserver;

import com.unboundid.ldap.listener.*;
import com.unboundid.ldap.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetAddress;

public class Main {

    private static Logger log= LoggerFactory.getLogger(Main.class);
    public static final String BASEDNS="dc=example,dc=org";
    public static final int port=1389;


    public static void main(String[] args) throws Exception {


            Main m=new Main();

            Thread.sleep(2000);
            m.run(args);


    }

    private void run(String[] args) throws Exception {



        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(BASEDNS);
        config.setListenerConfigs(new InMemoryListenerConfig(
                "listen",
                InetAddress.getByName("0.0.0.0"),
                port,
                ServerSocketFactory.getDefault(),
                SocketFactory.getDefault(),
                (SSLSocketFactory) SSLSocketFactory.getDefault()));

        String server=InetAddress.getLocalHost().getHostAddress();

        Interceptor i=new Interceptor();
        config.addInMemoryOperationInterceptor(i);

        InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
        log.info("server address {}",server);
        log.info("LDAP Listening on 0.0.0.0:" + port);
        ds.startListening();
    }

}

