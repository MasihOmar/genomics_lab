package com.genomicslab.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class ResultsModel {
    private final StringProperty resultId;
    private final StringProperty testId;
    private final StringProperty patientId;
    private final StringProperty resultDetails;
    private final ObjectProperty<LocalDateTime> resultDate;

    public ResultsModel(String resultId, String testId, String patientId,  String resultDetails, LocalDateTime resultDate) {
        this.resultId = new SimpleStringProperty(resultId);
        this.testId = new SimpleStringProperty(testId);
        this.patientId = new SimpleStringProperty(patientId);
        this.resultDetails = new SimpleStringProperty(resultDetails);
        this.resultDate = new SimpleObjectProperty<>(resultDate);
    }

    public String getResultId() {
        return resultId.get();
    }


    public StringProperty resultIdProperty() {
        return resultId;
    }

    public String getTestId() {
        return testId.get();
    }

    public void setTestId(String testId) {
        this.testId.set(testId);
    }

    public StringProperty testIdProperty() {
        return testId;
    }

    public String getPatientId() {
        return patientId.get();
    }

    public StringProperty patientIdProperty() {
        return patientId;
    }


    public String getResultDetails() {
        return resultDetails.get();
    }


    public StringProperty resultDetailsProperty() {
        return resultDetails;
    }

    public LocalDateTime getResultDate() {
        return resultDate.get();
    }


    public ObjectProperty<LocalDateTime> resultDateProperty() {
        return resultDate;
    }
}


