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
    public DockerRule dockerRule = new DockerRule("itzg/elasticsearch:5")
            .leavingRunning(System.getProperty("testLeaveRunning") != null);

    @Test
    public void testAccess() throws Exception {

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