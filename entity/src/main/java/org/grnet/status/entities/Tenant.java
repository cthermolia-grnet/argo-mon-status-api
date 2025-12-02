package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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

    @ManyToMany(cascade = CascadeType.PERSIST)  // cascade persist so saving Tenant saves new Contacts
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

}