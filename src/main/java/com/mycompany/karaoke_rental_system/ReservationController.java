package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;

public class ReservationController implements Initializable {

    @FXML
    private TableColumn<Reservation, String> statusCol;

    @FXML
    private Button add_item_btn;

    @FXML
    private TextField amount_textfield;

    @FXML
    private TableColumn<Package, Double> bundlePriceCol;

    @FXML
    private TableColumn<Reservation, String> customerCol;

    @FXML
    private ComboBox<Customer> customer_cmb;

    @FXML
    private TableColumn<Reservation, String> addressCol;

    @FXML
    private CheckBox delivery_checkbox;

    @FXML
    private TextArea delivery_txtarea;

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
    private ComboBox<String> payment_method_cmb;

    @FXML
    private Label penalty_label;

    @FXML
    private TableColumn<Reservation, String> periodCol;

    @FXML
    private Button record_btn;

    @FXML
    private TableView<Reservation> reservation_table;

    @FXML
    private DatePicker start_date;

    @FXML
    private ComboBox<String> status_cmb;

    @FXML
    private TableColumn<Reservation, String> packageCol;

    private ObservableList<Package> packageList;
    private ObservableList<Customer> customerList;

    private Package selectedPackage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TableColumnsForPackage();
        loadCustomers(); // Add this
        setupDateListener(); // Add this
        setupPackageSelection();
        setupListView();
        TableColumnsForReservation();
        loadReservations();

        start_date.setValue(LocalDate.now());
        end_date.setValue(LocalDate.now().plusDays(7));

        status_cmb.getItems().addAll("All", "Rented", "Available");
        status_cmb.getSelectionModel().selectFirst();

        status_cmb.valueProperty().addListener((obs, oldVal, newVal) -> checkAvailability());

        checkAvailability();

        delivery_txtarea.visibleProperty().bind(delivery_checkbox.selectedProperty());
        delivery_txtarea.managedProperty().bind(delivery_checkbox.selectedProperty());
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
    customerCol.setCellValueFactory(cellData -> 
        new SimpleStringProperty(cellData.getValue().getCustomer().getName()));
    // Period (Start Date - End Date)
    periodCol.setCellValueFactory(cellData -> {
        Reservation res = cellData.getValue();
        String period = res.getStartDate().toString() + " - " + res.getEndDate().toString();
        return new SimpleStringProperty(period);
    });
    // Customer Address
    addressCol.setCellValueFactory(cellData -> 
        new SimpleStringProperty(cellData.getValue().getCustomer().getAddress()));
    // Package Name
    packageCol.setCellValueFactory(cellData -> 
        new SimpleStringProperty(cellData.getValue().getPkg().getName()));
    // Reservation Status
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
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
            showError("Reservation Failed", "Error creating reservation: " + e.getMessage());
        }
    }

    private int createReservation(Connection conn) throws SQLException {
        String sql = "{call sp_create_reservation(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, customer_cmb.getValue().getCustomerId());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(start_date.getValue().atStartOfDay()));
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(end_date.getValue().atStartOfDay()));
            stmt.setBoolean(4, delivery_checkbox.isSelected());
            stmt.setString(5, delivery_txtarea.getText());
            stmt.setInt(6, Model.getInstance().getcurrentuserid());
            stmt.registerOutParameter(7, Types.INTEGER);

            stmt.execute();
            return stmt.getInt(7);
        }
    }

    private void addPackageToReservation(Connection conn, int reservationId) throws SQLException {
        String sql = "INSERT INTO reservation_items (reservation_id, package_id, price_per_unit, added_by) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, reservationId);
            pst.setInt(2, selectedPackage.getPackageId());
            pst.setDouble(3, selectedPackage.getBundlePrice());
            pst.setInt(4, Model.getInstance().getcurrentuserid());
            pst.executeUpdate();
        }
    }

    private boolean validDates() {
        if (start_date.getValue() == null || end_date.getValue() == null) {
            return false;
        }
        if (end_date.getValue().isBefore(start_date.getValue())) {
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
    if (selectedPackage == null) {
        showError("Missing Package", "Please select a package");
        return false;
    }
    if (!validDates()) {
        return false;
    }

    // Check for overlapping reservations
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

    // Uncheck the delivery checkbox and clear the delivery text area
    delivery_checkbox.setSelected(false);
    delivery_txtarea.clear();

    // Clear the selected package and package contents list
    package_table.getSelectionModel().clearSelection();
    packageContentsList.setItems(FXCollections.emptyObservableList());

    // Reset status combo box to "All"
    status_cmb.getSelectionModel().selectFirst();

    // Optionally, clear any other fields (e.g., amount_textfield)
    amount_textfield.clear();
}
    private boolean checkForOverlappingReservations(Customer customer, LocalDate startDate, LocalDate endDate) {
    String query = "SELECT COUNT(*) AS count FROM reservations "
                 + "WHERE customer_id = ? "
                 + "AND (start_date <= ? AND end_date >= ?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(query)) {
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
                ""  // Status will be empty here as it's not relevant in this context
            );
            
            // Create Reservation object
            Reservation reservation = new Reservation();
            reservation.setCustomer(customer);
            reservation.setPkg(pkg);
            reservation.setStartDate(rs.getDate("start_datetime").toLocalDate());
            reservation.setEndDate(rs.getDate("end_datetime").toLocalDate());
            reservation.setStatus(rs.getString("status"));
            
            reservations.add(reservation);
        }
        
        reservation_table.setItems(reservations);
        
    } catch (SQLException e) {
        showError("Database Error", "Failed to load reservations: " + e.getMessage());
    }
}
    

}
