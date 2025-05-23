package com.mycompany.karaoke_rental_system;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.control.Alert;

public class EquipmentController implements Initializable {

    @FXML
    private Button add_equipment_btn;

    @FXML
    private TableColumn<Equipment, String> descriptionCol;

    @FXML
    private Button edit_equipment_btn;

    @FXML
    private TableView<Equipment> equipment_table;

    @FXML
    private TableColumn<Equipment, String> nameCol;

    @FXML
    private TableColumn<Equipment, Double> penaltyCol;

    @FXML
    private TableColumn<Equipment, Double> priceCol;

    @FXML
    private TextField search_txtfield;

    @FXML
    private TableColumn<Equipment, String> statusCol;

    @FXML
    private ComboBox<String> status_filter_cmb;

    @FXML
    private Button view_maintenance_btn;

    //package tabbedpane
    @FXML
    private Button edit_package_btn;
    @FXML
    private TableColumn<Package, String> package_decriptionCol;

    @FXML
    private TableColumn<Package, String> package_nameCol;

    @FXML
    private TableColumn<Package, Double> package_priceCol;
    @FXML
    private TextField package_searchfield;

    @FXML
    private ComboBox<String> package_status_filter;
    @FXML
    private Button add_package_btn;

    @FXML
    private Button config_package_btn;
    @FXML
    private TableView<Package> package_table;

    private ObservableList<Equipment> equipmentList;
    private ObservableList<Package> packageList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the table columns
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("rentalPrice"));
        penaltyCol.setCellValueFactory(new PropertyValueFactory<>("overduePenalty"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Initialize the equipment list and load data
        equipmentList = FXCollections.observableArrayList();
        equipment_table.setItems(equipmentList);
        loadEquipmentData(); // Load data after initializing the list

        package_nameCol.setCellValueFactory((new PropertyValueFactory<>("name")));
        package_decriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        package_priceCol.setCellValueFactory(new PropertyValueFactory<>("bundlePrice"));

        package_status_filter.getItems().addAll("Active", "Inactive");
        package_status_filter.setOnAction(e -> filterPackageByStatus());

        packageList = FXCollections.observableArrayList();
        package_table.setItems(packageList);
        loadPackageData(); // Load data after initializing the list

        // Add event handlers
        add_package_btn.setOnAction(e -> openPackageDialog(null));
        edit_package_btn.setOnAction(e -> {
            Package selectedPackage = package_table.getSelectionModel().getSelectedItem();
            if (selectedPackage != null) {
                openPackageDialog(selectedPackage);
            }
        });
        add_equipment_btn.setOnAction(e -> openEquipmentDialog(null));
        edit_equipment_btn.setOnAction(e -> {
            Equipment selectedEquipment = equipment_table.getSelectionModel().getSelectedItem();
            if (selectedEquipment != null) {
                openEquipmentDialog(selectedEquipment);
            }
        });

        // Initialize the status filter combo box
        status_filter_cmb.getItems().addAll("Available", "Rented", "Maintenance");
        status_filter_cmb.setOnAction(e -> filterEquipmentByStatus()); // Add event handler

        // Add search functionality
        search_txtfield.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEquipmentList(newValue);
        });
        
        package_searchfield.textProperty().addListener((observable,oldValue,newValue) ->{
        filterPackageList(newValue);     
    });

    }

    private void loadEquipmentData() {
        equipmentList.clear();
        String query = "SELECT * FROM equipment";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Equipment equipment = new Equipment(
                        resultSet.getInt("equipment_id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("rental_price"),
                        resultSet.getDouble("overdue_penalty"),
                        resultSet.getString("status"),
                        resultSet.getInt("created_by"),
                        resultSet.getTimestamp("created_at"),
                        resultSet.getInt("updated_by")
                );
                equipmentList.add(equipment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openEquipmentDialog(Equipment equipment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EquipmentDialog.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            EquipmentDialogController controller = loader.getController();
            controller.setEquipment(equipment);

            stage.showAndWait();

            // Refresh the table after the dialog is closed
            loadEquipmentData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterEquipmentList(String searchText) {
        ObservableList<Equipment> filteredList = FXCollections.observableArrayList();
        for (Equipment equipment : equipmentList) {
            if (equipment.getName().toLowerCase().contains(searchText.toLowerCase())
                    || equipment.getDescription().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(equipment);
            }
        }
        equipment_table.setItems(filteredList);
    }
    
    private void filterPackageList(String searchText){
        ObservableList<Package> filteredList = FXCollections.observableArrayList();
        for(Package pkg : packageList){
            if(pkg.getName().toLowerCase().contains(searchText.toLowerCase())
                    ||pkg.getDescription().toLowerCase().contains(searchText.toLowerCase())){
                filteredList.add(pkg);
            }
        }
        package_table.setItems(filteredList);
    }

    private void filterEquipmentByStatus() {
        String selectedStatus = status_filter_cmb.getValue();
        ObservableList<Equipment> filteredList = FXCollections.observableArrayList();
        if (selectedStatus == null || selectedStatus.isEmpty()) {
            filteredList.addAll(equipmentList);
        } else {
            for (Equipment equipment : equipmentList) {
                if (equipment.getStatus().equalsIgnoreCase(selectedStatus)) {
                    filteredList.add(equipment);
                }
            }
        }
        equipment_table.setItems(filteredList);
    }

    private void loadPackageData() {
        packageList.clear();
        String query = "SELECT * FROM packages";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Package pkg = new Package(
                        resultSet.getInt("package_id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("bundle_price"),
                        resultSet.getString("status"),
                        resultSet.getInt("created_by"),
                        resultSet.getTimestamp("created_at"),
                        resultSet.getInt("updated_by")
                );
                packageList.add(pkg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openPackageDialog(Package pkg) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PackageDialog.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);

            PackageDialogController controller = loader.getController();
            controller.setPackageData(pkg, pkg != null); // Pass package data and edit mode
            controller.setDialogStage(stage);

            stage.showAndWait();

            // Refresh the table after the dialog is closed
            loadPackageData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

 private void deleteSelectedPackage() {
    Package selectedPackage = package_table.getSelectionModel().getSelectedItem();
    if (selectedPackage != null) {
        // Check if the package is used in any reservation
        String checkQuery = "SELECT COUNT(*) FROM rental_items WHERE package_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, selectedPackage.getPackageId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Show warning and block deletion
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Deletion Blocked");
                alert.setHeaderText("Cannot delete package");
                alert.setContentText("This package is linked to existing rental. Delete rental first.");
                alert.showAndWait();
                return; // Exit the method to prevent further execution
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally show an error dialog to inform the user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to check package usage");
            alert.setContentText("An error occurred while checking if the package is in use.");
            alert.showAndWait();
            return; // Exit the method on database error
        }

        // Proceed with deletion if no reservations exist
        String deleteQuery = "DELETE FROM packages WHERE package_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, selectedPackage.getPackageId());
            int rowsAffected = deleteStmt.executeUpdate();
            if (rowsAffected > 0) {
                // Remove the package from the observable list
                packageList.remove(selectedPackage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally show an error dialog to inform the user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to delete package");
            alert.setContentText("An error occurred while deleting the package.");
            alert.showAndWait();
        }
    } else {
        // Inform the user if no package is selected
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Selection");
        alert.setHeaderText("No package selected");
        alert.setContentText("Please select a package to delete.");
        alert.showAndWait();
    }
}

    private void filterPackageByStatus() {
    // Get the selected status from the combo box
    String selectedStatus = (String) package_status_filter.getValue();

    // Create a new observable list to hold filtered packages
    ObservableList<Package> filteredList = FXCollections.observableArrayList();

    // Check if no status is selected or the combo box is empty
    if (selectedStatus == null || selectedStatus.isEmpty()) {
        // If no status is selected, show all packages
        filteredList.addAll(packageList);
    } else {
        // Filter packages by the selected status
        for (Package pkg : packageList) { // Iterate through the full package list
            if (pkg.getStatus().equalsIgnoreCase(selectedStatus)) { // Compare status (case-insensitive)
                filteredList.add(pkg); // Add matching packages to the filtered list
            }
        }
    }

    // Update the package table with the filtered list
    package_table.setItems(filteredList);
}
    }
