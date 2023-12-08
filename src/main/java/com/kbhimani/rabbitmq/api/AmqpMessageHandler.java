package com.kbhimani.rabbitmq.api;

import com.kbhimani.rabbitmq.config.ApplicationProperties;
import com.kbhimani.rabbitmq.model.Job;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class AmqpMessageHandler {
    RabbitTemplate rabbitMqTemplate;
    ApplicationProperties.RabbitConfig rabbitConfig;

    @Autowired
    AmqpMessageHandler(RabbitTemplate rabbitMqTemplate, ApplicationProperties applicationProperties) {
        this.rabbitMqTemplate = rabbitMqTemplate;
        this.rabbitConfig = applicationProperties.getRabbitConfig();
    }

    @RabbitListener(queues = {"${application.rabbitConfig.jobRequestQueueName}"}, messageConverter = "messageConverter", errorHandler = "errorHandler")
    public void handleNewJobRequest(@Payload final Job jobRequest, @Headers final Map<String, Object> headers) {
        // handle job request from the queue
        if (jobRequest.getId() % 2 == 0) {
            throw new AmqpRejectAndDontRequeueException("Operation failed for job with id: %d".formatted(jobRequest.getId()));
        }
    }

    @RabbitListener(queues = {"${application.rabbitConfig.jobRequestDLQName}"}, messageConverter = "messageConverter", errorHandler = "errorHandler")
    public void handleDLQRequest(@Payload Message failedMessage) {
        // update the retry counter and drop if exceeded the threshold
        var currRetryCount = Optional.ofNullable((Integer) failedMessage.getMessageProperties().getHeaders().get("x-retries-count")).orElse(1);
        var maxRetryCount = Integer.parseInt(rabbitConfig.getMaxJobRetryCount());

        if (currRetryCount > maxRetryCount) {
            return;
        }

        failedMessage.getMessageProperties().getHeaders().put("x-retries-count", ++currRetryCount);
        rabbitMqTemplate.convertAndSend(rabbitConfig.getExchangeName(), rabbitConfig.getJobDelayedRoutingKey(), failedMessage);
    }
}
