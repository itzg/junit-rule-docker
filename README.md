[![CircleCI](https://circleci.com/gh/itzg/junit-rule-docker/tree/master.svg?style=svg)](https://circleci.com/gh/itzg/junit-rule-docker/tree/master)
[ ![Download](https://api.bintray.com/packages/itzgeoff/artifacts/junit-rule-docker/images/download.svg) ](https://bintray.com/itzgeoff/artifacts/junit-rule-docker/_latestVersion)


Provides a JUnit rule that starts a container prior to test execution, enables
access to the exposed ports of the container, and removes the container after
test execution.

This artifact is [available at JCenter](https://bintray.com/bintray/jcenter?filterByPkgName=junit-rule-docker), 
so it may be included by adding this dependency:

```
<dependency>
    <groupId>me.itzg.testing</groupId>
    <artifactId>junit-rule-docker</artifactId>
    <version>1.2</version>
    <scope>test</scope>
</dependency>
```

and the jcenter repository, if you haven't already:

```
<repository>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
    <id>bintrary</id>
    <name>bintray</name>
    <url>http://jcenter.bintray.com</url>
</repository>
```

The following example shows how to use the rule to start an Elasticsearch instance, waiting for Elasticsearch to
start, and lookup access to the container's port 9200:

```
@Rule
public DockerRule dockerRule = new DockerRule("itzg/elasticsearch:5")
                                   .waitForLog("started");

@Test
public void testAccess() throws Exception {

    InetSocketAddress accessToPort = dockerRule.getAccessToPort(9200);
    
    ...
```