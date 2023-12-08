package com.kbhimani.rabbitmq.api;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;

@Component(value = "errorHandler")
public class AmqpErrorHandler implements RabbitListenerErrorHandler {

    @Override
    public Object handleError(Message amqpMessage, org.springframework.messaging.Message<?> message,
                              ListenerExecutionFailedException exception) throws Exception {
        if (exception.getCause() instanceof AmqpRejectAndDontRequeueException) {
            throw (AmqpRejectAndDontRequeueException) exception.getCause();
        }
        return null;
    }

}
