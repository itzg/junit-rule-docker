package me.itzg.testing;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.junit.ClassRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;


public class DockerRuleTestElasticsearch {

	private static final Logger LOGGER = LogManager.getLogger(DockerRuleTestElasticsearch.class);

	protected final static String TEST_CLUSTER = "testCluster";
	protected final static String TEST_INDEX = "testindex";
	private final static String DOCKER_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:5.5.2";

	@ClassRule
	public static DockerRule elasticSearchRule =
			new DockerRule(DOCKER_IMAGE)
					.parameter("cluster.name="+TEST_CLUSTER, "xpack.security.enabled=false")
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
			assertEquals(jsonResponse, "{\"_all\":{\"primaries\":{\"docs\":{\"count\":0,\"deleted\":0},\"store\":{\"size_in_bytes\":324,\"throttle_time_in_millis\":0},\"indexing\":{\"index_total\":0,\"index_time_in_millis\":0,\"index_current\":0,\"index_failed\":0,\"delete_total\":0,\"delete_time_in_millis\":0,\"delete_current\":0,\"noop_update_total\":0,\"is_throttled\":false,\"throttle_time_in_millis\":0},\"get\":{\"total\":0,\"time_in_millis\":0,\"exists_total\":0,\"exists_time_in_millis\":0,\"missing_total\":0,\"missing_time_in_millis\":0,\"current\":0},\"search\":{\"open_contexts\":0,\"query_total\":0,\"query_time_in_millis\":0,\"query_current\":0,\"fetch_total\":0,\"fetch_time_in_millis\":0,\"fetch_current\":0,\"scroll_total\":0,\"scroll_time_in_millis\":0,\"scroll_current\":0,\"suggest_total\":0,\"suggest_time_in_millis\":0,\"suggest_current\":0},\"merges\":{\"current\":0,\"current_docs\":0,\"current_size_in_bytes\":0,\"total\":0,\"total_time_in_millis\":0,\"total_docs\":0,\"total_size_in_bytes\":0,\"total_stopped_time_in_millis\":0,\"total_throttled_time_in_millis\":0,\"total_auto_throttle_in_bytes\":104857600},\"refresh\":{\"total\":0,\"total_time_in_millis\":0,\"listeners\":0},\"flush\":{\"total\":0,\"total_time_in_millis\":0},\"warmer\":{\"current\":0,\"total\":5,\"total_time_in_millis\":0},\"query_cache\":{\"memory_size_in_bytes\":0,\"total_count\":0,\"hit_count\":0,\"miss_count\":0,\"cache_size\":0,\"cache_count\":0,\"evictions\":0},\"fielddata\":{\"memory_size_in_bytes\":0,\"evictions\":0},\"completion\":{\"size_in_bytes\":0},\"segments\":{\"count\":0,\"memory_in_bytes\":0,\"terms_memory_in_bytes\":0,\"stored_fields_memory_in_bytes\":0,\"term_vectors_memory_in_bytes\":0,\"norms_memory_in_bytes\":0,\"points_memory_in_bytes\":0,\"doc_values_memory_in_bytes\":0,\"index_writer_memory_in_bytes\":0,\"version_map_memory_in_bytes\":0,\"fixed_bit_set_memory_in_bytes\":0,\"max_unsafe_auto_id_timestamp\":-1,\"file_sizes\":{}},\"translog\":{\"operations\":0,\"size_in_bytes\":215},\"request_cache\":{\"memory_size_in_bytes\":0,\"evictions\":0,\"hit_count\":0,\"miss_count\":0},\"recovery\":{\"current_as_source\":0,\"current_as_target\":0,\"throttle_time_in_millis\":0}},\"total\":{\"docs\":{\"count\":0,\"deleted\":0},\"store\":{\"size_in_bytes\":324,\"throttle_time_in_millis\":0},\"indexing\":{\"index_total\":0,\"index_time_in_millis\":0,\"index_current\":0,\"index_failed\":0,\"delete_total\":0,\"delete_time_in_millis\":0,\"delete_current\":0,\"noop_update_total\":0,\"is_throttled\":false,\"throttle_time_in_millis\":0},\"get\":{\"total\":0,\"time_in_millis\":0,\"exists_total\":0,\"exists_time_in_millis\":0,\"missing_total\":0,\"missing_time_in_millis\":0,\"current\":0},\"search\":{\"open_contexts\":0,\"query_total\":0,\"query_time_in_millis\":0,\"query_current\":0,\"fetch_total\":0,\"fetch_time_in_millis\":0,\"fetch_current\":0,\"scroll_total\":0,\"scroll_time_in_millis\":0,\"scroll_current\":0,\"suggest_total\":0,\"suggest_time_in_millis\":0,\"suggest_current\":0},\"merges\":{\"current\":0,\"current_docs\":0,\"current_size_in_bytes\":0,\"total\":0,\"total_time_in_millis\":0,\"total_docs\":0,\"total_size_in_bytes\":0,\"total_stopped_time_in_millis\":0,\"total_throttled_time_in_millis\":0,\"total_auto_throttle_in_bytes\":104857600},\"refresh\":{\"total\":0,\"total_time_in_millis\":0,\"listeners\":0},\"flush\":{\"total\":0,\"total_time_in_millis\":0},\"warmer\":{\"current\":0,\"total\":5,\"total_time_in_millis\":0},\"query_cache\":{\"memory_size_in_bytes\":0,\"total_count\":0,\"hit_count\":0,\"miss_count\":0,\"cache_size\":0,\"cache_count\":0,\"evictions\":0},\"fielddata\":{\"memory_size_in_bytes\":0,\"evictions\":0},\"completion\":{\"size_in_bytes\":0},\"segments\":{\"count\":0,\"memory_in_bytes\":0,\"terms_memory_in_bytes\":0,\"stored_fields_memory_in_bytes\":0,\"term_vectors_memory_in_bytes\":0,\"norms_memory_in_bytes\":0,\"points_memory_in_bytes\":0,\"doc_values_memory_in_bytes\":0,\"index_writer_memory_in_bytes\":0,\"version_map_memory_in_bytes\":0,\"fixed_bit_set_memory_in_bytes\":0,\"max_unsafe_auto_id_timestamp\":-1,\"file_sizes\":{}},\"translog\":{\"operations\":0,\"size_in_bytes\":215},\"request_cache\":{\"memory_size_in_bytes\":0,\"evictions\":0,\"hit_count\":0,\"miss_count\":0},\"recovery\":{\"current_as_source\":0,\"current_as_target\":0,\"throttle_time_in_millis\":0}}},\"indices\":{\"testindex\":{\"primaries\":{\"docs\":{\"count\":0,\"deleted\":0},\"store\":{\"size_in_bytes\":324,\"throttle_time_in_millis\":0},\"indexing\":{\"index_total\":0,\"index_time_in_millis\":0,\"index_current\":0,\"index_failed\":0,\"delete_total\":0,\"delete_time_in_millis\":0,\"delete_current\":0,\"noop_update_total\":0,\"is_throttled\":false,\"throttle_time_in_millis\":0},\"get\":{\"total\":0,\"time_in_millis\":0,\"exists_total\":0,\"exists_time_in_millis\":0,\"missing_total\":0,\"missing_time_in_millis\":0,\"current\":0},\"search\":{\"open_contexts\":0,\"query_total\":0,\"query_time_in_millis\":0,\"query_current\":0,\"fetch_total\":0,\"fetch_time_in_millis\":0,\"fetch_current\":0,\"scroll_total\":0,\"scroll_time_in_millis\":0,\"scroll_current\":0,\"suggest_total\":0,\"suggest_time_in_millis\":0,\"suggest_current\":0},\"merges\":{\"current\":0,\"current_docs\":0,\"current_size_in_bytes\":0,\"total\":0,\"total_time_in_millis\":0,\"total_docs\":0,\"total_size_in_bytes\":0,\"total_stopped_time_in_millis\":0,\"total_throttled_time_in_millis\":0,\"total_auto_throttle_in_bytes\":104857600},\"refresh\":{\"total\":0,\"total_time_in_millis\":0,\"listeners\":0},\"flush\":{\"total\":0,\"total_time_in_millis\":0},\"warmer\":{\"current\":0,\"total\":5,\"total_time_in_millis\":0},\"query_cache\":{\"memory_size_in_bytes\":0,\"total_count\":0,\"hit_count\":0,\"miss_count\":0,\"cache_size\":0,\"cache_count\":0,\"evictions\":0},\"fielddata\":{\"memory_size_in_bytes\":0,\"evictions\":0},\"completion\":{\"size_in_bytes\":0},\"segments\":{\"count\":0,\"memory_in_bytes\":0,\"terms_memory_in_bytes\":0,\"stored_fields_memory_in_bytes\":0,\"term_vectors_memory_in_bytes\":0,\"norms_memory_in_bytes\":0,\"points_memory_in_bytes\":0,\"doc_values_memory_in_bytes\":0,\"index_writer_memory_in_bytes\":0,\"version_map_memory_in_bytes\":0,\"fixed_bit_set_memory_in_bytes\":0,\"max_unsafe_auto_id_timestamp\":-1,\"file_sizes\":{}},\"translog\":{\"operations\":0,\"size_in_bytes\":215},\"request_cache\":{\"memory_size_in_bytes\":0,\"evictions\":0,\"hit_count\":0,\"miss_count\":0},\"recovery\":{\"current_as_source\":0,\"current_as_target\":0,\"throttle_time_in_millis\":0}},\"total\":{\"docs\":{\"count\":0,\"deleted\":0},\"store\":{\"size_in_bytes\":324,\"throttle_time_in_millis\":0},\"indexing\":{\"index_total\":0,\"index_time_in_millis\":0,\"index_current\":0,\"index_failed\":0,\"delete_total\":0,\"delete_time_in_millis\":0,\"delete_current\":0,\"noop_update_total\":0,\"is_throttled\":false,\"throttle_time_in_millis\":0},\"get\":{\"total\":0,\"time_in_millis\":0,\"exists_total\":0,\"exists_time_in_millis\":0,\"missing_total\":0,\"missing_time_in_millis\":0,\"current\":0},\"search\":{\"open_contexts\":0,\"query_total\":0,\"query_time_in_millis\":0,\"query_current\":0,\"fetch_total\":0,\"fetch_time_in_millis\":0,\"fetch_current\":0,\"scroll_total\":0,\"scroll_time_in_millis\":0,\"scroll_current\":0,\"suggest_total\":0,\"suggest_time_in_millis\":0,\"suggest_current\":0},\"merges\":{\"current\":0,\"current_docs\":0,\"current_size_in_bytes\":0,\"total\":0,\"total_time_in_millis\":0,\"total_docs\":0,\"total_size_in_bytes\":0,\"total_stopped_time_in_millis\":0,\"total_throttled_time_in_millis\":0,\"total_auto_throttle_in_bytes\":104857600},\"refresh\":{\"total\":0,\"total_time_in_millis\":0,\"listeners\":0},\"flush\":{\"total\":0,\"total_time_in_millis\":0},\"warmer\":{\"current\":0,\"total\":5,\"total_time_in_millis\":0},\"query_cache\":{\"memory_size_in_bytes\":0,\"total_count\":0,\"hit_count\":0,\"miss_count\":0,\"cache_size\":0,\"cache_count\":0,\"evictions\":0},\"fielddata\":{\"memory_size_in_bytes\":0,\"evictions\":0},\"completion\":{\"size_in_bytes\":0},\"segments\":{\"count\":0,\"memory_in_bytes\":0,\"terms_memory_in_bytes\":0,\"stored_fields_memory_in_bytes\":0,\"term_vectors_memory_in_bytes\":0,\"norms_memory_in_bytes\":0,\"points_memory_in_bytes\":0,\"doc_values_memory_in_bytes\":0,\"index_writer_memory_in_bytes\":0,\"version_map_memory_in_bytes\":0,\"fixed_bit_set_memory_in_bytes\":0,\"max_unsafe_auto_id_timestamp\":-1,\"file_sizes\":{}},\"translog\":{\"operations\":0,\"size_in_bytes\":215},\"request_cache\":{\"memory_size_in_bytes\":0,\"evictions\":0,\"hit_count\":0,\"miss_count\":0},\"recovery\":{\"current_as_source\":0,\"current_as_target\":0,\"throttle_time_in_millis\":0}}}}}");

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testDocker() throws DockerCertificateException, DockerException, InterruptedException {
		final DockerClient docker = DefaultDockerClient.fromEnv().build();
		docker.pull(DOCKER_IMAGE);
	}

}
