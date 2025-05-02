package com.mycompany.karaoke_rental_system;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

public class PaymentSummary {
    private final IntegerProperty reservationId = new SimpleIntegerProperty();
    private final DoubleProperty totalAmount = new SimpleDoubleProperty();
    private final DoubleProperty amountPaid = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final DoubleProperty balance = new SimpleDoubleProperty();

    public PaymentSummary(int reservationId, double totalamount, double amountpaid, String status) {
        this.reservationId.set(reservationId);
        this.totalAmount.set(totalamount);
        this.amountPaid.set(amountpaid);
        this.status.set(status);

        // Bind balance to totalAmount - amountPaid using createDoubleBinding
        balance.bind(Bindings.createDoubleBinding(
                () -> this.totalAmount.get() - this.amountPaid.get(),
                this.totalAmount,
                this.amountPaid
        ));
    }
    public PaymentSummary(Reservation reservation) {
        this(
                reservation.getReservationId(),
                reservation.getTotalAmount(),
                reservation.getPaidAmount(),
                reservation.getPaymentStatus()
        );

        // Listen to Reservation changes and update PaymentSummary
        reservation.reservationIdProperty().addListener((obs, oldVal, newVal) ->
                this.reservationId.set(newVal.intValue())
        );
        reservation.totalAmountProperty().addListener((obs, oldVal, newVal) ->
                this.totalAmount.set(newVal.doubleValue())
        );
        reservation.paidAmountProperty().addListener((obs, oldVal, newVal) ->
                this.amountPaid.set(newVal.doubleValue())
        );
        reservation.paymentStatusProperty().addListener((obs, oldVal, newVal) ->
                this.status.set(newVal)
        );
    }

    // Property getters
    public IntegerProperty reservationIdProperty() { return reservationId; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }
    public DoubleProperty amountPaidProperty() { return amountPaid; }
    public StringProperty statusProperty() { return status; }
    public DoubleProperty balanceProperty() { return balance;}

    // Regular getters
    public int getReservationId() { return reservationId.get(); }
    public double getTotalAmount() { return totalAmount.get(); }
    public double getAmountPaid() { return amountPaid.get(); }
    public double getBalance() { return balance.get(); }
    public String getStatus() { return status.get(); }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid.set(amountPaid);
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

}
