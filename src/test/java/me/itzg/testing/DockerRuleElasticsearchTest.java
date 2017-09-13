package me.itzg.testing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class DockerRuleElasticsearchTest {

	protected final static String TEST_CLUSTER = "testCluster";
	protected final static String TEST_INDEX = "testindex";
	private static final Logger LOGGER = LogManager.getLogger(DockerRuleElasticsearchTest.class);
	private final static String DOCKER_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:5.5.2";

	@ClassRule
	public static DockerRule elasticSearchRule =
			new DockerRule(DOCKER_IMAGE)
					.parameter("cluster.name=" + TEST_CLUSTER, "xpack.security.enabled=false")
					.waitForLog("started");

	@Test
	public void testConnectsToDocker() throws Exception {
		try {
			LOGGER.debug("connecting to: " + elasticSearchRule.getAccessToPort(9300));
			Settings settings = Settings.builder()
					.put("cluster.name", TEST_CLUSTER)
					.put("client.transport.sniff", false) //discover rest of the cluster
					.put("client.transport.ignore_cluster_name", true)
					.put("discovery.zen.fd.ping_timeout", "60s")
					.put("transport.tcp.connect_timeout", "60s")
					.build();
			Client client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(elasticSearchRule.getAccessToPort(9300)));

			LOGGER.debug("client connected");
			client.admin().indices().prepareCreate(TEST_INDEX).get();

			IndicesStatsResponse indicesStatsResponse = client
					.admin()
					.indices()
					.prepareStats(TEST_INDEX)
					.all()
					.execute().actionGet();

			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder.startObject();
			indicesStatsResponse.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			String jsonResponse = builder.prettyPrint().string();

			assertTrue("Response from ES is missing", jsonResponse.contains("_all"));
			assertTrue("Test Index is missing in ES Response", jsonResponse.contains(TEST_INDEX));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
