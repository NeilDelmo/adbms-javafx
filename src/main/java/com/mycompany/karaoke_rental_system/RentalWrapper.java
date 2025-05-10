package com.mycompany.karaoke_rental_system;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RentalWrapper {
        private final ObjectProperty<Rental> rental = new SimpleObjectProperty<>();

        public RentalWrapper(Rental reservation) {
            this.rental.set(reservation);
        }

        public Rental getRental() {
            return rental.get();
        }

    @Override
    public String toString() {
        if (rental.get() == null) {
            return "Unknown Reservation";
        }

        Rental res = rental.get();

        String customerName = (res.customerProperty().get() != null)
                ? res.customerProperty().get().getName()
                : "Unknown Customer";

        LocalDate start = res.startDateProperty().get();
        LocalDate end = res.endDateProperty().get();
        double total = res.totalAmountProperty().get();
        double paid = res.paidAmountProperty().get();

        String formattedStart = (start != null) ? start.format(DateTimeFormatter.ofPattern("MMM dd")) : "Unknown";
        String formattedEnd = (end != null) ? end.format(DateTimeFormatter.ofPattern("MMM dd")) : "Unknown";

        return String.format("%s (%s - %s) - ₱%.2f",
                customerName,
                formattedStart,
                formattedEnd,
                total - paid);
    }
}
