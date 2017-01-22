package me.itzg.testing;

import com.google.common.io.CharStreams;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by geoff on 1/21/17.
 */
public class DockerRuleTest {

    @Rule
    public DockerRule dockerRule = new DockerRule("itzg/elasticsearch:5");

    @Test
    public void testAccess() throws Exception {

        System.out.println("Inside test");

        InetSocketAddress accessToPort = dockerRule.getAccessToPort(9200);
        System.out.println("Port 9200 at " + accessToPort);

        URL url = new URL("http", accessToPort.getHostName(), accessToPort.getPort(), "/");

        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < 20000) {
            try {
                String strContent = CharStreams.toString(new InputStreamReader(((InputStream) url.getContent())));
                System.out.println(strContent);
                break;
            } catch (SocketException e) {
                Thread.sleep(500);
            }
        }
    }
}