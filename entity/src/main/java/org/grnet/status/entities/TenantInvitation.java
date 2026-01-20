package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.grnet.status.enums.InvitationStatus;

import java.time.Instant;

@Entity
@Table(name = "t_Tenant_Invitation")
public class TenantInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "tenant_id")
    public Tenant tenant;

    @NotNull
    @Column
    public String email;

    @NotNull
    @Column
    public String role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column
    public InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "created_at")
    public Instant createdAt = Instant.now();

    @Column(name = "responded_at")
    public Instant respondedAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "responded_by")
    public String respondedBy;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

