/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.karaoke_rental_system;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.Optional;

public class Rental {

    private final IntegerProperty rentalId = new SimpleIntegerProperty();
    private final ObjectProperty<Customer> customer = new SimpleObjectProperty<>();
    private final IntegerProperty customerId = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> startDate =  new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty();
    private final ObjectProperty<Package> pkg = new SimpleObjectProperty<>();
    private final DoubleProperty overdueCharges = new SimpleDoubleProperty();
    private final DoubleProperty totalAmount = new SimpleDoubleProperty();      // Added for payment system
    private final DoubleProperty paidAmount = new SimpleDoubleProperty();
    private final StringProperty paymentStatus = new SimpleStringProperty("Unpaid");

    public Rental(int rentalId,int customerId, Customer customer, LocalDate startDate,
                       LocalDate endDate, String status, Package pkg,
                       Double overdueCharges, Double totalAmount) {
        this.rentalId.set(rentalId);
        this.customerId.set(customerId);
        this.customer.set(customer);
        this.startDate.set(startDate);
        this.endDate.set(endDate);
        this.status.set(status);
        this.pkg.set(pkg);
        this.overdueCharges.set(overdueCharges);
        this.totalAmount.set(totalAmount);
        this.paidAmount.set(0.0);     // Initialize to zero
        this.paymentStatus.set("Unpaid"); // Default status
        this.paidAmount.addListener((obs,oldVal,newVal)->updatePaymentStatus());
    }
    public Rental(){
        this.paidAmount.addListener((obs, oldVal, newVal)-> updatePaymentStatus());
    }

    public int getRentalId() {
        return rentalId.get();
    }

    public IntegerProperty rentalIdProperty(){
        return rentalId;
    }
    public void setRentalId(int reservationId){
        this.rentalId.set(reservationId);
    }
    public Customer getCustomer(){
        return customer.get();
    }
    public ObjectProperty<Customer> customerProperty(){
        return customer;
    }
    public void setCustomer(Customer customer){
        this.customer.set(customer);
    }
    public int getCustomerId(){
        return customerId.get();
    }
    public IntegerProperty customerIdProperty(){
        return customerId;
    }
    public void setCustomerId(int customerId){
        this.customerId.set(customerId);
    }

    public LocalDate getStartDate(){
        return startDate.get();
    }
    public ObjectProperty<LocalDate> startDateProperty(){
        return startDate;
    }
    public void setStartDate(LocalDate startDate){
        this.startDate.set(startDate);
    }
    public LocalDate getEndDate(){
        return endDate.get();
    }
    public ObjectProperty<LocalDate> endDateProperty(){
        return endDate;
    }
    public void setEndDate(LocalDate endDate){
        this.endDate.set(endDate);
    }

    public String getStatus(){
        return status.get();
    }
    public  StringProperty getStatusProperty(){
        return status;
    }
    public void setStatus(String status){
        this.status.set(status);
    }
    public Package getPkg(){
        return pkg.get();
    }
    public ObjectProperty<Package> getPkgProperty(){
        return pkg;
    }
    public void setPkg(Package pkg){
        this.pkg.set(pkg);
    }

    public Double getOverdueCharges(){
        return overdueCharges.get();
    }

    public DoubleProperty getOverdueChargesProperty(){
        return overdueCharges;
    }
    public void setOverdueCharges(Double overdueCharges){
        this.overdueCharges.set(overdueCharges);
    }
    public double getTotalAmount() {
        if (pkg.get() != null && totalAmount.get() == 0.0) {
            // Use Optional to handle null values safely
            return pkg.get().getBundlePrice() +
                    Optional.ofNullable(overdueCharges.get()).orElse(0.0);
        }
        return totalAmount.get();
    }

    public DoubleProperty totalAmountProperty() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount.set(totalAmount);
    }
    public double getPaidAmount() {
        return paidAmount.get();
    }

    public DoubleProperty paidAmountProperty() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount.set(paidAmount);
    }

    public String getPaymentStatus() {
        return paymentStatus.get();
    }

    public StringProperty paymentStatusProperty() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus.set(paymentStatus);
    }

    private void updatePaymentStatus() {
        double balance = getTotalAmount() - getPaidAmount();
        if (balance <= 0) {
            paymentStatus.set("Paid");
        } else if (getPaidAmount() > 0) {
            paymentStatus.set("Partial");
        } else {
            paymentStatus.set("Unpaid");
        }
    }

    public double getBalance() {
        return getTotalAmount() - getPaidAmount();
    }

}
