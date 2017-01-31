[![Build Status](https://img.shields.io/travis/shinesolutions/aem-aem-stack-manager.svg)](http://travis-ci.org/shinesolutions/aem-stack-manager)

# AEM Stack Manager
AEM Stack Manager is a Java application for managing AEM stacks created using aem-aws-stack-builder.


## Build

This project requires Java 8 to compile and run the source code. Apache Maven 3.3 was used as the build tool.

### Create JAR file
```
mvn package
```
This will create a JAR file in the '\target' directory called aem-stack-manager-x.x.x.jar. 
By default the generated JAR file will contain all of the required dependencies
  

## Usage
The JAR file is created as a 'fully executable' jar. See Spring Boot [deployment and install](http://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html).

### Running the JAR
There are two ways to run the JAR:
```
java -jar aem-stack-manager-x.x.x.jar
```
or as a 'fully executable' direct application (only works on Unix/Linux based systems):
```
./aem-stack-manager-x.x.x.jar
```
