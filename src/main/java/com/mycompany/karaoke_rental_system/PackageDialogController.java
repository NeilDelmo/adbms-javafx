package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;

public class PackageDialogController {

    public TableColumn<Equipment, Integer> idCoL;
    public TableColumn<Equipment, String> nameCol;
    public TableColumn<Equipment, Double> priceCol;
    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField bundlePriceField;
    @FXML
    private TableView<Equipment> equipmentTable;
    @FXML
    private ListView<Equipment> selectedEquipmentList;

    private ObservableList<Equipment> equipmentList;
    private ObservableList<Equipment> selectedEquipment;

    private Stage dialogStage;
    private Package packageData;
    private boolean isEditMode;

    @FXML
    private void initialize() {
        equipmentList = FXCollections.observableArrayList();
        selectedEquipment = FXCollections.observableArrayList();

        idCoL.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getEquipmentId()).asObject());
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        priceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getRentalPrice()).asObject());

        // Initialize the table columns
        equipmentTable.setItems(equipmentList);
        selectedEquipmentList.setItems(selectedEquipment);

        // Load equipment data into the table
        loadEquipmentData();
    }

    private void loadEquipmentData() {
        Connection conn = DatabaseConnection.getConnection();
        String query;
        if (isEditMode) {
            query = "SELECT e.equipment_id, e.name, e.description, e.rental_price, e.overdue_penalty, e.status, " +
                    "e.created_by, e.created_at, e.updated_by " + // Include missing columns
                    "FROM equipment e " +
                    "WHERE e.equipment_id NOT IN (" +
                    "   SELECT pe.equipment_id FROM package_equipment pe WHERE pe.package_id != ?" +
                    ")";
        } else {
            query = "SELECT e.equipment_id, e.name, e.description, e.rental_price, e.overdue_penalty, e.status, " +
                    "e.created_by, e.created_at, e.updated_by " + // Include missing columns
                    "FROM equipment e " +
                    "WHERE e.equipment_id NOT IN (SELECT equipment_id FROM package_equipment)";
        }
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            if (isEditMode) {
                statement.setInt(1, packageData.getPackageId());
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Equipment equipment = new Equipment(
                        resultSet.getInt("equipment_id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("rental_price"),
                        resultSet.getDouble("overdue_penalty"),
                        resultSet.getString("status"),
                        resultSet.getInt("created_by"), // Now available in the result set
                        resultSet.getTimestamp("created_at"), // Now available in the result set
                        resultSet.getInt("updated_by") // Now available in the result set
                );
                equipmentList.add(equipment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddEquipment() {
        Equipment selectedEquipmentItem = equipmentTable.getSelectionModel().getSelectedItem();
        if (selectedEquipmentItem != null) {
            selectedEquipment.add(selectedEquipmentItem);
            equipmentList.remove(selectedEquipmentItem);
            updateBundlePrice();
        }
    }

    @FXML
    private void handleRemoveEquipment() {
        Equipment selectedEquipmentItem = selectedEquipmentList.getSelectionModel().getSelectedItem();
        if (selectedEquipmentItem != null) {
            selectedEquipment.remove(selectedEquipmentItem);
            equipmentList.add(selectedEquipmentItem);
            updateBundlePrice();
        }
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText();
        String description = descriptionField.getText();
        String bundlePriceText = bundlePriceField.getText();

        if (name.isEmpty() || description.isEmpty()) {
            showAlert("Input Error", "Name and Description are required fields.");
            return;
        }

        double bundlePrice;
        try {
            bundlePrice = Double.parseDouble(bundlePriceText);
            if (bundlePrice <= 0) {
                showAlert("Input Error", "Bundle Price must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid number for Bundle Price.");
            return;
        }

        // Validate selected equipment
        if (selectedEquipment.isEmpty()) {
            showAlert("Input Error", "At least one equipment item must be selected.");
            return;
        }

        // Validate user ID
        int currentUserId = Model.getInstance().getcurrentuserid();
        if (currentUserId <= 0) {
            showAlert("Authentication Error", "No valid user session. Please login again.");
            return;
        }

        if (isEditMode) {
            updatePackage(name, description, bundlePrice);
        } else {
            savePackage(name, description, bundlePrice);
        }
        dialogStage.close();
    }

    private void savePackage(String name, String description, double bundlePrice) {
        Connection conn = DatabaseConnection.getConnection();
        String insertPackageQuery = "INSERT INTO packages (name, description, bundle_price, created_by) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(insertPackageQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setDouble(3, bundlePrice);
            statement.setInt(4, Model.getInstance().getcurrentuserid()); 

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int packageId = generatedKeys.getInt(1);
                        savePackageEquipment(packageId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void savePackageEquipment(int packageId) {
        Connection conn = DatabaseConnection.getConnection();
        String insertPackageEquipmentQuery = "INSERT INTO package_equipment (package_id, equipment_id) VALUES (?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(insertPackageEquipmentQuery)) {
            for (Equipment equipment : selectedEquipment) {
                statement.setInt(1, packageId);
                statement.setInt(2, equipment.getEquipmentId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPackageData(Package packageData, boolean isEditMode) {
        this.packageData = packageData;
        this.isEditMode = isEditMode;

        if (isEditMode) {
            nameField.setText(packageData.getName());
            descriptionField.setText(packageData.getDescription());
            bundlePriceField.setText(String.valueOf(packageData.getBundlePrice()));
            // Load existing package equipment into selectedEquipmentList
            loadPackageEquipment(packageData.getPackageId());
        }
    }

    private void loadPackageEquipment(int packageId) {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT e.equipment_id, e.name, e.description, e.rental_price, e.overdue_penalty, e.status, " +
                "e.created_by, e.created_at, e.updated_by " + // Include missing columns
                "FROM package_equipment pe JOIN equipment e ON pe.equipment_id = e.equipment_id " +
                "WHERE pe.package_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, packageId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Equipment equipment = new Equipment(
                            resultSet.getInt("equipment_id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getDouble("rental_price"),
                            resultSet.getDouble("overdue_penalty"),
                            resultSet.getString("status"),
                            resultSet.getInt("created_by"), // Now available in the result set
                            resultSet.getTimestamp("created_at"), // Now available in the result set
                            resultSet.getInt("updated_by") // Now available in the result set
                    );
                    selectedEquipment.add(equipment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePackage(String name, String description, double bundlePrice) {
        Connection conn = DatabaseConnection.getConnection();
        String updatePackageQuery = "UPDATE packages SET name = ?, description = ?, bundle_price = ?, updated_by = ? WHERE package_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(updatePackageQuery)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setDouble(3, bundlePrice);
            statement.setInt(4,Model.getInstance().getcurrentuserid());
            statement.setInt(5, packageData.getPackageId());

            statement.executeUpdate();

            // Update package equipment
            deleteExistingPackageEquipment();
            savePackageEquipment(packageData.getPackageId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteExistingPackageEquipment() {
        Connection conn = DatabaseConnection.getConnection();
        String deleteQuery = "DELETE FROM package_equipment WHERE package_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(deleteQuery)) {
            statement.setInt(1, packageData.getPackageId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void updateBundlePrice() {
        double total = selectedEquipment.stream()
                .mapToDouble(Equipment::getRentalPrice)
                .sum();
        bundlePriceField.setText(String.format("%.2f", total)); // Update the text field
    }
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
