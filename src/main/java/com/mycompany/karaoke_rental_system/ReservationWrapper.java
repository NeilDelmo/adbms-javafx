package com.mycompany.karaoke_rental_system;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReservationWrapper {
        private final ObjectProperty<Reservation> reservation = new SimpleObjectProperty<>();

        public ReservationWrapper(Reservation reservation) {
            this.reservation.set(reservation);
        }

        public Reservation getReservation() {
            return reservation.get();
        }

    @Override
    public String toString() {
        if (reservation.get() == null) {
            return "Unknown Reservation";
        }

        Reservation res = reservation.get();

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
