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
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;

public class EquipmentController implements Initializable {

    @FXML
    private Button add_equipment_btn;

    @FXML
    private Button delete_equipment_btn;

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
    private TableColumn<?, ?> package_decriptionCol;

    @FXML
    private TableColumn<?, ?> package_nameCol;

    @FXML
    private TableColumn<?, ?> package_priceCol;
    @FXML
    private TextField package_searchfield;

    @FXML
    private ComboBox<?> package_status_filter;
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
        delete_equipment_btn.setOnAction(e -> deleteSelectedEquipment());

        // Initialize the status filter combo box
        status_filter_cmb.getItems().addAll("Available", "Rented", "Maintenance");
        status_filter_cmb.setOnAction(e -> filterEquipmentByStatus()); // Add event handler

        // Add search functionality
        search_txtfield.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEquipmentList(newValue);
        });
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(255, 255, 255, 0.6));
        dropShadow.setRadius(10);
        dropShadow.setSpread(0.5);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);

        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.web("#00bfff"));
        innerShadow.setRadius(10);

// Chain the effects
        dropShadow.setInput(innerShadow);
        addHoverEffect(add_equipment_btn, dropShadow);
        addHoverEffect(edit_equipment_btn, dropShadow);
        addHoverEffect(delete_equipment_btn, dropShadow);
        addHoverEffect(view_maintenance_btn, dropShadow);

    }

    private void addHoverEffect(Button button, DropShadow normalEffect) {
        button.setOnMouseEntered(e -> {
            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.rgb(255, 255, 255, 0.8));
            hoverShadow.setRadius(15);
            hoverShadow.setSpread(0.6);
            hoverShadow.setOffsetX(0);
            hoverShadow.setOffsetY(4);

            InnerShadow hoverInner = new InnerShadow();
            hoverInner.setColor(Color.web("#00bfff"));
            hoverInner.setRadius(15);

            hoverShadow.setInput(hoverInner);

            button.setEffect(hoverShadow);
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });

        button.setOnMouseExited(e -> {
            button.setEffect(normalEffect);
            button.setScaleX(1);
            button.setScaleY(1);
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
                        resultSet.getString("status")
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

    private void deleteSelectedEquipment() {
        Equipment selectedEquipment = equipment_table.getSelectionModel().getSelectedItem();
        if (selectedEquipment != null) {
            String deleteQuery = "DELETE FROM equipment WHERE equipment_id = ?";
            try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(deleteQuery)) {

                statement.setInt(1, selectedEquipment.getEquipmentId());
                statement.executeUpdate();

                // Remove from the observable list
                equipmentList.remove(selectedEquipment);
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    private void refreshEquipmentTable() {
        // Logic to refresh the table, possibly reloading data from the database
        // For now, just clear and re-add dummy data
        equipmentList.clear();
        equipmentList.addAll(/* reload data from database */);
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
                        resultSet.getDouble("bundle_price")
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
            String deleteQuery = "DELETE FROM packages WHERE package_id = ?";
            try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(deleteQuery)) {

                statement.setInt(1, selectedPackage.getPackageId());
                statement.executeUpdate();

                // Remove from the observable list
                packageList.remove(selectedPackage);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
