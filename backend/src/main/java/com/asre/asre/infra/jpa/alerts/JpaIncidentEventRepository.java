package com.asre.asre.infra.jpa.alerts;

import com.asre.asre.domain.alerts.IncidentEvent;
import com.asre.asre.domain.alerts.IncidentEventRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class JpaIncidentEventRepository implements IncidentEventRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JpaIncidentEventRepository(@Qualifier("supabaseJdbcTemplate") JdbcTemplate jdbcTemplate,
                                      ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    private RowMapper<IncidentEvent> createRowMapper() {
        return (rs, rowNum) -> {
            try {
                String contentJson = rs.getString("content");
                Map<String, Object> content = null;
                if (contentJson != null && !contentJson.isEmpty()) {
                    content = objectMapper.readValue(contentJson, new TypeReference<Map<String, Object>>() {});
                }
                
                Timestamp timestamp = rs.getTimestamp("timestamp");
                
                return IncidentEvent.builder()
                        .id((UUID) rs.getObject("id"))
                        .incidentId((UUID) rs.getObject("incident_id"))
                        .eventType(IncidentEvent.IncidentEventType.valueOf(rs.getString("event_type")))
                        .content(content)
                        .timestamp(timestamp != null ? timestamp.toInstant() : null)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to map incident event", e);
            }
        };
    }

    @Override
    public IncidentEvent save(IncidentEvent event) {
        if (event.getId() == null) {
            UUID id = UUID.randomUUID();
            Instant now = event.getTimestamp() != null ? event.getTimestamp() : Instant.now();
            
            try {
                String contentJson = event.getContent() != null 
                    ? objectMapper.writeValueAsString(event.getContent()) 
                    : null;
                
                String sql = "INSERT INTO incident_events (id, incident_id, event_type, content, timestamp) " +
                        "VALUES (?, ?, ?::incident_event_type, ?::jsonb, ?)";
                
                jdbcTemplate.update(sql, id, event.getIncidentId(), event.getEventType().name(), 
                        contentJson, Timestamp.from(now));
                
                return IncidentEvent.builder()
                        .id(id)
                        .incidentId(event.getIncidentId())
                        .eventType(event.getEventType())
                        .content(event.getContent())
                        .timestamp(now)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to save incident event", e);
            }
        } else {
            // Update not typically needed for events, but handle if needed
            return event;
        }
    }

    @Override
    public List<IncidentEvent> findByIncidentId(UUID incidentId) {
        String sql = "SELECT id, incident_id, event_type, content, timestamp FROM incident_events " +
                "WHERE incident_id = ? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, createRowMapper(), incidentId);
    }
}

