package com.mycompany.karaoke_rental_system;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

public class PaymentSummary {
    private final IntegerProperty rentalId = new SimpleIntegerProperty();
    private final DoubleProperty totalAmount = new SimpleDoubleProperty();
    private final DoubleProperty amountPaid = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final DoubleProperty balance = new SimpleDoubleProperty();

    public PaymentSummary(int rentalId, double totalamount, double amountpaid, String status) {
        this.rentalId.set(rentalId);
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
    public PaymentSummary(Rental rental) {
        this(
                rental.getRentalId(),
                rental.getTotalAmount(),
                rental.getPaidAmount(),
                rental.getPaymentStatus()
        );

        // Listen to Reservation changes and update PaymentSummary
        rental.rentalIdProperty().addListener((obs, oldVal, newVal) ->
                this.rentalId.set(newVal.intValue())
        );
        rental.totalAmountProperty().addListener((obs, oldVal, newVal) ->
                this.totalAmount.set(newVal.doubleValue())
        );
        rental.paidAmountProperty().addListener((obs, oldVal, newVal) ->
                this.amountPaid.set(newVal.doubleValue())
        );
        rental.paymentStatusProperty().addListener((obs, oldVal, newVal) ->
                this.status.set(newVal)
        );
    }

    // Property getters
    public IntegerProperty rentalIdProperty() { return rentalId; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }
    public DoubleProperty amountPaidProperty() { return amountPaid; }
    public StringProperty statusProperty() { return status; }
    public DoubleProperty balanceProperty() { return balance;}

    // Regular getters
    public int getRentalId() { return rentalId.get(); }
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
