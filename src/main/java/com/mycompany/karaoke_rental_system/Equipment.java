
package com.mycompany.karaoke_rental_system;

import java.sql.Timestamp;

public class Equipment {
    private int equipmentId;
    private String name;
    private String description;
    private double rentalPrice;
    private double overduePenalty;
    private String status;
    private int created_by;
    private Timestamp created_at;
    private int updated_by;

    // Constructors, getters, and setters
    public Equipment(int equipmentId, String name, String description, double rentalPrice, double overduePenalty, String status, int created_by, Timestamp created_at, int updated_by) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.description = description;
        this.rentalPrice = rentalPrice;
        this.overduePenalty = overduePenalty;
        this.status = status;
        this.created_by = created_by;
        this.created_at = created_at;
        this.updated_by = updated_by;
    }
    
    public Equipment(int equipmentId, String name, double rentalPrice, String description){
        this.equipmentId = equipmentId;
        this.name = name;
        this.rentalPrice = rentalPrice;
        this.description = description;
        
        
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
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

    public double getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(double rentalPrice) {
        this.rentalPrice = rentalPrice;
    }

    public double getOverduePenalty() {
        return overduePenalty;
    }

    public void setOverduePenalty(double overduePenalty) {
        this.overduePenalty = overduePenalty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override
    public String toString(){
        return name;
    }
}
