Provides a JUnit rule that starts a container prior to test execution, enables
access to the exposed ports of the container, and removes the container after
test execution.

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