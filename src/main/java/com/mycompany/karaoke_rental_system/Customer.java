/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.karaoke_rental_system;

import java.time.LocalDateTime;

/**
 *
 * @author Neil
 */
public class Customer {

        private int customerId;
        private String name;
        private String phone;
        private String address;
        private int createdBy;
        private int totalBookings;
        private LocalDateTime lastBooking;
        private double totalSpent;

        public Customer(int customerId, String name, String phone, String address, int createdBy, int totalBookings, LocalDateTime lastBooking, double totalSpent) {
            this.customerId = customerId;
            this.name = name;
            this.phone = phone;
            this.address = address;
            this.createdBy = createdBy;
            this.totalBookings = totalBookings;
            this.lastBooking = lastBooking;
            this.totalSpent = totalSpent;
        }
        
        public Customer(int customerId, String name){
            this.customerId = customerId;
            this.name = name;
        }
        public Customer(int customerId,String name, String address){
            this.customerId = customerId;
            this.name = name;
            this.address = address;
        }

        // Getters
        public int getCustomerId() {
            return customerId;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public String getAddress() {
            return address;
        }

        public int getTotalBookings() {
            return totalBookings;
        }

        public LocalDateTime getLastBooking() {
            return lastBooking;
        }

        public double getTotalSpent() {
            return totalSpent;
        }
        @Override
        public String toString(){
        return name;
        }
        
    }
