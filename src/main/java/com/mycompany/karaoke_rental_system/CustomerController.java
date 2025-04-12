package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;
import java.sql.ResultSet;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class CustomerController implements Initializable {

    @FXML
    private BorderPane rootPane;

    @FXML
    private TextField address_txt;

    @FXML
    private TableView<Customer> customer_table;

    @FXML
    private TextField name_txt;

    @FXML
    private TextField phone_txt;

    @FXML
    private ListView<RentalHistory> rental_historylist;

    @FXML
    private TextField search_txt;

    @FXML
    private Button submit_btn;

    @FXML
    private Button reservation_btn;

    @FXML
    private TableColumn<Customer, String> nameCol;
    @FXML
    private TableColumn<Customer, String> phoneCol;
    @FXML
    private TableColumn<Customer, String> addressCol;
    @FXML
    private TableColumn<Customer, Number> bookingsCol;
    @FXML
    private TableColumn<Customer, String> lastBookingCol;
    @FXML
    private TableColumn<Customer, String> spentCol;

    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private ObservableList<RentalHistory> rental_history = FXCollections.observableArrayList();

    public Button getreservation_btn() {
        return reservation_btn;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadCustomers();
        setupSearch();
        setupTableSelection();

        reservation_btn.setOnAction(e
                    -> rootPane.setCenter(Model.getInstance().getViewFactory().getDashboardView())
            );

        }

    private void setupTableColumns() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        bookingsCol.setCellValueFactory(new PropertyValueFactory<>("totalBookings"));

        lastBookingCol.setCellValueFactory(cell -> {
            LocalDateTime date = cell.getValue().getLastBooking();
            return new SimpleStringProperty(date != null
                    ? date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A");
        });

        spentCol.setCellValueFactory(cell
                -> new SimpleStringProperty(String.format("₱%,.2f", cell.getValue().getTotalSpent())));

        customer_table.setItems(customers);
    }

    @FXML
    private void saveCustomer() {
        String name = name_txt.getText().trim();
        String phone = phone_txt.getText().trim();
        String address = address_txt.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            showAlert("Error", "All fields are required!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO customers (name, phone, address, created_by) VALUES (?, ?, ?, ?)")) {

            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, address);
            pstmt.setInt(4, 1); // Replace with actual user ID from login

            pstmt.executeUpdate();
            clearFields();
            loadCustomers(); // Refresh the table

        } catch (SQLException e) {
            showAlert("Database Error", "Error saving customer: " + e.getMessage());
        }
    }

    private void loadCustomers() {
        customers.clear();
        String query = "SELECT c.customer_id, c.name, c.phone, c.address, c.created_by, "
                + "COUNT(r.reservation_id) AS total_bookings, "
                + "MAX(r.end_datetime) AS last_booking, "
                + "COALESCE(SUM(p.amount), 0) AS total_spent "
                + "FROM customers c "
                + "LEFT JOIN reservations r ON c.customer_id = r.customer_id "
                + "LEFT JOIN payments p ON r.reservation_id = p.reservation_id "
                + "WHERE c.created_by = ? " // Filter by current user
                + "GROUP BY c.customer_id";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, Model.getInstance().getcurrentuserid()); // Implement this based on your auth system

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getInt("created_by"),
                        rs.getInt("total_bookings"),
                        rs.getTimestamp("last_booking") != null
                        ? rs.getTimestamp("last_booking").toLocalDateTime() : null,
                        rs.getDouble("total_spent")
                ));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading customers: " + e.getMessage());
        }
    }

    private void setupSearch() {
        search_txt.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                customer_table.setItems(customers);
            } else {
                ObservableList<Customer> filtered = customers.filtered(c
                        -> c.getName().toLowerCase().contains(newVal.toLowerCase())
                        || c.getPhone().contains(newVal)
                );
                customer_table.setItems(filtered);
            }
        });
    }

    private void setupTableSelection() {
        customer_table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadRentalHistory(newVal.getCustomerId());
            }
        });
    }

    private void loadRentalHistory(int customerId) {
        rental_history.clear();
        String query
                = "SELECT r.reservation_id, c.name AS customer_name, "
                + "r.start_datetime, r.end_datetime, r.status, "
                + "COALESCE(SUM(CASE WHEN ri.package_id IS NOT NULL THEN p.bundle_price ELSE ri.price_per_unit END), 0) AS total_rental_value "
                + "FROM reservations r "
                + "JOIN customers c ON r.customer_id = c.customer_id "
                + "LEFT JOIN reservation_items ri ON r.reservation_id = ri.reservation_id "
                + "LEFT JOIN packages p ON ri.package_id = p.package_id "
                + "WHERE r.customer_id = ? "
                + "GROUP BY r.reservation_id, c.name, r.start_datetime, r.end_datetime, r.status";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                rental_history.add(new RentalHistory(
                        rs.getInt("reservation_id"),
                        rs.getString("customer_name"),
                        rs.getTimestamp("start_datetime").toLocalDateTime(),
                        rs.getTimestamp("end_datetime").toLocalDateTime(),
                        rs.getString("status"),
                        rs.getDouble("total_rental_value")
                ));
            }
            rental_historylist.setItems(rental_history);

        } catch (SQLException e) {
            showAlert("Database Error", "Error loading rental history: " + e.getMessage());
        }
    }

    private void clearFields() {
        name_txt.clear();
        phone_txt.clear();
        address_txt.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Customer {

        private final int customerId;
        private final String name;
        private final String phone;
        private final String address;
        private final int createdBy;
        private final int totalBookings;
        private final LocalDateTime lastBooking;
        private final double totalSpent;

        public Customer(int customerId, String name, String phone, String address, int createdBy, int totalBookings, LocalDateTime lastBooking, double totalSpent) {
            this.customerId = customerId;
            this.name = name;
            this.phone = phone;
            this.address = address;
            this.createdBy = createdBy;
            this.totalBookings = totalBookings;
            this.lastBooking = lastBooking;
            this.totalSpent = totalSpent;
        }

        // Getters
        public int getCustomerId() {
            return customerId;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public String getAddress() {
            return address;
        }

        public int getTotalBookings() {
            return totalBookings;
        }

        public LocalDateTime getLastBooking() {
            return lastBooking;
        }

        public double getTotalSpent() {
            return totalSpent;
        }
    }

    public static class RentalHistory {

        private final int reservationId;
        private final String customerName;
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        private final String status;
        private final double totalValue;

        public RentalHistory(int reservationId, String customerName, LocalDateTime startDate,
                LocalDateTime endDate, String status, double totalValue) {
            this.reservationId = reservationId;
            this.customerName = customerName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.totalValue = totalValue;
        }

        // Getters
        public int getReservationId() {
            return reservationId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public String getStatus() {
            return status;
        }

        public double getTotalValue() {
            return totalValue;
        }
    }

}
