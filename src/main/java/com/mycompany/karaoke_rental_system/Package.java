
package com.mycompany.karaoke_rental_system;

import java.sql.Timestamp;

public class Package {
    private int packageId;
    private String name;
    private String description;
    private double bundlePrice;
    private String status;
    private int created_by;
    private Timestamp created_at;
    private int updated_by;

    // Constructor
    public Package(int packageId, String name, String description, double bundlePrice, String status, int created_by, Timestamp created_at, int updated_by) {
        this.packageId = packageId;
        this.name = name;
        this.description = description;
        this.bundlePrice = bundlePrice;
        this.status = status;
        this.created_by = created_by;
        this.created_at = created_at;
        this.updated_by = updated_by;
    }
    
    public Package(int packageId, String name, double bundlePrice, String status){
        this.packageId = packageId;
        this.name = name;
        this.bundlePrice = bundlePrice;
        this.status = status;
    }
    // Getters and Setters
    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBundlePrice() {
        return bundlePrice;
    }

    public void setBundlePrice(double bundlePrice) {
        this.bundlePrice = bundlePrice;
    }
    public void setstatus(String status){
        this.status = status;
    }

    public String getStatus(){
        return status;
    }


    public int getCreated_by() {
        return created_by;
    }

    public void setCreated_by(int created_by) {
        this.created_by = created_by;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public int getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(int updated_by) {
        this.updated_by = updated_by;
    }
}