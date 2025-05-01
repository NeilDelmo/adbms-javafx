package com.mycompany.karaoke_rental_system;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.*;

public class RentalHistory {

        private final IntegerProperty reservationId = new SimpleIntegerProperty();
        private final StringProperty customerName = new SimpleStringProperty();
        private final ObjectProperty<LocalDateTime> startDate = new SimpleObjectProperty<>();
        private final ObjectProperty<LocalDateTime> endDate = new SimpleObjectProperty<>();
        private final StringProperty status = new SimpleStringProperty();
        private final DoubleProperty totalValue = new SimpleDoubleProperty();

        public RentalHistory(int reservationId, String customerName, LocalDateTime startDate,
                             LocalDateTime endDate, String status, double totalValue) {
            this.reservationId.set(reservationId);
            this.customerName.set(customerName);
            this.startDate.set(startDate);
            this.endDate.set(endDate);
            this.status.set(status);
            this.totalValue.set(totalValue);
        }

        // Getters
        public int getReservationId() {
            return reservationId.get();
        }
        public IntegerProperty getReservationIdProperty(){return reservationId;}

        public String getCustomerName() {
            return customerName.get();
        }


        public LocalDateTime getStartDate() {
            return startDate.get();
        }

        public LocalDateTime getEndDate() {
            return endDate.get();
        }

        public String getStatus() {
            return status.get();
        }

        public double getTotalValue() {
            return totalValue.get();
        }

        @Override
        public String toString() {
            return String.format("%s | %s to %s | Status: %s | Total: ₱%,.2f",
                    customerName.get(),
                    startDate.get().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                    endDate.get().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                    status.get(),
                    totalValue.get());
        }

    }
