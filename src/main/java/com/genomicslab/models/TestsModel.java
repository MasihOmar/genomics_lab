
package com.genomicslab.models;

import javafx.beans.property.*;

public class TestsModel {
    private final StringProperty testId;
    private final StringProperty name;
    private final StringProperty description;
    private final DoubleProperty price;

    public TestsModel(String testId, String name, String description, double price) {
        this.testId = new SimpleStringProperty(testId);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.price = new SimpleDoubleProperty(price);
    }

    // Getters and Setters for properties
    public String getTestId() {
        return testId.get();
    }

    public void setTestId(String testId) {
        this.testId.set(testId);
    }

    public StringProperty testIdProperty() {
        return testId;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public DoubleProperty priceProperty() {
        return price;
    }

}


