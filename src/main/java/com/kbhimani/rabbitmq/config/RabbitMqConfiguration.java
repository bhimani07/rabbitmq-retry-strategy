package com.kbhimani.rabbitmq.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbhimani.rabbitmq.api.AmqpErrorHandler;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class RabbitMqConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private final ApplicationProperties.RabbitConfig rabbitConfig;

    @Autowired
    public RabbitMqConfiguration(ApplicationProperties rabbitConfig) {
        this.rabbitConfig = rabbitConfig.getRabbitConfig();
    }

    @Bean
    @Qualifier("topicExchange")
    TopicExchange topicExchange() {
        return ExchangeBuilder.topicExchange(rabbitConfig.getExchangeName()).durable(true).build();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory,
                                         final MessageConverter messageConverter) {
        final var rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    @DependsOn({"objectMapper"})
    public MessageConverter messageConverter(final ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitListenerErrorHandler amqpErrorHandler() {
        return applicationContext.getBean(AmqpErrorHandler.class);
    }

    @Bean
    @Qualifier("jobRequestQueue")
    @DependsOn({"topicExchange", "jobRequestDLQQueue"})
    public Queue jobRequestQueue() {
        return QueueBuilder
                .durable(rabbitConfig.getJobRequestQueueName())
                .deadLetterExchange(rabbitConfig.getExchangeName())
                .deadLetterRoutingKey(rabbitConfig.getJobRequestDLQRoutingKey())
                .build();
    }

    @Bean
    public Binding jobRequestQueueBinding() {
        return new Binding(rabbitConfig.getJobRequestQueueName(), Binding.DestinationType.QUEUE,
                rabbitConfig.getExchangeName(), rabbitConfig.getJobRequestRoutingKey(), null);
    }

    @Bean
    @Qualifier("jobRequestDLQQueue")
    @DependsOn({"topicExchange"})
    public Queue jobRequestDLQQueue() {
        return QueueBuilder
                .durable(rabbitConfig.getJobRequestDLQName())
                .build();
    }

    @Bean
    public Binding jobRequestDLQQueueBinding() {
        return new Binding(rabbitConfig.getJobRequestDLQName(), Binding.DestinationType.QUEUE,
                rabbitConfig.getExchangeName(), rabbitConfig.getJobRequestDLQRoutingKey(), null);
    }

    @Bean
    @Qualifier("jobDelayedQueue")
    @DependsOn({"topicExchange"})
    public Queue jobDelayedQueue() {
        return QueueBuilder.durable(rabbitConfig.getJobDelayedQueueName())
                .deadLetterExchange(rabbitConfig.getExchangeName())
                .deadLetterRoutingKey(rabbitConfig.getJobRequestRoutingKey())
                .ttl(Integer.parseInt(rabbitConfig.getJobDelayedQueueTTL()))
                .build();
    }

    @Bean
    public Binding delayedQueueBinding() {
        return new Binding(rabbitConfig.getJobDelayedQueueName(), Binding.DestinationType.QUEUE,
                rabbitConfig.getExchangeName(), rabbitConfig.getJobDelayedRoutingKey(), null);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
