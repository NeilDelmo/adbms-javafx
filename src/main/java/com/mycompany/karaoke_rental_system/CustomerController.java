package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class CustomerController implements Initializable {
    @FXML
    private Button edit_btn;

    @FXML
    private VBox rootPane;

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

        customer_table.setEditable(true);
        reservation_btn.setOnAction(e -> {
            Customer selectedCustomer = customer_table.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                Model.getInstance().getViewFactory().setSelectedCustomer(selectedCustomer);
                BorderPane parent = (BorderPane) rootPane.getParent();
                parent.setCenter(Model.getInstance().getViewFactory().getReservationView());
            } else {
                showAlert("Selection Error", "Please select a customer to proceed.");
            }
        });

    }

    private void setupTableColumns() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event ->{
            Customer customer = event.getRowValue();
            String newName = event.getNewValue().trim();
            if(!newName.isEmpty()){
                updateCustomerField(customer.getCustomerId(),"name",newName);
                customer.setName(newName);
            }else{
                showAlert("Error","Name cannot be empty");
            }
        });

        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setCellFactory((TextFieldTableCell.forTableColumn()));
        phoneCol.setOnEditCommit(event ->{
            Customer customer = event.getRowValue();
            String newPhone = event.getNewValue().trim();
            if(!newPhone.isEmpty()){
                updateCustomerField(customer.getCustomerId(),"phone",newPhone);
                customer.setPhone(newPhone);
            }else{
                showAlert("Error","Phone cannot be empty");
            }
        });

        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setCellFactory(TextFieldTableCell.forTableColumn());
        addressCol.setOnEditCommit(event ->{
            Customer customer = event.getRowValue();
            String newAddress = event.getNewValue().trim();
            if(!newAddress.isEmpty()) {
                updateCustomerField(customer.getCustomerId(),"address",newAddress);
                customer.setAddress(newAddress);
            }else{
                showAlert("Error","Address Cannot be empty");
            }
        });

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

    private void updateCustomerField(int customerId, String field, String newValue) {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall("{call sp_update_customer(?, ?, ?, ?, ?)}")) {

            Customer customer = CustomerDAO.getCustomerById(customerId);
            if (customer == null) {
                showAlert("Error", "Customer not found.");
                return;
            }

            cstmt.setInt(1, customerId);
            cstmt.setString(2, field.equals("name") ? newValue : customer.getName());
            cstmt.setString(3, field.equals("phone") ? newValue : customer.getPhone());
            cstmt.setString(4, field.equals("address") ? newValue : customer.getAddress());
            cstmt.setInt(5, Model.getInstance().getcurrentuserid());

            cstmt.execute();

        } catch (SQLException e) {
            showAlert("Database Error", "Error updating customer: " + e.getMessage());
        }
    }



    private void saveChanges(Customer customer) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE customers SET name = ?, phone = ?, address = ? WHERE customer_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, customer.getName());
            pst.setString(2, customer.getPhone());
            pst.setString(3, customer.getAddress());
            pst.setInt(4, customer.getCustomerId());
            pst.executeUpdate();
        } catch (SQLException e) {
            showAlert("Error", "Error Saving Changes " + e.getMessage());
        } finally {
            // Disable editing after saving
            nameCol.setEditable(false);
            phoneCol.setEditable(false);
            addressCol.setEditable(false);
            customer_table.refresh();
        }
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

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall("{call sp_insert_customer(?, ?, ?, ?, ?)}")) {

            cstmt.setString(1, name);
            cstmt.setString(2, phone);
            cstmt.setString(3, address);
            cstmt.setInt(4, Model.getInstance().getcurrentuserid());
            cstmt.registerOutParameter(5, Types.INTEGER);

            cstmt.execute();

            int newCustomerId = cstmt.getInt(5);
            System.out.println("New customer ID: " + newCustomerId);

            clearFields();
            loadCustomers();

        } catch (SQLException e) {
            showAlert("Database Error", "Error saving customer: " + e.getMessage());
        }
    }
    @FXML
    private void handleEditRow() {
        Customer selectedCustomer = customer_table.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            int rowIndex = customer_table.getSelectionModel().getSelectedIndex();
            customer_table.edit(rowIndex, nameCol); // Start editing at the name column
        } else {
            showAlert("Selection Error", "Please select a row to edit.");
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
                + "WHERE c.created_by = ? "
                + "GROUP BY c.customer_id";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, Model.getInstance().getcurrentuserid());
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
    
    public void refreshData() {
    loadCustomers(); // Reload customers from the database
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

        @Override
        public String toString() {
            return String.format("%s | %s to %s | Status: %s | Total: ₱%,.2f",
                    customerName,
                    startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                    endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                    status,
                    totalValue);
        }

    }

}
