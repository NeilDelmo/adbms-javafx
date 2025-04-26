package com.mycompany.karaoke_rental_system;
import javafx.beans.property.*;

public class PaymentHistory {
    private final StringProperty date;
    private final DoubleProperty amount;
    private final StringProperty method;
    private final StringProperty recordedBy;

    public PaymentHistory(String date, double amount,
                          String method, String recordedBy) {
        this.date = new SimpleStringProperty(date);
        this.amount = new SimpleDoubleProperty(amount);
        this.method = new SimpleStringProperty(method);
        this.recordedBy = new SimpleStringProperty(recordedBy);
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
