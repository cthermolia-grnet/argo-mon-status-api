package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_Tenant")
@Getter @Setter
public class Tenant {

    @Id
    public String id;

    @NotNull
    @Column(name = "name", unique = true)
    public String name;

    @Column(name = "email")
    public String email;

    @Column(name = "website")
    public String website;

    @Column(name = "description")
    public String description;

    @NotNull
    @Column(name = "updated_by")
    public String updatedBy;

    @Column(name = "image")
    public String image;

    @NotNull
    @Column(name = "created_at")
    public Timestamp createdAt;

    @Column(name = "updated_at")
    public Timestamp updatedAt;

    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Getter
    private String metadata;

    @Column(name = "status", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Getter
    private String status;

    @Column(name = "node")
    @Getter
    private Boolean node;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "tenant_contact",
            joinColumns = @JoinColumn(name = "tenant_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private Set<Contact> contacts = new HashSet<>();

    @OneToMany(
            mappedBy = "tenant",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<TenantProjectJunction> tenantProjects = new HashSet<>();
    @PrePersist
    public void prePersist() {
        if (status == null || status.trim().isEmpty()) {
            status = "{}";
        }
    }
}