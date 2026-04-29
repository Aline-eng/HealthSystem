package com.health.healthsystem.model;

import com.health.healthsystem.util.InputValidator;

public class Doctor {
    private final String id;
    private final String name;
    private final String specialization;

    public Doctor(String id, String name, String specialization) {
        this.id = InputValidator.validateId(id, "Doctor ID");
        this.name = InputValidator.validateName(name, "Doctor name");
        this.specialization = InputValidator.validateSpecialization(specialization, "Doctor specialization");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }

    @Override
    public String toString() { return id + " | " + name + " | " + specialization; }
}
