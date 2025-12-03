package com.asre.asre.infra.alerts;

import com.asre.asre.application.alerts.AlertRuleService;
import com.asre.asre.config.RabbitMQConfig;
import com.asre.asre.domain.alerts.AlertRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Scheduler that enqueues alert rule evaluations every minute.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertEvaluationScheduler {

    private final AlertRuleService alertRuleService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Runs every minute to evaluate all active alert rules.
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    public void evaluateActiveRules() {
        try {
            var activeRules = alertRuleService.getActiveRules();
            log.debug("Evaluating {} active alert rules", activeRules.size());

            Instant now = Instant.now();

            for (AlertRule rule : activeRules) {
                try {
                    Map<String, Object> message = new HashMap<>();
                    message.put("ruleId", rule.getId().toString());
                    message.put("projectId", rule.getProjectId().toString());
                    message.put("serviceId", rule.getServiceId().toString());
                    message.put("metricName", rule.getMetricName());
                    message.put("evaluationTime", now.toString());

                    String messageJson = objectMapper.writeValueAsString(message);
                    rabbitTemplate.convertAndSend(RabbitMQConfig.ALERTS_EVALUATE_QUEUE, messageJson);
                    
                } catch (Exception e) {
                    log.error("Failed to enqueue evaluation for rule {}: {}", rule.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to schedule alert evaluations: {}", e.getMessage(), e);
        }
    }
}

