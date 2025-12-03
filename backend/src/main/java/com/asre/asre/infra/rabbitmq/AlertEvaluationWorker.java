package com.asre.asre.infra.rabbitmq;

import com.asre.asre.application.alerts.AlertEvaluationService;
import com.asre.asre.application.alerts.EvaluateRuleCommand;
import com.asre.asre.config.RabbitMQConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ worker that processes alert rule evaluation requests.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertEvaluationWorker {

    private final AlertEvaluationService alertEvaluationService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.ALERTS_EVALUATE_QUEUE)
    public void handleEvaluationMessage(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {});
            
            EvaluateRuleCommand command = EvaluateRuleCommand.builder()
                    .ruleId(UUID.fromString((String) payload.get("ruleId")))
                    .projectId(UUID.fromString((String) payload.get("projectId")))
                    .serviceId(UUID.fromString((String) payload.get("serviceId")))
                    .metricName((String) payload.get("metricName"))
                    .evaluationTime(payload.get("evaluationTime") != null 
                        ? Instant.parse((String) payload.get("evaluationTime")) 
                        : Instant.now())
                    .build();

            alertEvaluationService.evaluateRule(command);
            
        } catch (Exception e) {
            log.error("Failed to evaluate alert rule: {}", e.getMessage(), e);
            // Send to DLQ
            try {
                rabbitTemplate.send(RabbitMQConfig.ALERTS_EVALUATE_QUEUE + ".dlq", 
                    org.springframework.amqp.core.MessageBuilder.withBody(message.getBytes()).build());
            } catch (Exception dlqError) {
                log.error("Failed to send to DLQ: {}", dlqError.getMessage());
            }
        }
    }
}

