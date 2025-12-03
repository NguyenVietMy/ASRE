package com.asre.asre.infra.jpa.alerts;

import com.asre.asre.domain.alerts.IncidentNote;
import com.asre.asre.domain.alerts.IncidentNoteRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class JpaIncidentNoteRepository implements IncidentNoteRepository {

    private final JdbcTemplate jdbcTemplate;

    public JpaIncidentNoteRepository(@Qualifier("supabaseJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<IncidentNote> createRowMapper() {
        return (rs, rowNum) -> {
            Timestamp createdAt = rs.getTimestamp("created_at");
            return IncidentNote.builder()
                    .id((UUID) rs.getObject("id"))
                    .incidentId((UUID) rs.getObject("incident_id"))
                    .authorUserId((UUID) rs.getObject("author_user_id"))
                    .content(rs.getString("content"))
                    .createdAt(createdAt != null ? createdAt.toInstant() : null)
                    .build();
        };
    }

    @Override
    public IncidentNote save(IncidentNote note) {
        if (note.getId() == null) {
            UUID id = UUID.randomUUID();
            Instant now = note.getCreatedAt() != null ? note.getCreatedAt() : Instant.now();
            
            String sql = "INSERT INTO incident_notes (id, incident_id, author_user_id, content, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
            
            jdbcTemplate.update(sql, id, note.getIncidentId(), note.getAuthorUserId(), 
                    note.getContent(), Timestamp.from(now));
            
            return IncidentNote.builder()
                    .id(id)
                    .incidentId(note.getIncidentId())
                    .authorUserId(note.getAuthorUserId())
                    .content(note.getContent())
                    .createdAt(now)
                    .build();
        } else {
            // Notes are typically immutable, but handle update if needed
            return note;
        }
    }

    @Override
    public List<IncidentNote> findByIncidentId(UUID incidentId) {
        String sql = "SELECT id, incident_id, author_user_id, content, created_at FROM incident_notes " +
                "WHERE incident_id = ? ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, createRowMapper(), incidentId);
    }
}

