module com.health.healthsystem {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.health.healthsystem to javafx.fxml;
    opens com.health.healthsystem.controller to javafx.fxml;

    exports com.health.healthsystem;
    exports com.health.healthsystem.controller;
    exports com.health.healthsystem.model;
    exports com.health.healthsystem.service;
    exports com.health.healthsystem.repository;
    exports com.health.healthsystem.exception;
    exports com.health.healthsystem.util;
}
