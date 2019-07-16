This is a new version of the Node Agent, which is a piece of software that sits inside a web-services containder (tomcat) and exposes a web-service endpoint to submit RTML (Robotic Telescope Markup Language) XML documents to. The Node Agent then makes an RMI (Remote Method Invocation) call to the TEA (Telescope Embedded Agent), which in turn either submits the Group encoded in the RTML document into the telescope's PhaseII database, or takes control of the telescope via the TOCA (Target of Oppurtunity Control Agent) interface for real-time followup.

The original Node Agent seemed to use an odd version of web service protocols which made it difficult to write multi-language clients for it. This version has been redeveloped using metro-jax-ws (https://github.com/javaee/metro-jax-ws) and eclipse following a tutorial here:
https://examples.javacodegeeks.com/enterprise-java/jws/jax-ws-web-services-on-tomcat/

The client-side Java code was based on the example here:
https://examples.javacodegeeks.com/enterprise-java/jws/jax-ws-hello-world-example-rpc-style/

The web-service authentication was developed following this:
https://www.mkyong.com/webservices/jax-ws/application-authentication-with-jax-ws/
It is currently pretty poor (i.e. passwords in plain-text, possibility of replay attacks etc) and should probably
be improved.

The client-side python code was developed using suds:

https://webkul.com/blog/python-suds-client/

also looking at this:

https://www.codeproject.com/Articles/238778/Web-Services-in-Ruby-Python-and-Java

Client-side authentication in python was developed looking at this:

https://stackoverflow.com/questions/115316/how-can-i-consume-a-wsdl-soap-web-service-in-python?rq=1


Most of the 'guts' of the Node Agent Code (TEA RMI invocation etc) has been copied from Neil's original node_agent, but repacked to avoid any potential namespace conflicts.

# Dependancies

## Libraries

Various dependancy libraries need installing in  WebContent/WEB-INF/lib for attempting a build:
From the jaxws-ri software (downloaded from:
https://repo1.maven.org/maven2/com/sun/xml/ws/jaxws-ri/2.3.0/jaxws-ri-2.3.0.zip

* FastInfoset.jar
* gmbal-api-only.jar
* ha-api.jar
* javax.annotation-api.jar
* javax.xml.soap-api.jar
* jaxb-api.jar
* jaxb-core.jar
* jaxb-impl.jar
* jaxb-jxc.jar
* jaxb-xjc.jar
* jaxws-api.jar
* jaxws-rt.jar
* jaxws-tools.jar
* jsr181-api.jar
* management-api.jar
* mimepull.jar
* policy.jar
* resolver.jar
* saaj-impl.jar
* stax2-api.jar
* stax-ex.jar
* streambuffer.jar
* woodstox-core-asl.jar

From ltdevsrv:/home/dev/bin/javalib/:
* ngat_util.jar
* ngat_util_logging.jar

From ltdevsrv:/home/dev/bin/estar/javalib/:
* org_estar_astrometry.jar
* org_estar_cluster.jar
* org_estar_rtml.jar
* tea.jar

## Eclipse

Eclipse needs the following plugins installed.
Go to Eclipse->Help menu->Install new software choose the Web, XML, Java EE plugin and install it.
(from the kepler software site)

The project was built as a Dynamic Web Poject, i.e. File->New->Other->Web->Dynamic Web Poject

The .project and .classpth files are in the repository as node_agent2.project and node_agent2.classpath.

# Building

There is currently no working ant build script for this project. Once installed in eclipse,
* Right click on the 'node_agent2' project
* Project->Export. Select Web -> WAR file
* Select node_agent2/bin/node_agent2.war as the output destination.

The software is also built into a jar, for the Java client:
* Right Click on the node_agent2 Project -> Export. Select Java -> JAR file
* Save to: node_agent2/bin/node_agent2.jar

# Installation

Copy the node_agent2.war to /usr/local/tomcat/webapps/ on the deployment machine (ltproxy).

The tomcat used will need to be running at least Java 8 (using the version of jaxws-ri mentioned above).

We can test the deployment was successful: http://ltproxy:8080/node_agent2/node_agent

Look in /usr/local/tomcat/logs/catalina.out for problems.

The WSDL (Web Services Description Lanaguage) for this endpoint is then available here:

http://ltproxy:8080/node_agent2/node_agent?wsdl

# Client software

## Java Client

Ensure node_agent2.jar is in the CLASSPATH, or run the client from node_agent2/build/classes.

Run the Java client as follows:
java org.estar.node_agent2.client.NodeAgentClient -help

e.g.
java org.estar.node_agent2.client.NodeAgentClient -host ltproxy -port 8080 -username <username> -password <password> -handle_rtml test_rtml_document.rtml -output_filename output.rtml

## Python client

The python client requires suds for web-services support:

For python 2.7
```shell
pip install suds
```    
</br>

For python 3.x
```shell
pip install suds-py3
```
</br>


The python client software is in node_agent2/src/python

Invoke as follows:
```shell
python NodeAgentClient.py --help

python NodeAgentClient.py --hostname ltproxy --port_number 8080 --username <username> --password <password> --handle_rtml test_rtml_document.rtml --output output.rtml
```
