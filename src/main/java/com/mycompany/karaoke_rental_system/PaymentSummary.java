package com.mycompany.karaoke_rental_system;
import javafx.beans.property.*;

public class PaymentSummary {
    private final IntegerProperty reservationId;
    private final DoubleProperty totalAmount;
    private final DoubleProperty amountPaid;
    private final DoubleProperty balance;
    private final StringProperty status;

    public PaymentSummary(int reservationId, double totalAmount,
                          double amountPaid, String status) {
        this.reservationId = new SimpleIntegerProperty(reservationId);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
        this.amountPaid = new SimpleDoubleProperty(amountPaid);
        this.balance = new SimpleDoubleProperty(totalAmount - amountPaid);
        this.status = new SimpleStringProperty(status);
    }

    // Property getters
    public IntegerProperty reservationIdProperty() { return reservationId; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }
    public DoubleProperty amountPaidProperty() { return amountPaid; }
    public DoubleProperty balanceProperty() { return balance; }
    public StringProperty statusProperty() { return status; }

    // Regular getters
    public int getReservationId() { return reservationId.get(); }
    public double getTotalAmount() { return totalAmount.get(); }
    public double getAmountPaid() { return amountPaid.get(); }
    public double getBalance() { return balance.get(); }
    public String getStatus() { return status.get(); }

}
