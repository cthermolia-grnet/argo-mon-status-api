package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;


import java.sql.Timestamp;

@Entity
@Table(name = "t_Status_Page")
@Getter
@Setter
public class StatusPage {

    @Id
    @UuidGenerator
    private String id;

    @NotNull
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "slug")
    private String slug;

    @NotNull
    @Column(name = "user_id")
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @NotNull
    @Column(name = "report")
    private String report;

    @Column(name = "config", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Getter
    private String config;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
