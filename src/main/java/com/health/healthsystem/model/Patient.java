package com.health.healthsystem.model;

import com.health.healthsystem.util.InputValidator;
import java.util.ArrayList;
import java.util.List;

public class Patient {
    private final String id;
    private final String name;
    private final List<MedicalRecords<?>> medicalRecords;

    public Patient(String id, String name) {
        this.id = InputValidator.validateId(id, "Patient ID");
        this.name = InputValidator.validateName(name, "Patient name");
        this.medicalRecords = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<MedicalRecords<?>> getMedicalRecords() { return medicalRecords; }

    public void addMedicalRecord(MedicalRecords<?> record) {
        if (record == null) throw new IllegalArgumentException("Medical record cannot be null.");
        medicalRecords.add(record);
    }

    @Override
    public String toString() { return id + " | " + name; }
}
