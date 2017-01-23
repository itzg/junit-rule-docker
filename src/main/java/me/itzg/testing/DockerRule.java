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

import com.google.common.base.Optional;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.NetworkSettings;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

/**
 * This is a JUnit rule that manages the lifecycle of a Docker container around a test case or suite.
 *
 * <p>The following is an example use as a class rule:</p>
 *
 * <pre>
 *     public class YourTest {
 *         &#64;Rule
 *         public static DockerRule dockerRule = new DockerRule("itzg/elasticsearch");
 *
 *         &#64;Test
 *         public void someTest() { ... }
 * </pre>
 *
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
    private boolean skipWhenOffline;
    private String uri;
    private Path dockerCertPath;

    public DockerRule(String image) {
        this.image = image;
    }

    /**
     * Specifies the command-line arguments to pass to the container.
     * @param command one or more command line arguments
     * @return this for chaining
     */
    public DockerRule command(String... command) {
        this.command = command;
        return this;
    }

    /**
     * Allows for configuring a Docker endpoint URI other than what's in the current environment.
     * @param uri the Docker daemon's endpoint
     * @return this for chaining
     */
    public DockerRule uri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Allows for leaving the created container running after the test rule has completed. This
     * might be useful if needing to further debug the results.
     * @param leaveRunning true to leave the container running, false to stop and remove the container
     * @return this for chaining
     */
    public DockerRule leavingRunning(boolean leaveRunning) {
        this.leaveRunning = leaveRunning;
        return this;
    }

    /**
     * Used to indicate that the test scenario should be skipped when unable to connect to the
     * Docker daemon. This option should be used with caution since it may cause an entire suite
     * to be skipped when used as a {@link org.junit.ClassRule}.
     * @param skipWhenOffline true to use {@link Assume} to skip the test scenario when failing to connect
     * @return this for chaining
     */
    public DockerRule skipWhenOffline(boolean skipWhenOffline) {
        this.skipWhenOffline = skipWhenOffline;
        return this;
    }

    /**
     * Specifies a specific location where the required set of client certificates are located.
     * @param dockerCertPath the path to a directory that contains cert.pem, key.pem, and ca.pem
     * @return this for chaining
     */
    public DockerRule dockerCertPath(Path dockerCertPath) {
        this.dockerCertPath = dockerCertPath;
        return this;
    }

    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
//                final DefaultDockerClient.Builder dockerClientBuilder;
                final DefaultDockerClient.Builder dockerClientBuilder = DefaultDockerClient.fromEnv();
                if (uri != null || dockerCertPath != null) {
                    LOGGER.debug("Using specified Docker access configuration");

//                    dockerClientBuilder = DefaultDockerClient.builder();

                    if (uri != null) {
                        dockerClientBuilder.uri(uri);
                    }
                    if (dockerCertPath != null) {
                        LOGGER.info("Using Docker certificates at {}", dockerCertPath);
                        final Optional<DockerCertificates> certs = DockerCertificates.builder()
                                .dockerCertPath(dockerCertPath)
                                .build();

                        if (certs.isPresent()) {
                            dockerClientBuilder.dockerCertificates(certs.get());
                        }
                        else {
                            Assert.fail("Given certificates were not loaded");
                        }

                    }
                }
                else {
                    LOGGER.debug("Loading Docker access configuration from environment");
//                    dockerClientBuilder = DefaultDockerClient.fromEnv();
                }

                dockerClient = dockerClientBuilder.build();

                try {
                    final Info info = dockerClient.info();
                    LOGGER.info("Using Docker node {}", info.name());
                } catch (DockerException|InterruptedException e) {
                    if (skipWhenOffline) {
                        Assume.assumeNoException(e);
                    }
                    else {
                        throw e;
                    }
                }

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
