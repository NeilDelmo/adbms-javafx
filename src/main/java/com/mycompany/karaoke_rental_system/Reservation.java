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
    private Double totalAmount;      // Added for payment system
    private Double paidAmount;      // Added for payment system
    private String paymentStatus;

    public Reservation(int reservationId, Customer customer, LocalDate startDate,
                       LocalDate endDate, String status, Package pkg,
                       Double overdueCharges, Double totalAmount) {
        this.reservationId = reservationId;
        this.customer = customer;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.pkg = pkg;
        this.overdueCharges = overdueCharges;
        this.totalAmount = totalAmount;
        this.paidAmount = 0.0;     // Initialize to zero
        this.paymentStatus = "Unpaid"; // Default status
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
    public Double getTotalAmount() {
        if (pkg != null && totalAmount == null) {
            return pkg.getBundlePrice() + (overdueCharges != null ? overdueCharges : 0.0);
        }
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getPaidAmount() {
        return paidAmount != null ? paidAmount : 0.0;
    }
    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
        // Auto-update payment status when paid amount changes
        updatePaymentStatus();
    }
    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    private void updatePaymentStatus() {
        double balance = getTotalAmount() - getPaidAmount();
        if (balance <= 0) {
            paymentStatus = "Paid";
        } else if (getPaidAmount() > 0) {
            paymentStatus = "Partial";
        } else {
            paymentStatus = "Unpaid";
        }
    }

    // Calculates remaining balance
    public Double getBalance() {
        return getTotalAmount() - getPaidAmount();
    }

    public void setOverdueCharges(Double overdueCharges) {
        this.overdueCharges = overdueCharges;
    }
}
