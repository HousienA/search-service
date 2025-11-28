package com.fullstack.searchservice.controllers;

import com.fullstack.searchservice.entity.Encounter;
import com.fullstack.searchservice.entity.Patient;
import com.fullstack.searchservice.entity.Practitioner;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/search/practitioners")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class PractitionerSearch {

    /**
     * Search practitioners by name
     * Example: GET /api/search/practitioners?name=Andersson
     */
    @GET
    public Uni<List<Practitioner>> searchPractitioners(
            @QueryParam("name") String name
    ) {
        if (name != null && !name.isBlank()) {
            return Practitioner.searchByName(name);
        }

        return Practitioner.listAll();
    }

    /**
     * Get all patients for a specific practitioner
     * Example: GET /api/search/practitioners/1/patients
     */
    @GET
    @Path("/{id}/patients")
    public Uni<List<Patient>> getPractitionerPatients(
            @PathParam("id") Long practitionerId
    ) {
        return Patient.findByPractitionerId(practitionerId);
    }

    /**
     * Get encounters for a practitioner (optionally filtered by date)
     * Examples:
     * - GET /api/search/practitioners/1/encounters
     * - GET /api/search/practitioners/1/encounters?date=2024-11-27
     */
    @GET
    @Path("/{id}/encounters")
    public Uni<List<Encounter>> getPractitionerEncounters(
            @PathParam("id") Long practitionerId,
            @QueryParam("date") String dateStr
    ) {
        Uni<List<Encounter>> encountersUni;

        if (dateStr != null && !dateStr.isBlank()) {
            LocalDate date = LocalDate.parse(dateStr);
            encountersUni = Encounter.findByPractitionerAndDate(practitionerId, date);
        } else {
            encountersUni = Encounter.findByPractitioner(practitionerId);
        }

        // Enrich encounters with patient names
        return encountersUni.flatMap(encounters -> {
            // For each encounter, fetch the patient name
            List<Uni<Encounter>> enrichedEncounters = encounters.stream()
                    .map(enc -> Patient.<Patient>findById(enc.patientId)
                            .map(patient -> {
                                enc.patientName = patient != null ? patient.fullName : "Unknown";
                                return enc;
                            })
                    )
                    .collect(Collectors.toList());

            return Uni.combine().all().unis(enrichedEncounters)
                    .with(list -> list.stream()
                            .map(obj -> (Encounter) obj)
                            .collect(Collectors.toList())
                    );
        });
    }
}
