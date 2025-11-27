package com.fullstack.searchservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "conditions")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "condition_name", nullable = false)
    public String conditionName;

    public String description;

    @Column(name = "patient_id")
    public Long patientId;
}
