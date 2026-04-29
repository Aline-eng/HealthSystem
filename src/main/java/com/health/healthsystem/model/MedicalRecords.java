package com.health.healthsystem.model;

import java.time.LocalDateTime;

public class MedicalRecords<T> {
    private final String id;
    private final T data;
    private final LocalDateTime createdAt;

    public MedicalRecords(String id, T data) {
        this(id, data, LocalDateTime.now());
    }

    public MedicalRecords(String id, T data, LocalDateTime createdAt) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Medical record ID cannot be empty.");
        if (data == null) throw new IllegalArgumentException("Medical record data cannot be null.");
        if (data instanceof String && ((String) data).trim().isEmpty())
            throw new IllegalArgumentException("Medical record data cannot be empty.");
        this.id = id.trim();
        this.data = data;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public T getData() { return data; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
