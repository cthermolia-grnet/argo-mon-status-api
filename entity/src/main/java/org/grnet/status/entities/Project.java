package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "t_Project")
@Getter
@Setter
public class Project {

    @Id
    @Column(name = "id")
    private String id;

    @NotNull
    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "start_date")
    private Timestamp startDate;

    @Column(name = "end_date")
    private Timestamp endDate;

    @Column(name = "sustainability_end_date")
    private Timestamp sustainabilityEndDate;

    @Column(name = "data_retention_policy")
    private String dataRetentionPolicy;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @OneToMany(
            mappedBy = "project",
            cascade = {},        // No cascade, or specify only what you want (e.g., PERSIST, MERGE)
            orphanRemoval = false
    )
    private Set<TenantProjectJunction> tenantProjects = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = "proj-" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toLowerCase();
        }

        if (createdAt == null) {
            createdAt = Timestamp.from(Instant.now());
        }
    }
}
