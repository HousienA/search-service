package com.fullstack.searchservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "practitioners")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Practitioner extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "full_name", nullable = false)
    public String fullName;

    public String email;
    public String phone;

    @Column(name = "auth_id", unique = true)
    public String authId;

    /**
     * Search by name (case-insensitive, partial match)
     */
    public static Uni<List<Practitioner>> searchByName(String name) {
        return list("LOWER(fullName) LIKE LOWER(?1)", "%" + name + "%");
    }
}
