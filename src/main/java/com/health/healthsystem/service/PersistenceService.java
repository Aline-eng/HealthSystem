package com.health.healthsystem.service;

import com.health.healthsystem.model.*;
import com.health.healthsystem.model.User.Role;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handles all file I/O.
 *
 * File layout (all under DATA_DIR):
 *   users.properties          username=hashedPassword:ROLE
 *   <username>_patients.csv   id,name
 *   <username>_doctors.csv    id,name,specialization
 *   <username>_appointments.csv  id,patientId,doctorId,dateTime
 *   <username>_records.csv    id,patientId,data,createdAt
 */
public class PersistenceService {

    private static final Path DATA_DIR = Paths.get(System.getProperty("user.home"), ".healthsystem");
    private static final Path USERS_FILE = DATA_DIR.resolve("users.properties");
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    static {
        try { Files.createDirectories(DATA_DIR); }
        catch (IOException e) { throw new RuntimeException("Cannot create data directory.", e); }
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    /** Returns map of username -> User loaded from file. */
    public Map<String, User> loadUsers() {
        Map<String, User> map = new LinkedHashMap<>();
        if (!Files.exists(USERS_FILE)) return map;
        Properties props = loadProperties(USERS_FILE);
        for (String username : props.stringPropertyNames()) {
            String[] parts = props.getProperty(username).split(":", 2);
            if (parts.length == 2)
                map.put(username, new User(username, parts[0], Role.valueOf(parts[1])));
        }
        return map;
    }

    /** Persists the full users map to file. */
    public void saveUsers(Map<String, User> users) {
        Properties props = new Properties();
        users.forEach((u, user) -> props.setProperty(u, user.getHashedPassword() + ":" + user.getRole().name()));
        saveProperties(props, USERS_FILE, "Healthcare System - Users");
    }

    // ── Patients ──────────────────────────────────────────────────────────────

    public List<String[]> loadPatients(String username) {
        return loadCsv(userFile(username, "patients"));
    }

    public void savePatients(String username, List<Patient> patients) {
        List<String[]> rows = new ArrayList<>();
        for (Patient p : patients) rows.add(new String[]{p.getId(), p.getName()});
        saveCsv(userFile(username, "patients"), rows);
    }

    // ── Doctors ───────────────────────────────────────────────────────────────

    public List<String[]> loadDoctors(String username) {
        return loadCsv(userFile(username, "doctors"));
    }

    public void saveDoctors(String username, List<Doctor> doctors) {
        List<String[]> rows = new ArrayList<>();
        for (Doctor d : doctors) rows.add(new String[]{d.getId(), d.getName(), d.getSpecialization()});
        saveCsv(userFile(username, "doctors"), rows);
    }

    // ── Appointments ──────────────────────────────────────────────────────────

    public List<String[]> loadAppointments(String username) {
        return loadCsv(userFile(username, "appointments"));
    }

    /**
     * Saves appointments. Each row: id, patientId, doctorId, dateTime.
     * Caller resolves patient/doctor objects from their respective lists.
     */
    public void saveAppointments(String username, List<Appointment> appointments) {
        List<String[]> rows = new ArrayList<>();
        for (Appointment a : appointments)
            rows.add(new String[]{
                a.getId(),
                a.getPatient().getId(),
                a.getDoctor().getId(),
                a.getDateTime().format(DT)
            });
        saveCsv(userFile(username, "appointments"), rows);
    }

    // ── Medical Records ───────────────────────────────────────────────────────

    public List<String[]> loadRecords(String username) {
        return loadCsv(userFile(username, "records"));
    }

    /** Saves all records across all patients. Row: recordId, patientId, data, createdAt */
    public void saveRecords(String username, List<Patient> patients) {
        List<String[]> rows = new ArrayList<>();
        for (Patient p : patients)
            for (MedicalRecords<?> r : p.getMedicalRecords())
                rows.add(new String[]{
                    r.getId(),
                    p.getId(),
                    r.getData().toString(),
                    r.getCreatedAt().format(DT)
                });
        saveCsv(userFile(username, "records"), rows);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private Path userFile(String username, String type) {
        return DATA_DIR.resolve(username + "_" + type + ".csv");
    }

    private List<String[]> loadCsv(Path path) {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(path)) return rows;
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) rows.add(line.split(",", -1));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
        return rows;
    }

    private void saveCsv(Path path, List<String[]> rows) {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            for (String[] row : rows) {
                bw.write(String.join(",", escapeCsv(row)));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + path, e);
        }
    }

    private String[] escapeCsv(String[] fields) {
        String[] escaped = new String[fields.length];
        for (int i = 0; i < fields.length; i++)
            escaped[i] = fields[i].replace(",", "\\,");
        return escaped;
    }

    private Properties loadProperties(Path path) {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) { props.load(in); }
        catch (IOException e) { throw new RuntimeException("Failed to read: " + path, e); }
        return props;
    }

    private void saveProperties(Properties props, Path path, String comment) {
        try (OutputStream out = Files.newOutputStream(path)) { props.store(out, comment); }
        catch (IOException e) { throw new RuntimeException("Failed to write: " + path, e); }
    }
}
