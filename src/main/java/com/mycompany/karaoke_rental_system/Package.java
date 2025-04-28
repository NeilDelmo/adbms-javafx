
package com.mycompany.karaoke_rental_system;

import java.sql.Timestamp;
import javafx.beans.property.*;

public class Package {
    private final IntegerProperty packageId = new SimpleIntegerProperty();
    private final StringProperty name =  new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty bundlePrice = new SimpleDoubleProperty();
    private final StringProperty status  = new SimpleStringProperty();
    private final IntegerProperty created_by =  new SimpleIntegerProperty();
    private ObjectProperty<Timestamp> created_at = new SimpleObjectProperty<>();
    private final IntegerProperty updated_by = new SimpleIntegerProperty();

    // Constructor
    public Package(int packageId, String name, String description, double bundlePrice, String status, int created_by, Timestamp created_at, int updated_by) {
        this.packageId.set(packageId);
        this.name.set(name);
        this.description.set(description);
        this.bundlePrice.set(bundlePrice);
        this.status.set(status);
        this.created_by.set(created_by);
        this.created_at.set(created_at);
        this.updated_by.set(updated_by);
    }
    
    public Package(int packageId, String name, double bundlePrice, String status){
        this.packageId.set(packageId);
        this.name.set(name);
        this.bundlePrice.set(bundlePrice);
        this.status.set(status);
    }
    // Getters and Setters
    public int getPackageId() {
        return packageId.get();
    }

    public void setPackageId(int packageId) {
        this.packageId.set(packageId);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public double getBundlePrice() {
        return bundlePrice.get();
    }

    public void setBundlePrice(double bundlePrice) {
        this.bundlePrice.set(bundlePrice);
    }
    public void setstatus(String status){
        this.status.set(status);
    }

    public String getStatus(){
        return status.get();
    }


    public int getCreated_by() {
        return created_by.get();
    }

    public void setCreated_by(int created_by) {
        this.created_by.set(created_by);
    }

    public Timestamp getCreated_at() {
        return created_at.get();
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at.set(created_at);
    }

    public int getUpdated_by() {
        return updated_by.get();
    }

    public void setUpdated_by(int updated_by) {
        this.updated_by.set(updated_by);
    }
}