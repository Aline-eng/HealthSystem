package com.health.healthsystem.repository;

import com.health.healthsystem.model.Patient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientRepository {
    private final Map<String, Patient> patients = new HashMap<>();

    public void addPatient(Patient patient) {
        if (patient == null) throw new IllegalArgumentException("Patient cannot be null.");
        if (patients.containsKey(patient.getId())) throw new IllegalArgumentException("Patient ID already exists.");
        patients.put(patient.getId(), patient);
    }

    public Patient getPatient(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        return patients.get(id.trim());
    }

    public boolean exists(String id) { return getPatient(id) != null; }

    public List<Patient> getAllPatients() { return new ArrayList<>(patients.values()); }
}
