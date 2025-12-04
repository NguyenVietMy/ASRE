package com.asre.asre.infra.rabbitmq;

import com.asre.asre.domain.ingestion.MessagePublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMessagePublisher implements MessagePublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishMetricsMessage(String queueName, String message) {
        rabbitTemplate.convertAndSend(queueName, message);
    }

    @Override
    public void publishLogsMessage(String queueName, String message) {
        rabbitTemplate.convertAndSend(queueName, message);
    }
}


