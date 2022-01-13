# log4j-hackapp

This application is designed to provide an easy way to explore the Log4Shell Vulnerability.


## To build
```
mvn compile package
docker-compose build 
```

## setup

Edit the javalevels.txt file in the root of the project.
Add the names of docker images that have Java installed to the list.
Note that the Java runtime in the image of your choice must be at least Java 8 and must be on the container classpath. 
All images listed must be in the local docker cache otherwise the will be shown by the tool but they will be disabled.

Simply pull the image before running this application 

```
docker pull <imagename>
```


## to run
```
docker-compose up 
```

Now open a browser to [localhost:8080](http:localhost:8080)


## How to change log4j versions

To add new log4j versions copy an existing module under drivr/log4jversions and amend to the version your require. 
Note that you **must** run a *mvn package* afterwards even for local development 

## dev mode

the application can be run outside docker in a development mode.  
Manually run the components.

- com.sonatype.demo.log4shell.FrontEnd   (in driver/server module)
- BasicTodoList   (in application module)
- com.sonatype.demo.log4shell.ldapserver.Main (in ldapserver module)



