package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_project", uniqueConstraints = { @UniqueConstraint(columnNames = {"tenant_id", "project_id"})})
public class TenantProjectJunction {

    @Id
    @Column(length = 36)
    public String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "tenant_id")
    public Tenant tenant;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "project_id")
    public Project project;

    @Column(name = "created_at")
    public Timestamp createdAt;

    @Column(name = "created_by")
    public String createdBy;

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Timestamp.from(Instant.now());
        }
    }
}
