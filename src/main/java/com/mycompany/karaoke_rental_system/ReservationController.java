package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import java.net.URL;
import java.sql.Connection;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.ComboBoxTableCell;

public class ReservationController implements Initializable {

    public Button record_btn;
    @FXML
    private ComboBox<String> filter_reservation_cmb;

    @FXML
    private TableColumn<Reservation, String> statusCol;

    @FXML
    private TableColumn<Package, Double> bundlePriceCol;

    @FXML
    private TableColumn<Reservation, String> customerCol;

    @FXML
    private ComboBox<Customer> customer_cmb;

    @FXML
    private TableColumn<Reservation, String> addressCol;

    @FXML
    private DatePicker end_date;

    @FXML
    private ListView<Equipment> packageContentsList;

    @FXML
    private TableColumn<Package, String> packageNameCol;

    @FXML
    private TableColumn<Package, String> packageStatusCol;

    @FXML
    private TableView<Package> package_table;

    @FXML
    private TableColumn<Reservation, String> periodCol;

    @FXML
    private TableView<Reservation> reservation_table;

    @FXML
    private DatePicker start_date;

    @FXML
    private ComboBox<String> status_cmb;

    @FXML
    private TableColumn<Reservation, String> packageCol;

    private ObservableList<Package> packageList;

    private Package selectedPackage;
    private ObservableList<Reservation> masterReservationList;

    private Customer selectedCustomer;

    public void setSelectedCustomer(Customer customer) {
        this.selectedCustomer = customer;
        if (customer != null && customer_cmb != null) {
            customer_cmb.getSelectionModel().select(customer);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TableColumnsForPackage();
        loadCustomers(); // Add this
        setupDateListener(); // Add this
        setupPackageSelection();
        setupListView();
        reservation_table.setEditable(true);
        TableColumnsForReservation();
        loadReservations();

        start_date.setValue(LocalDate.now());
        end_date.setValue(LocalDate.now().plusDays(7));

        status_cmb.getItems().addAll("All", "Rented", "Available");
        status_cmb.getSelectionModel().selectFirst();

        status_cmb.valueProperty().addListener((obs, oldVal, newVal) -> checkAvailability());

        checkAvailability();

        filter_reservation_cmb.getItems().addAll("All", "Pending", "Confirmed", "Completed", "Cancelled", "Overdue");
        filter_reservation_cmb.getSelectionModel().selectFirst();

        filter_reservation_cmb.valueProperty().addListener((obs, oldVal, newVal) -> filterReservations(newVal));

    }

    private void TableColumnsForPackage() {
        packageNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        bundlePriceCol.setCellValueFactory(new PropertyValueFactory<>("bundlePrice"));
        packageStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        packageList = FXCollections.observableArrayList();
        package_table.setItems(packageList);
    }

    private void TableColumnsForReservation() {
        // Customer Name
        customerCol.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getCustomer().getName()));
        // Period (Start Date - End Date)
        periodCol.setCellValueFactory(cellData -> {
            Reservation res = cellData.getValue();
            String period = res.getStartDate().toString() + " - " + res.getEndDate().toString();
            return new SimpleStringProperty(period);
        });
        // Customer Address
        addressCol.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getCustomer().getAddress()));
        // Package Name
        packageCol.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getPkg().getName()));
        // Reservation Status
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        ObservableList<String> statusOptions = FXCollections.observableArrayList("Pending", "Confirmed", "Completed", "Cancelled", "Overdue");
        statusCol.setCellFactory(ComboBoxTableCell.forTableColumn(statusOptions));
        statusCol.setOnEditCommit(event -> {
            Reservation reservation = event.getRowValue();
            String newStatus = event.getNewValue();

            if (reservation == null || newStatus == null || newStatus.isEmpty()) {
                showError("Invalid Selection", "Unable to update reservation.");
                return;
            }

            // ✅ Check if current status is restricted
            String currentStatus = reservation.getStatus();
            if (isRestrictedStatus(currentStatus)) {
                showError("Status Locked",
                        "Cannot change status of a reservation that is " + currentStatus + ".");
                return;
            }

            // Proceed with valid status update
            updateReservationStatus(reservation.getReservationId(), newStatus);
            reservation.setStatus(newStatus);

            // Handle equipment status if completed
            if ("Completed".equals(newStatus)) {
                updateEquipmentStatus(reservation);
            }
        });
    }

    // Helper method to check restricted statuses
    private boolean isRestrictedStatus(String status) {
        return status != null && (
                status.equals("Overdue") ||
                        status.equals("Completed") ||
                        status.equals("Cancelled")
        );
    }

    private void updateReservationStatus(int reservationId, String newStatus) {
        int currentUser = Model.getInstance().getcurrentuserid();
        if (currentUser <= 0) currentUser = 1;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE reservations SET status = ?, updated_by = ? WHERE reservation_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, newStatus);
                pst.setInt(2, currentUser);
                pst.setInt(3, reservationId);
                pst.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to update reservation status: " + e.getMessage());
        }
    }

    private void updateEquipmentStatus(int equipmentId, String newStatus) throws SQLException {
        int currentUser = Model.getInstance().getcurrentuserid();
        if (currentUser <= 0) currentUser = 1;

        String query = "UPDATE equipment SET status = ?, updated_by = ? WHERE equipment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, currentUser);
            pstmt.setInt(3, equipmentId);
            pstmt.executeUpdate();
        }
    }

    private void updateEquipmentStatus(Reservation reservation) {
        try {
            int reservationId = reservation.getReservationId();

            // Query to get associated equipment IDs from reservation_items
            String query = "SELECT equipment_id FROM reservation_items WHERE reservation_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, reservationId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int equipmentId = rs.getInt("equipment_id");
                    updateEquipmentStatus(equipmentId, "Available"); // Call the existing method
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to update equipment status: " + e.getMessage());
        }
    }

    private void setupListView() {
        packageContentsList.setCellFactory(lv -> new ListCell<Equipment>() {
            @Override
            protected void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
    }

    private void setupPackageSelection() {
        package_table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedPackage = newSelection;
                    loadPackageContents();
                });
    }

    private void loadPackageContents() {
        if (selectedPackage == null) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT e.equipment_id, e.name, e.rental_price "
                    + "FROM package_equipment pe "
                    + "JOIN equipment e ON pe.equipment_id = e.equipment_id "
                    + "WHERE pe.package_id = ?";

            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, selectedPackage.getPackageId());
            ResultSet rs = pst.executeQuery();

            ObservableList<Equipment> contents = FXCollections.observableArrayList();
            while (rs.next()) {
                contents.add(new Equipment(
                        rs.getInt("equipment_id"),
                        rs.getString("name"),
                        rs.getDouble("rental_price"),
                        "In Package"
                ));
            }
            packageContentsList.setItems(contents);

        } catch (SQLException e) {
            showError("Database Error", "Failed to load package contents: " + e.getMessage());
        }
    }

    private void loadCustomers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT customer_id, name FROM customers";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            ObservableList<Customer> customers = FXCollections.observableArrayList();
            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name")
                ));
            }
            customer_cmb.setItems(customers);

        } catch (SQLException e) {
            showError("Database Error", "Failed to load customers: " + e.getMessage());
        }
    }

    private void setupDateListener() {
        start_date.valueProperty().addListener((obs, oldVal, newVal) -> checkAvailability());
        end_date.valueProperty().addListener((obs, oldVal, newVal) -> checkAvailability());
    }

    @FXML
    private void checkAvailability() {
        if (!validDates()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall("{call sp_check_package_availability(?, ?, ?)}")) {

            stmt.setDate(1, Date.valueOf(start_date.getValue()));
            stmt.setDate(2, Date.valueOf(end_date.getValue()));
            stmt.setInt(3, Model.getInstance().getcurrentuserid());

            ResultSet rs = stmt.executeQuery();
            packageList.clear();

            while (rs.next()) {
                packageList.add(new Package(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("availability_status")
                ));
            }
            String selectedStatus = status_cmb.getValue();
            ObservableList<Package> filteredPackages = packageList.filtered(pkg -> {
                if (selectedStatus == null || "All".equals(selectedStatus)) {
                    return true;
                } else if ("Rented".equals(selectedStatus)) {
                    return "Reserved".equals(pkg.getStatus());
                } else if ("Available".equals(selectedStatus)) {
                    return "Available".equals(pkg.getStatus());
                }
                return false;
            });
            package_table.setItems(filteredPackages);

        } catch (SQLException e) {
            showError("Availability Check Failed", e.getMessage());
        }
    }

    private void filterReservations(String status) {
        ObservableList<Reservation> filteredReservations = FXCollections.observableArrayList();

        // Iterate through the master list
        for (Reservation reservation : masterReservationList) {
            if ("All".equals(status) || reservation.getStatus().equals(status)) {
                filteredReservations.add(reservation);
            }
        }

        // Update the reservation table with the filtered list
        reservation_table.setItems(filteredReservations);
    }

    @FXML
    private void handleAddReservation() {
        if (!validateReservation()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Create reservation
            int reservationId = createReservation(conn);

            // Add package to reservation items
            addPackageToReservation(conn, reservationId);

            conn.commit();
            showSuccess("Reservation Created", "Reservation ID: " + reservationId);
            resetForm();
            ViewFactory vf = new ViewFactory();
            vf.clearViews();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Reservation Failed", "Error creating reservation: " + e.getMessage());
        }
    }
    @FXML
    private void handleCancelReservation() {
        Reservation selectedReservation = reservation_table.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showError("No Selection", "Please select a reservation to cancel.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "{call sp_cancel_reservation(?, ?)}";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setInt(1, selectedReservation.getReservationId());
                stmt.setInt(2, Model.getInstance().getcurrentuserid());
                stmt.execute();
            }
            loadReservations();  // Reload reservations
            checkAvailability(); // Refresh package availability
            showSuccess("Cancellation Successful", "Reservation ID: " + selectedReservation.getReservationId() + " has been cancelled.");
        } catch (SQLException e) {
            showError("Cancellation Failed", "Error cancelling reservation: " + e.getMessage());
        }
    }
    @FXML
    private void handleConfirmReservation() {
        Reservation selectedReservation = reservation_table.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showError("No Selection", "Please select a reservation to confirm.");
            return;
        }

        // ✅ Check if current status is restricted
        if (isRestrictedStatus(selectedReservation.getStatus())) {
            showError("Action Not Allowed",
                    "Cannot confirm a reservation that is " + selectedReservation.getStatus() + ".");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "{call sp_confirm_reservation(?, ?)}";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setInt(1, selectedReservation.getReservationId());
                stmt.setInt(2, Model.getInstance().getcurrentuserid());
                stmt.execute();
            }
            loadReservations();
            checkAvailability();
            showSuccess("Confirmation Successful",
                    "Reservation ID: " + selectedReservation.getReservationId() + " has been confirmed.");
        } catch (SQLException e) {
            showError("Confirmation Failed", "Error confirming reservation: " + e.getMessage());
        }
    }

    private int createReservation(Connection conn) throws SQLException {
        String sql = "{call sp_create_reservation(?, ?, ?, ?, ?)}";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            // Bind parameters
            stmt.setInt(1, customer_cmb.getValue().getCustomerId());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(start_date.getValue().atStartOfDay()));
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(end_date.getValue().atStartOfDay()));

            // Retrieve and validate currentUserId
            int currentUserId = Model.getInstance().getcurrentuserid();
            System.out.println("Retrieved currentUserId: " + currentUserId); // Debug output
            if (currentUserId == 0 || currentUserId < 0) {
                throw new SQLException("Invalid user ID: " + currentUserId);
            }

            // Bind currentUserId to the stored procedure
            stmt.setInt(4, currentUserId);
            System.out.println("Bound currentUserId to CallableStatement: " + currentUserId); // Debug output

            // Register OUT parameter
            stmt.registerOutParameter(5, Types.INTEGER);

            // Execute the stored procedure
            stmt.execute();

            // Retrieve the generated reservation ID
            int reservationId = stmt.getInt(5);
            System.out.println("Created Reservation ID: " + reservationId); // Debug output
            return reservationId;
        }
    }

    private void addPackageToReservation(Connection conn, int reservationId) throws SQLException {
        int currentUserId = Model.getInstance().getcurrentuserid();

        String sql = "{CALL sp_add_reservation_item(?, ?, ?, ?)}";

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, reservationId);
            cstmt.setNull(2, Types.INTEGER); // No equipment_id
            cstmt.setInt(3, selectedPackage.getPackageId()); // Only package_id
            cstmt.setInt(4, currentUserId);

            cstmt.execute();
        }
    }

    private boolean validDates() {
        if (start_date.getValue() == null || end_date.getValue() == null) {
            return false;
        }
        if (!end_date.getValue().isAfter(start_date.getValue())) {
            showError("Invalid Dates", "End date must be after start date");
            return false;
        }
        return true;
    }

    private boolean validateReservation() {
        if (customer_cmb.getValue() == null) {
            showError("Missing Customer", "Please select a customer");
            return false;
        }
        System.out.println("Customer ID: " + customer_cmb.getValue().getCustomerId());

        if (selectedPackage == null) {
            showError("Missing Package", "Please select a package");
            return false;
        }
        System.out.println("Selected Package ID: " + selectedPackage.getPackageId());

        if (!validDates()) {
            return false;
        }

        Customer selectedCustomer = customer_cmb.getValue();
        LocalDate startDate = start_date.getValue();
        LocalDate endDate = end_date.getValue();

        if (checkForOverlappingReservations(selectedCustomer, startDate, endDate)) {
            showError("Duplicate Reservation", "This customer already has a reservation within the specified date range.");
            return false;
        }
        return true;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void resetForm() {
        // Reset customer combo box
        customer_cmb.getSelectionModel().clearSelection();

        // Reset date pickers to default values
        start_date.setValue(LocalDate.now());
        end_date.setValue(LocalDate.now().plusDays(7));

        // Clear the selected package and package contents list
        package_table.getSelectionModel().clearSelection();
        packageContentsList.setItems(FXCollections.emptyObservableList());

        // Reset status combo box to "All"
        status_cmb.getSelectionModel().selectFirst();
    }

    private boolean checkForOverlappingReservations(Customer customer, LocalDate startDate, LocalDate endDate) {
        String query = "SELECT COUNT(*) AS count FROM reservations "
                + "WHERE customer_id = ? "
                + "AND (start_datetime <= ? AND end_datetime >= ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, customer.getCustomerId());
            pst.setDate(2, Date.valueOf(endDate));
            pst.setDate(3, Date.valueOf(startDate));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0; // Return true if overlapping reservations exist
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to check for overlapping reservations: " + e.getMessage());
        }
        return false;
    }

    private void loadReservations() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM current_rentals";

            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            ObservableList<Reservation> reservations = FXCollections.observableArrayList();
            while (rs.next()) {
                // Create Customer object
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("address")
                );

                // Create Package object
                Package pkg = new Package(
                        rs.getInt("package_id"),
                        rs.getString("package_name"),
                        rs.getDouble("price"),
                        "" // Status will be empty here as it's not relevant in this context
                );

                // Create Reservation object
                Reservation reservation = new Reservation();
                reservation.setReservationId(rs.getInt("reservation_id"));
                reservation.setCustomer(customer);
                reservation.setPkg(pkg);
                reservation.setStartDate(rs.getDate("start_datetime").toLocalDate());
                reservation.setEndDate(rs.getDate("end_datetime").toLocalDate());
                reservation.setStatus(rs.getString("status"));
                reservation.setOverdueCharges((rs.getDouble("overdue_charges")));

                reservation.setReservationId(rs.getInt("reservation_id"));
                System.out.println("Loaded Reservation ID: " + rs.getInt("reservation_id"));
                System.out.println("Customer Name: " + rs.getString("customer_name"));
                System.out.println("Package Name: " + rs.getString("package_name"));

                reservations.add(reservation);
            }
            masterReservationList = reservations;
            reservation_table.setItems(masterReservationList);

        } catch (SQLException e) {
            showError("Database Error", "Failed to load reservations: " + e.getMessage());
        }
    }

}
