package com.health.healthsystem.service;

import com.health.healthsystem.model.*;
import com.health.healthsystem.model.User.Role;
import com.health.healthsystem.model.User.Gender;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PersistenceService {
    private static final Path DATA_DIR = Paths.get(System.getProperty("user.home"), ".healthsystem");
    private static final Path USERS_FILE = DATA_DIR.resolve("users.properties");
    private static final Path APPTS_FILE = DATA_DIR.resolve("appointments.csv");
    private static final Path RECORDS_FILE = DATA_DIR.resolve("records.csv");
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    static {
        try { Files.createDirectories(DATA_DIR); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public Map<String, User> loadUsers() {
        Map<String, User> map = new LinkedHashMap<>();
        if (!Files.exists(USERS_FILE)) return map;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(USERS_FILE)) { props.load(in); } catch (IOException e) { return map; }

        for (String username : props.stringPropertyNames()) {
            String[] p = props.getProperty(username).split(":", -1);
            if (p.length >= 6) {
                try {
                    User user = new User(username, p[0], Role.valueOf(p[1]), p[2], LocalDate.parse(p[3]), Gender.valueOf(p[4]), "null".equals(p[5]) ? null : p[5]);
                    map.put(username, user);
                } catch (Exception ignored) {}
            }
        }
        loadMedicalRecords(map);
        return map;
    }

    public void saveUsers(Map<String, User> users) {
        Properties props = new Properties();
        users.forEach((u, user) -> {
            String value = String.join(":", user.getHashedPassword(), user.getRole().name(), user.getFullName(), user.getBirthDate().toString(), user.getGender().name(), (user.getSpecialization() == null ? "null" : user.getSpecialization()));
            props.setProperty(u, value);
        });
        try (OutputStream out = Files.newOutputStream(USERS_FILE)) { props.store(out, null); } catch (IOException ignored) {}
        saveMedicalRecords(users);
    }

    private void loadMedicalRecords(Map<String, User> users) {
        if (!Files.exists(RECORDS_FILE)) return;
        try (BufferedReader br = Files.newBufferedReader(RECORDS_FILE)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] r = line.split(",", -1);
                if (r.length >= 4) {
                    // If the description has commas, split results in > 4 parts.
                    // The ID is r[0], the Patient is r[1], and the Date is the LAST part.
                    String id = r[0];
                    String username = r[1];
                    String dateStr = r[r.length - 1]; // Date is always at the end

                    // Join everything in between as the description
                    StringBuilder dataBuilder = new StringBuilder();
                    for (int i = 2; i < r.length - 1; i++) {
                        dataBuilder.append(r[i]).append(i == r.length - 2 ? "" : ",");
                    }

                    User patient = users.get(username);
                    if (patient != null) {
                        try {
                            LocalDateTime dt = LocalDateTime.parse(dateStr, DT);
                            patient.getMedicalRecords().add(new MedicalRecords<>(id, dataBuilder.toString(), dt));
                        } catch (Exception e) {
                            System.err.println("Skipping malformed record date: " + dateStr);
                        }
                    }
                }
            }
        } catch (IOException ignored) {}
    }


    private void saveMedicalRecords(Map<String, User> users) {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(RECORDS_FILE))) {
            users.values().forEach(u -> u.getMedicalRecords().forEach(r ->
                    pw.println(String.join(",", r.getId(), u.getUsername(), r.getData().toString(), r.getCreatedAt().format(DT)))
            ));
        } catch (IOException ignored) {}
    }

    public List<Appointment> loadAllAppointments(Map<String, User> userMap) {
        List<Appointment> list = new ArrayList<>();
        if (!Files.exists(APPTS_FILE)) return list;
        try (BufferedReader br = Files.newBufferedReader(APPTS_FILE)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] r = line.split(",", -1);
                if (r.length >= 4) {
                    User p = userMap.get(r[1]); User d = userMap.get(r[2]);
                    if (p != null && d != null) list.add(new Appointment(r[0], p, d, LocalDateTime.parse(r[3], DT)));
                }
            }
        } catch (IOException ignored) {}
        return list;
    }

    public void saveAppointments(List<Appointment> appointments) {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(APPTS_FILE))) {
            appointments.forEach(a -> pw.println(String.join(",", a.getId(), a.getPatient().getUsername(), a.getDoctor().getUsername(), a.getDateTime().format(DT))));
        } catch (IOException ignored) {}
    }
}