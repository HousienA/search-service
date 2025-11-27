package com.fullstack.searchservice.controllers;

import com.fullstack.searchservice.entity.Patient;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/search/patients")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated  // Requires valid Keycloak token
public class PatientSearch {

    /**
     * Search patients with multiple filters
     *
     * Examples:
     * - GET /api/search/patients?name=Anna
     * - GET /api/search/patients?pnr=1990
     * - GET /api/search/patients?name=Anna&pnr=1990
     * - GET /api/search/patients?condition=diabetes
     * - GET /api/search/patients (returns all)
     */
    @GET
    public Uni<List<Patient>> searchPatients(
            @QueryParam("name") String name,
            @QueryParam("pnr") String pnr,
            @QueryParam("condition") String condition
    ) {
        // Priority: condition > name+pnr > pnr > name > all

        if (condition != null && !condition.isBlank()) {
            return Patient.findByConditionName(condition);
        }

        if (name != null && !name.isBlank() && pnr != null && !pnr.isBlank()) {
            return Patient.searchByNameAndPnr(name, pnr);
        }

        if (pnr != null && !pnr.isBlank()) {
            return Patient.searchByPersonalNumber(pnr);
        }

        if (name != null && !name.isBlank()) {
            return Patient.searchByName(name);
        }

        // No filters = return all patients
        return Patient.listAll();
    }
}
