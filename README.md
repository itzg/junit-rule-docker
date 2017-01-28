[![CircleCI](https://circleci.com/gh/itzg/junit-rule-docker/tree/master.svg?style=svg)](https://circleci.com/gh/itzg/junit-rule-docker/tree/master)

[ ![Download](https://api.bintray.com/packages/itzgeoff/artifacts/junit-rule-docker/images/download.svg) ](https://bintray.com/itzgeoff/artifacts/junit-rule-docker/_latestVersion)


Provides a JUnit rule that starts a container prior to test execution, enables
access to the exposed ports of the container, and removes the container after
test execution.

This artifact is available in JCenter, so it may be included by adding this dependency:

```
<dependency>
    <groupId>me.itzg.testing</groupId>
    <artifactId>junit-rule-docker</artifactId>
    <version>1.1.1</version>
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

The following example shows how to use the rule to start an Elasticsearch instance
and lookup access to the container's port 9200:

```
@Rule
public DockerRule dockerRule = new DockerRule("itzg/elasticsearch:5");

@Test
public void testAccess() throws Exception {

    InetSocketAddress accessToPort = dockerRule.getAccessToPort(9200);
    
    ...
```