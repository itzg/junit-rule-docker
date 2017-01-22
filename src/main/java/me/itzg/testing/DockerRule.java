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
 * Created by geoff on 1/21/17.
 */
public class DockerRule implements TestRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerRule.class);

    private final String image;
    private String[] command;
    private DockerClient dockerClient;
    private ContainerCreation container;

    public DockerRule(String image) {
        this.image = image;
    }

    public DockerRule command(String... command) {
        this.command = command;
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

                LOGGER.info("Starting container {}", container.id());

                dockerClient.startContainer(container.id());

                statement.evaluate();

                LOGGER.info("Stopping container {}", container.id());
                dockerClient.killContainer(container.id());
                dockerClient.removeContainer(container.id());

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
