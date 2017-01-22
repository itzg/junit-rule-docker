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

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.NetworkSettings;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Geoff Bourne
 * @since Jan 2017
 */
public class DockerRule implements TestRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerRule.class);

    private final String image;
    private String[] command;
    private DockerClient dockerClient;
    private ContainerCreation container;
    private boolean leaveRunning;

    public DockerRule(String image) {
        this.image = image;
    }

    public DockerRule command(String... command) {
        this.command = command;
        return this;
    }

    public DockerRule leavingRunning(boolean leaveRunning) {
        this.leaveRunning = leaveRunning;
        return this;
    }

    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                dockerClient = DefaultDockerClient.fromEnv().build();

                dockerClient.pull(image, new ProgressHandler() {
                    @Override
                    public void progress(ProgressMessage progressMessage) throws DockerException {
                        if (progressMessage.id() != null && progressMessage.progress() == null) {
                            LOGGER.info("Pulling {} : {} : {}", image, progressMessage.id(), progressMessage.status());
                        } else if (progressMessage.id() == null) {
                            LOGGER.info("Pulling {} : {}", image, progressMessage.status());

                        }
                    }
                });

                HostConfig.Builder hostConfigBuilder = HostConfig.builder();
                hostConfigBuilder.publishAllPorts(true);

                ContainerConfig.Builder configBuilder = ContainerConfig.builder()
                        .image(image)
                        .hostConfig(hostConfigBuilder.build())
                        .cmd(command);

                container = dockerClient.createContainer(configBuilder.build());

                final String id = container.id();

                LOGGER.info("Starting container {}", id);

                dockerClient.startContainer(id);

                statement.evaluate();

                if (!leaveRunning) {
                    LOGGER.info("Stopping container {}", id);
                    dockerClient.killContainer(id);
                    dockerClient.removeContainer(id);
                }
                else {
                    LOGGER.info("Leaving container {} running", id);
                }

                dockerClient.close();
            }
        };
    }

    public InetSocketAddress getAccessToPort(int containerPort) throws DockerException, InterruptedException {
        ContainerInfo containerInfo = dockerClient.inspectContainer(container.id());

        NetworkSettings networkSettings = containerInfo.networkSettings();

        List<PortBinding> portBindings = networkSettings.ports().get(String.format("%d/tcp", containerPort));
        PortBinding portBinding = portBindings.get(0);

        return new InetSocketAddress(dockerClient.getHost(), Integer.parseInt(portBinding.hostPort()));
    }
}
