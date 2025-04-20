/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.karaoke_rental_system;

import java.time.LocalDate;

public class Reservation {

    private int reservationId;
    private Customer customer;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Package pkg;
    private Double overdueCharges;

    public Reservation(int reservationId, Customer customer, LocalDate startDate, LocalDate endDate, String status, Package pkg, Double overdueCharges) {
        this.reservationId = reservationId;
        this.customer = customer;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.pkg = pkg;
        this.overdueCharges= overdueCharges;

    }
    public Reservation(){}

    public int getReservationId() {
        return reservationId;
    }
    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }
    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Package getPkg() {
        return pkg;
    }
    public void setPkg(Package pkg) {
        this.pkg = pkg;
    }

    public Double getOverdueCharges() {
        return overdueCharges;
    }

    public void setOverdueCharges(Double overdueCharges) {
        this.overdueCharges = overdueCharges;
    }
}
