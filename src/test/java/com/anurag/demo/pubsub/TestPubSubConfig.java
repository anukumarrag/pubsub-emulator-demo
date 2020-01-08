package com.anurag.demo.pubsub;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.autoconfigure.pubsub.GcpPubSubProperties;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

/**
 * Creates the topic and subscription in GCP emulator. If emulator is not running, this will hang.
 */
@Component
public class TestPubSubConfig {

	@Autowired
    private GcpPubSubProperties gcpPubSubProperties;
    private GenericContainer pubsubContainer;
    
    @PostConstruct
    void createResources() {
        int pubsubPort = 8085;
        pubsubContainer =
            new FixedHostPortGenericContainer("google/cloud-sdk")
                .withFixedExposedPort(pubsubPort, pubsubPort)
                .withExposedPorts(pubsubPort)
                .withCommand(
                    "/bin/sh",
                    "-c",
                    String.format(
                        "gcloud beta emulators pubsub start --project %s --host-port=0.0.0.0:%d",
                        gcpPubSubProperties.getProjectId(), pubsubPort)
                )
                .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*started.*$"));

        pubsubContainer.start();
    }

    @PreDestroy
    void destroyResources() {
        pubsubContainer.stop();
    }
}
