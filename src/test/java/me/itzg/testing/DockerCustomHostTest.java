/*
 * Copyright (c) 2017 by Geoff Bourne <itzgeoff@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.itzg.testing;

import org.junit.ClassRule;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Geoff Bourne
 * @since Jan 2017
 */
@org.junit.Ignore("used for testing custom dockerHostUri")
public class DockerCustomHostTest {

    @ClassRule
    public static DockerRule dockerRule = new DockerRule("itzg/elasticsearch:5")
            .uri(System.getProperty("dockerHostUri"))
            .dockerCertPath(Paths.get(System.getProperty("dockerCerts")))
            .leavingRunning(true);

    @Test
    public void testSuccess() throws Exception {
        final InetSocketAddress accessToPort = dockerRule.getAccessToPort(9200);

        HttpAssert.waitForEndpoint(new URL("http", accessToPort.getHostName(), accessToPort.getPort(), "/"),
                500,
                20,
                200);
    }
}
