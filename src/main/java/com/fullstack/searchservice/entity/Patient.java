package com.fullstack.searchservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "patients")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Patient extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "full_name", nullable = false)
    public String fullName;

    @Column(name = "personal_number", nullable = false, unique = true)
    public String personalNumber;

    public String email;
    public String phone;

    @Column(name = "auth_id", unique = true)
    public String authId;

    // ============ SEARCH METHODS ============

    /**
     * Search by name (case-insensitive, partial match)
     */
    public static Uni<List<Patient>> searchByName(String name) {
        return list("LOWER(fullName) LIKE LOWER(?1)", "%" + name + "%");
    }

    /**
     * Search by personal number (partial match)
     */
    public static Uni<List<Patient>> searchByPersonalNumber(String pnr) {
        return list("personalNumber LIKE ?1", "%" + pnr + "%");
    }

    /**
     * Search by both name AND personal number
     */
    public static Uni<List<Patient>> searchByNameAndPnr(String name, String pnr) {
        return list(
                "LOWER(fullName) LIKE LOWER(?1) AND personalNumber LIKE ?2",
                "%" + name + "%",
                "%" + pnr + "%"
        );
    }

    /**
     * Find patients by condition name (requires join)
     */
    public static Uni<List<Patient>> findByConditionName(String conditionName) {
        return find(
                "SELECT DISTINCT p FROM Patient p " +
                        "JOIN Condition c ON c.patientId = p.id " +
                        "WHERE LOWER(c.conditionName) LIKE LOWER(?1)",
                "%" + conditionName + "%"
        ).list();
    }

    /**
     * Find patients by practitioner ID
     */
    public static Uni<List<Patient>> findByPractitionerId(Long practitionerId) {
        // Note: This assumes you have a primary_practitioner_id column
        // If not, adjust based on your schema
        return find(
                "SELECT p FROM Patient p WHERE p.id IN " +
                        "(SELECT e.patientId FROM Encounter e WHERE e.practitionerId = ?1)",
                practitionerId
        ).list();
    }
}
