package com.kbhimani.rabbitmq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final RabbitConfig rabbitConfig = new RabbitConfig();

    @Getter
    @Setter
    public static class RabbitConfig {
        private String exchangeName;
        private String jobRequestQueueName;
        private String jobRequestRoutingKey;

        private String jobRequestDLQName;
        private String jobRequestDLQRoutingKey;
        private String maxJobRetryCount;

        private String jobDelayedQueueName;
        private String jobDelayedRoutingKey;
        private String jobDelayedQueueTTL;
    }

}
