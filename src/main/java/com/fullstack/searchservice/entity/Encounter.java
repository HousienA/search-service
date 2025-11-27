package com.fullstack.searchservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "encounters")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Encounter extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "encounter_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime encounterDate;

    public String diagnosis;

    @Column(length = 5000)
    public String notes;

    @Column(name = "patient_id")
    public Long patientId;

    @Column(name = "practitioner_id")
    public Long practitionerId;

    // We'll add patient name on the fly in the resource
    @Transient
    public String patientName;

    /**
     * Find encounters by practitioner and specific date
     */
    public static Uni<List<Encounter>> findByPractitionerAndDate(Long practitionerId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return list(
                "practitionerId = ?1 AND encounterDate BETWEEN ?2 AND ?3 ORDER BY encounterDate DESC",
                practitionerId,
                startOfDay,
                endOfDay
        );
    }

    /**
     * Find all encounters for a practitioner
     */
    public static Uni<List<Encounter>> findByPractitioner(Long practitionerId) {
        return list("practitionerId = ?1 ORDER BY encounterDate DESC", practitionerId);
    }
}
