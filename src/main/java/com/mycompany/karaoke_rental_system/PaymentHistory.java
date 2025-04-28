package com.mycompany.karaoke_rental_system;
import javafx.beans.property.*;

public class PaymentHistory {
    private final StringProperty date = new SimpleStringProperty();
    private final DoubleProperty amount = new SimpleDoubleProperty();
    private final StringProperty method = new SimpleStringProperty();
    private final StringProperty recordedBy = new SimpleStringProperty();

    public PaymentHistory(String date, double amount,
                          String method, String recordedBy) {
        this.date.set(date);
        this.amount.set(amount);
        this.method.set(method);
        this.recordedBy.set(recordedBy);
    }

    // Property getters
    public StringProperty dateProperty() { return date; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty methodProperty() { return method; }
    public StringProperty recordedByProperty() { return recordedBy; }

    // Regular getters
    public String getDate() { return date.get(); }
    public double getAmount() { return amount.get(); }
    public String getMethod() { return method.get(); }
    public String getRecordedBy() { return recordedBy.get(); }
}
