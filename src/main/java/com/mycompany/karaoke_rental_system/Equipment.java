
package com.mycompany.karaoke_rental_system;

   public class Equipment {
    private int equipmentId;
    private String name;
    private String description;
    private double rentalPrice;
    private double overduePenalty;
    private String status;

    // Constructors, getters, and setters
    public Equipment(int equipmentId, String name, String description, double rentalPrice, double overduePenalty, String status) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.description = description;
        this.rentalPrice = rentalPrice;
        this.overduePenalty = overduePenalty;
        this.status = status;
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
}
