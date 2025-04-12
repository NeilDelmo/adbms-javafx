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
import javafx.scene.control.Alert;

public class ReservationController implements Initializable {

    @FXML
    private Button add_item_btn;

    @FXML
    private TextField amount_textfield;

    @FXML
    private Button check_availability_btn;

    @FXML
    private TableColumn<?, ?> customerCol;

    @FXML
    private ComboBox<Customer> customer_cmb;

    @FXML
    private TableColumn<?, ?> deliveryCol;

    @FXML
    private CheckBox delivery_checkbox;

    @FXML
    private TextArea delivery_txtarea;

    @FXML
    private DatePicker end_date;

    @FXML
    private TableView<RentableItem> equipment_table;

    @FXML
    private TableColumn<RentableItem, String> nameCol;

    @FXML
    private ComboBox<?> payment_method_cmb;

    @FXML
    private Label penalty_label;

    @FXML
    private TableColumn<?, ?> periodCol;

    @FXML
    private TableColumn<RentableItem, Double> priceCol;

    @FXML
    private Button record_btn;

    @FXML
    private Button remove_item_btn;

    @FXML
    private TableView<?> reservation_table;

    @FXML
    private ListView<?> selected_list;

    @FXML
    private DatePicker start_date;

    @FXML
    private TableColumn<RentableItem, String> statusCol;

    @FXML
    private ComboBox<?> status_cmb;

    @FXML
    private TableColumn<RentableItem, String> typeCol;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadCustomers(); // Add this
        setupDateListener(); // Add this
    }

    private void setupTableColumns() {
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadCustomers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT customer_id, name FROM customers";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                customer_cmb.getItems().add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", " Failed to load customers: " + e.getMessage());
        }

    }

    private void setupDateListener() {
        start_date.valueProperty().addListener((obs, oldVal, newVal) -> checkAvailability());
        end_date.valueProperty().addListener((obs, oldVal, newVal) -> checkAvailability());
    }

    private void checkAvailability() {
        if (start_date.getValue() == null || end_date.getValue() == null) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall("{call sp_check_equipment_availability(?, ?, ?)}")) {

            stmt.setDate(1, Date.valueOf(start_date.getValue()));
            stmt.setDate(2, Date.valueOf(end_date.getValue()));
            stmt.setInt(3, Model.getInstance().getcurrentuserid());

            ResultSet rs = stmt.executeQuery();
            equipment_table.getItems().clear();

            while (rs.next()) {
                // Check if it's equipment or package
                if (rs.getString("item_type").equalsIgnoreCase("equipment")) {
                    equipment_table.getItems().add(new Equipment(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("availability_status")
                    ));
                } else {
                    equipment_table.getItems().add(new RentalPackage(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("availability_status")
                    ));
                }
            }
        } catch (SQLException e) {
            showError("Availability Check Failed", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public class Customer {

        private final int id;
        private final String name;

        public Customer(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name; // This determines what's displayed in the ComboBox
        }
    }

    public interface RentableItem {

        int getId();

        String getName();

        String getType();

        double getPrice();

        String getStatus();
    }

    public class Equipment implements RentableItem {

        private final int id;
        private final String name;
        private final double price;
        private final String status;

        public Equipment(int id, String name, double price, String status) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.status = status;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getType() {
            return "Equipment";
        }

        @Override
        public double getPrice() {
            return price;
        }

        @Override
        public String getStatus() {
            return status;
        }
    }

    public class RentalPackage implements RentableItem {

        private final int id;
        private final String name;
        private final double price;
        private final String status;

        public RentalPackage(int id, String name, double price, String status) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.status = status;
        }

        @Override  // Add proper annotation
        public int getId() {  // Rename from getid() to getId()
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getType() {  // Add this method
            return "Package";
        }

        @Override
        public double getPrice() {
            return price;
        }

        @Override
        public String getStatus() {
            return status;
        }
    }

}
