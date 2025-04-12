package com.mycompany.karaoke_rental_system;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;

public class PackageDialogController {

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
    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private ObservableList<Equipment> equipmentList;
    private ObservableList<Equipment> selectedEquipment;

    private Stage dialogStage;
    private Package packageData;
    private boolean isEditMode;

    @FXML
    private void initialize() {
        equipmentList = FXCollections.observableArrayList();
        selectedEquipment = FXCollections.observableArrayList();

        // Initialize the table columns
        equipmentTable.setItems(equipmentList);
        selectedEquipmentList.setItems(selectedEquipment);

        // Load equipment data into the table
        loadEquipmentData();
    }

  private void loadEquipmentData() {
    Connection conn = DatabaseConnection.getConnection();

    String query = "SELECT equipment_id, name, description, rental_price, overdue_penalty, status FROM equipment";
    try (PreparedStatement statement = conn.prepareStatement(query);
         ResultSet resultSet = statement.executeQuery()) {

        while (resultSet.next()) {
            Equipment equipment = new Equipment(
                resultSet.getInt("equipment_id"),
                resultSet.getString("name"),
                resultSet.getString("description"), // Include description
                resultSet.getDouble("rental_price"),
                resultSet.getDouble("overdue_penalty"), // Include overdue penalty
                resultSet.getString("status") // Include status
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
        }
    }

    @FXML
    private void handleRemoveEquipment() {
        Equipment selectedEquipmentItem = selectedEquipmentList.getSelectionModel().getSelectedItem();
        if (selectedEquipmentItem != null) {
            selectedEquipment.remove(selectedEquipmentItem);
        }
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText();
        String description = descriptionField.getText();
        double bundlePrice = Double.parseDouble(bundlePriceField.getText());

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
            statement.setInt(4, 1); // Assuming a default user ID for created_by

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
    String query = "SELECT e.equipment_id, e.name, e.description, e.rental_price, e.overdue_penalty, e.status " +
                   "FROM package_equipment pe JOIN equipment e ON pe.equipment_id = e.equipment_id " +
                   "WHERE pe.package_id = ?";
    try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setInt(1, packageId);
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Equipment equipment = new Equipment(
                    resultSet.getInt("equipment_id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"), // Include description
                    resultSet.getDouble("rental_price"),
                    resultSet.getDouble("overdue_penalty"), // Include overdue penalty
                    resultSet.getString("status") // Include status
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
        String updatePackageQuery = "UPDATE packages SET name = ?, description = ?, bundle_price = ? WHERE package_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(updatePackageQuery)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setDouble(3, bundlePrice);
            statement.setInt(4, packageData.getPackageId());

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
}
