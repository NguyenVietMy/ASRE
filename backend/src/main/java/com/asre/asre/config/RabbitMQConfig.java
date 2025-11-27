package com.asre.asre.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String METRICS_INGEST_QUEUE = "metrics.ingest";
    public static final String LOGS_INGEST_QUEUE = "logs.ingest";
    public static final String METRICS_DLQ = "metrics.dlq";
    public static final String LOGS_DLQ = "logs.dlq";
    public static final String ALERTS_EVALUATE_QUEUE = "alerts.evaluate";

    @Bean
    public Queue metricsIngestQueue() {
        return QueueBuilder.durable(METRICS_INGEST_QUEUE).build();
    }

    @Bean
    public Queue logsIngestQueue() {
        return QueueBuilder.durable(LOGS_INGEST_QUEUE).build();
    }

    @Bean
    public Queue metricsDlq() {
        return QueueBuilder.durable(METRICS_DLQ).build();
    }

    @Bean
    public Queue logsDlq() {
        return QueueBuilder.durable(LOGS_DLQ).build();
    }

    @Bean
    public Queue alertsEvaluateQueue() {
        return QueueBuilder.durable(ALERTS_EVALUATE_QUEUE).build();
    }

    @Bean
    public MessageConverter messageConverter() {
        // Using SimpleMessageConverter for JSON strings
        // In production, consider using Jackson2JsonMessageConverter with proper
        // configuration
        return new SimpleMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(100);
        return factory;
    }
}
