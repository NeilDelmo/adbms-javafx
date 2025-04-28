/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.karaoke_rental_system;

import java.nio.file.WatchEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

/**
 *
 * @author Neil
 */
public class Customer{

        private final IntegerProperty customerId  = new SimpleIntegerProperty();
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty phone = new SimpleStringProperty();
        private final StringProperty address = new SimpleStringProperty();
        private final IntegerProperty createdBy = new SimpleIntegerProperty();
        private final IntegerProperty totalBookings = new SimpleIntegerProperty();
        private final ObjectProperty<LocalDateTime> lastBooking = new SimpleObjectProperty<>();
        private final DoubleProperty totalSpent = new SimpleDoubleProperty();

        public Customer(int customerId, String name, String phone, String address, int createdBy, int totalBookings, LocalDateTime lastBooking, double totalSpent) {
            this.customerId.set(customerId);
            this.name.set(name);
            this.phone.set(phone);
            this.address.set(address);
            this.createdBy.set(createdBy);
            this.totalBookings.set(totalBookings);
            this.lastBooking.set(lastBooking);
            this.totalSpent.set(totalSpent);
        }
        public Customer(){}
        
        public Customer(int customerId, String name){
            this.customerId.set(customerId);
            this.name.set(name);
        }
        public Customer(int customerId,String name, String address){
            this.customerId.set(customerId);
            this.name.set(name);
            this.address.set(address);
        }

        public int getCustomerId(){
            return customerId.get();
        }
        public IntegerProperty getCustomerIdProperty(){
            return customerId;
        }
        public void setCustomerId(int customerId){
            this.customerId.set(customerId);
        }
        public String getName(){
            return name.get();
        }
        public StringProperty getNameProperty(){
            return name;
        }
        public void setName(String name){
            this.name.set(name);
        }
        public String getPhone(){
            return phone.get();
        }
        public StringProperty getPhoneProperty(){
            return phone;
        }
        public void setPhone(String phone){
            this.phone.set(phone);
        }

        public String getAddress(){
             return address.get();
         }
         public StringProperty getAddressProperty(){
             return address;
         }
         public void setAddress(String address){
             this.address.set(address);
         }
    public Integer getCreatedBy(){
        return createdBy.get();
    }
    public IntegerProperty getCreatedByProperty(){
        return createdBy;
    }
    public void setCreatedBy(int createdBy){
        this.createdBy.set(createdBy);
    }
    public Integer getTotalBookings(){
        return totalBookings.get();
    }
    public IntegerProperty getTotalBookingsProperty(){
        return totalBookings;
    }
    public void setTotalBookings(int totalBookings){
        this.totalBookings.set(totalBookings);
    }
    public LocalDateTime getLastBooking(){
        return lastBooking.get();
    }
    public ObjectProperty<LocalDateTime> getLastBookingsProperty(){
        return lastBooking;
    }
    public void setLastBooking(LocalDateTime lastBooking){
        this.lastBooking.set(lastBooking);
    }
    public Double getTotalSpent(){
        return totalSpent.get();
    }
    public DoubleProperty getTotalSpentProperty(){
        return totalSpent;
    }
    public void setTotalSpent(Double totalSpent){
        this.totalSpent.set(totalSpent);
    }


}
