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

import com.google.common.io.CharStreams;
import com.google.common.net.HostAndPort;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by geoff on 1/21/17.
 */
public class DockerRuleTest {

    @Rule
    public DockerRule dockerRule = new DockerRule("httpd:alpine")
            .leavingRunning(System.getProperty("testLeaveRunning") != null)
            .waitForLog("AH00094");

    @Test
    public void testAccess() throws Exception {

        InetSocketAddress accessToPort = dockerRule.getAccessToPort(80);

        System.out.println("Port 80 at " + accessToPort);
        assertThat(accessToPort.getPort(), not(equalTo(0)));

        final HostAndPort hostAndPort = dockerRule.getHostAndPort(80);
        assertThat(hostAndPort.getPort(), not(equalTo(0)));

        URL url = new URL("http", accessToPort.getHostName(), accessToPort.getPort(), "/");

        String strContent = CharStreams.toString(new InputStreamReader(((InputStream) url.getContent())));

        assertThat(strContent, containsString("It works"));
    }
}