
package com.mycompany.karaoke_rental_system;

import java.sql.Timestamp;

import javafx.beans.NamedArg;
import javafx.beans.property.*;

public class Equipment {
    public static final String STATUS_AVAILABLE = "Available";
    public static final String STATUS_RENTED = "Rented";
     private  final IntegerProperty equipmentId =  new SimpleIntegerProperty();
    private final StringProperty name =  new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty rentalPrice = new SimpleDoubleProperty();
    private final DoubleProperty overduePenalty = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final IntegerProperty created_by = new SimpleIntegerProperty();
    private final ObjectProperty<Timestamp> created_at = new SimpleObjectProperty<>();
    private final IntegerProperty updated_by = new SimpleIntegerProperty();

    // Constructors, getters, and setters
    public Equipment(int equipmentId, String name, String description, double rentalPrice, double overduePenalty, String status, int created_by, Timestamp created_at, int updated_by) {
        this.equipmentId.set(equipmentId);
        this.name.set(name);
        this.description.set(description);
        this.rentalPrice.set(rentalPrice);
        this.overduePenalty.set(overduePenalty);
        this.status.set(status);
        this.created_by.set(created_by);
        this.created_at.set(created_at);
        this.updated_by.set(updated_by);
    }
    
    public Equipment(int equipmentId, String name, double rentalPrice, String description){
        this.equipmentId.set(equipmentId);
        this.name.set(name);
        this.rentalPrice.set(rentalPrice);
        this.description.set(description);
        
        
    }

    public int getEquipmentId() {
        return equipmentId.get();
    }
    public IntegerProperty getEquipmentIdProperty(){
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId.set(equipmentId);
    }

    public String getName() {
        return name.get();
    }
    public StringProperty getNameProperty(){
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty getDescriptionProperty(){
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public Double getRentalPrice() {
        return rentalPrice.get();
    }
    public DoubleProperty getRentalPriceProperty(){
        return  rentalPrice;
    }

    public void setRentalPrice(double rentalPrice) {
        this.rentalPrice.set(rentalPrice);
    }

    public double getOverduePenalty() {
        return overduePenalty.get();
    }
    public DoubleProperty getOverduePenaltyProperty(){
        return overduePenalty;
    }

    public void setOverduePenalty(double overduePenalty) {
        this.overduePenalty.set(overduePenalty);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
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

    @Override
    public String toString(){
        return name.get();
    }
}
