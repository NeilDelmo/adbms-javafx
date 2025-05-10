package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class PaymentController implements Initializable {
    public PieChart methodPiechart;
    // FXML Injections
    @FXML private ComboBox<RentalWrapper> rentalCombo;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> methodCombo;
    @FXML private Label balanceLabel;
    @FXML private ProgressBar paymentProgress;
    @FXML private Label statusBadge;
    @FXML private Label overdueAlert;
    @FXML private Button submitPaymentBtn;

    // Table Views
    @FXML private TableView<PaymentSummary> paymentSummaryTable;
    @FXML private TableColumn<PaymentSummary, Integer> reservationIdCol;
    @FXML private TableColumn<PaymentSummary, Double> totalAmountCol;
    @FXML private TableColumn<PaymentSummary, Double> amountPaidCol;
    @FXML private TableColumn<PaymentSummary, Double> balanceCol;
    @FXML private TableColumn<PaymentSummary, String> statusCol;

    @FXML private TableView<PaymentHistory> paymentHistoryTable;
    @FXML private TableColumn<PaymentHistory, String> dateCol;
    @FXML private TableColumn<PaymentHistory, Double> amountCol;
    @FXML private TableColumn<PaymentHistory, String> methodCol;
    @FXML private TableColumn<PaymentHistory, String> recordedByCol;

    private ObservableList<RentalWrapper> unpaidRentals = FXCollections.observableArrayList();
    private ObservableList<PaymentSummary> paymentSummaries = FXCollections.observableArrayList();
    private ObservableList<PaymentHistory> paymentHistories = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupPaymentMethodCombo();
        setupReservationCombo();
        setupSummaryTable();
        setupHistoryTable();
        setupTableSelectionListener();
        loadUnpaidReservations();         // Combo box: unpaid only
        loadAllReservationsForTable();
        loadPaymentMethodData();

    }
    private void setupPaymentMethodCombo() {
        methodCombo.getItems().addAll("Cash", "GCash", "Bank Transfer", "Other");
        methodCombo.getSelectionModel().selectFirst();
    }

    private void setupReservationCombo() {
        rentalCombo.setCellFactory(param -> new ListCell<RentalWrapper>() {
            @Override
            protected void updateItem(RentalWrapper item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
        rentalCombo.setButtonCell(new ListCell<RentalWrapper>() {
            @Override
            protected void updateItem(RentalWrapper item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Rental");
                } else {
                    setText(item.toString());
                }
            }
        });

        rentalCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Rental rental = newVal.getRental();

                // Unbind before rebinding to avoid errors
                balanceLabel.textProperty().unbind();
                paymentProgress.progressProperty().unbind();

                // Rebind to selected reservation
                balanceLabel.textProperty().bind(Bindings.createStringBinding(
                        () -> String.format("Balance: ₱%.2f", rental.getTotalAmount() - rental.getPaidAmount()),
                        rental.paidAmountProperty()
                ));

                paymentProgress.progressProperty().bind(Bindings.when(
                        rental.totalAmountProperty().greaterThan(0)
                ).then(
                        Bindings.divide(rental.paidAmountProperty(), rental.totalAmountProperty())
                ).otherwise(0.0));

                updatePaymentInfo(rental);
            } else {
                balanceLabel.textProperty().set("Balance: ₱0.00");
                paymentProgress.setProgress(0);
            }
        });
    }

    private void setupSummaryTable() {
        reservationIdCol.setCellValueFactory(cellData -> cellData.getValue().rentalIdProperty().asObject());
        totalAmountCol.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());
        amountPaidCol.setCellValueFactory(cellData -> cellData.getValue().amountPaidProperty().asObject());

        // Use the bound balanceProperty
        // ✅ Correct binding
        balanceCol.setCellValueFactory(cellData ->
                cellData.getValue().balanceProperty().asObject()
        );
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        paymentSummaryTable.setItems(paymentSummaries);
    }
    // Add this to your initialize() method after setupHistoryTable()
    private void setupTableSelectionListener() {
        paymentSummaryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Get reservation ID from the selected PaymentSummary
                int selectedReservationId = newVal.getRentalId();

                // Try to find this reservation in the combo box items first
                boolean foundInCombo = false;
                for (RentalWrapper wrapper : unpaidRentals) {
                    if (wrapper.getRental().getRentalId()== selectedReservationId) {
                        // Select it in the combo box to reuse existing binding logic
                        rentalCombo.getSelectionModel().select(wrapper);
                        foundInCombo = true;
                        break;
                    }
                }

                // If not found in combo box (e.g., if it's a paid reservation)
                if (!foundInCombo) {
                    // Unbind before setting new values to avoid errors
                    balanceLabel.textProperty().unbind();
                    paymentProgress.progressProperty().unbind();

                    // Update UI directly with PaymentSummary data
                    balanceLabel.setText(String.format("Balance: ₱%.2f", newVal.getBalance()));

                    double progressValue = 0.0;
                    if (newVal.getTotalAmount() > 0) {
                        progressValue = newVal.getAmountPaid() / newVal.getTotalAmount();
                    }
                    paymentProgress.setProgress(progressValue);

                    // Load payment history for this reservation
                    loadPaymentHistoryForReservation(selectedReservationId);
                }
            }
        });
    }
    private void loadPaymentHistoryForReservation(int reservationId) {
        paymentHistories.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM payments WHERE rental_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, reservationId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                paymentHistories.add(new PaymentHistory(
                        rs.getDate("payment_date").toString(),
                        rs.getDouble("amount"),
                        rs.getString("payment_method"),
                        getUsernameById(rs.getInt("recorded_by"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupHistoryTable() {
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        amountCol.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        methodCol.setCellValueFactory(cellData -> cellData.getValue().methodProperty());
        recordedByCol.setCellValueFactory(cellData -> cellData.getValue().recordedByProperty());

        paymentHistoryTable.setItems(paymentHistories);
    }


    public void setReservation(Rental rental) {
        RentalWrapper rentalWrapper = new RentalWrapper(rental);
        unpaidRentals.add(rentalWrapper);
        rentalCombo.getSelectionModel().select(rentalWrapper);

        // Bind balance label
        balanceLabel.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("Balance: ₱%.2f",
                        rental.getTotalAmount() - rental.getPaidAmount()),
                rental.paidAmountProperty()
        ));

        // Bind progress bar
        paymentProgress.progressProperty().bind(Bindings.divide(
                rental.paidAmountProperty(),
                rental.totalAmountProperty()
        ));
    }
    private void loadUnpaidReservations() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM rental_summary WHERE payment_status != 'Paid'";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            unpaidRentals.clear();
            while (rs.next()) {
                Rental rental = new Rental();
                rental.rentalIdProperty().set(rs.getInt("rental_id"));
                rental.customerIdProperty().set(rs.getInt("customer_id"));
                rental.startDateProperty().set(rs.getDate("start_datetime").toLocalDate());
                rental.endDateProperty().set(rs.getDate("end_datetime").toLocalDate());
                rental.totalAmountProperty().set(rs.getDouble("total_amount"));
                rental.paidAmountProperty().set(rs.getDouble("paid_amount"));
                rental.paymentStatusProperty().set(rs.getString("payment_status"));
                Customer customer = new Customer();
                customer.setName(rs.getString("customer_name"));
                rental.customerProperty().set(customer);
                unpaidRentals.add(new RentalWrapper(rental));
            }
            rentalCombo.setItems(unpaidRentals);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAllReservationsForTable() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM rental_summary";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            paymentSummaries.clear();
            while (rs.next()) {
                Rental rental = new Rental();
                rental.rentalIdProperty().set(rs.getInt("rental_id"));
                rental.customerIdProperty().set(rs.getInt("customer_id"));
                rental.startDateProperty().set(rs.getDate("start_datetime").toLocalDate());
                rental.endDateProperty().set(rs.getDate("end_datetime").toLocalDate());
                rental.totalAmountProperty().set(rs.getDouble("total_amount"));
                rental.paidAmountProperty().set(rs.getDouble("paid_amount"));
                rental.paymentStatusProperty().set(rs.getString("payment_status"));
                Customer customer = new Customer();
                customer.setName(rs.getString("customer_name"));
                rental.customerProperty().set(customer);
                paymentSummaries.add(new PaymentSummary(rental));
            }
            paymentSummaryTable.setItems(paymentSummaries);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePaymentInfo(Rental rental) {
        // Update summary table
        for (PaymentSummary summary : paymentSummaries) {
            if (summary.getRentalId() == rental.getRentalId()) {
                summary.setAmountPaid(rental.getPaidAmount());
                summary.setStatus(rental.getPaymentStatus());
                break;
            }
        }

        // Update history table
        paymentHistories.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM payments WHERE rental_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, rental.getRentalId());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                paymentHistories.add(new PaymentHistory(
                        rs.getDate("payment_date").toString(),
                        rs.getDouble("amount"),
                        rs.getString("payment_method"),
                        getUsernameById(rs.getInt("recorded_by"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // In PaymentController.java
    private String getUsernameById(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT username FROM users WHERE user_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            return rs.next() ? rs.getString("username") : "Unknown";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error";
        }
    }

    private void loadPaymentMethodData(){
        try(Connection conn = DatabaseConnection.getConnection()){
            String query = "SELECT payment_method, SUM(amount) AS total_amount " +
                    "FROM payments GROUP BY payment_method";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            ObservableList<PieChart.Data> piechart = FXCollections.observableArrayList();
            while (rs.next()){
                String method = rs.getString("payment_method");
                Double totalAmount  = rs.getDouble("total_amount");
                piechart.add(new PieChart.Data(method + ": ₱" + String.format("%.2f",totalAmount),totalAmount));

            }
            methodPiechart.setData(piechart);
            methodPiechart.setTitle("Payment Method Distribution");
            methodPiechart.setLabelsVisible(true);
            methodPiechart.setLegendVisible(true);
        }catch (SQLException e){
            e.getMessage();
        }
    }
    @FXML
    private void submitPayment() {
        RentalWrapper selected = rentalCombo.getValue();
        if (selected == null) {
            showAlert("No Selection", "Please select a rentals first.");
            return;
        }

        String paymentMethod = methodCombo.getValue();
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            showAlert("Invalid Method", "Please select a payment method.");
            return;
        }

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            showAlert("Invalid Amount", "Please enter an amount.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert("Invalid Amount", "Amount must be greater than zero.");
                return;
            }

            // Get reservation and calculate balance
            Rental rental = selected.getRental();
            double totalDue = rental.getTotalAmount();
            double currentPaid = rental.getPaidAmount();
            double balance = totalDue - currentPaid;

            // Handle overpayment
            if (balance > 0 && amount > balance) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Overpayment Detected");
                alert.setHeaderText("Payment exceeds remaining balance");
                alert.setContentText("Proceed with payment of ₱" + String.format("%.2f", amount) +
                        " when only ₱" + String.format("%.2f", balance) + " is due?");

                ButtonType buttonTypeYes = new ButtonType("Proceed");
                ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeCancel);
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isEmpty() || result.get() != buttonTypeYes) {
                    return; // Cancel payment
                }
            }
            // Prevent zero division in progress bar
            else if (totalDue <= 0) {
                showAlert("Invalid Rental", "This rental has no valid amount to pay.");
                return;
            }

            // Save payment to database
            int reservationId = rental.getRentalId();
            savePaymentToDatabase(reservationId, amount, paymentMethod);

            // Update local reservation model
            rental.setPaidAmount(currentPaid + amount);

            // Update UI
            updatePaymentInfo(rental);
            amountField.clear();
            methodCombo.getSelectionModel().selectFirst();

            // Success message
            loadPaymentMethodData();
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Payment Successful");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Payment of ₱" + String.format("%.2f", amount) +
                    " recorded successfully.");
            successAlert.showAndWait();

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid numeric amount.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to record payment: " + e.getMessage());
        }
    }
    private void savePaymentToDatabase(int reservationId, double amount, String paymentMethod) throws SQLException {
        int currentUserId = Model.getInstance().getcurrentuserid();
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (CallableStatement cstmt = conn.prepareCall("{CALL sp_record_payment(?, ?, ?, ?, ?)}")) {
                cstmt.setInt(1, reservationId);
                cstmt.setDouble(2, amount);
                cstmt.setString(3, paymentMethod);
                cstmt.setInt(4, currentUserId);

                // Register the OUT parameter last
                cstmt.registerOutParameter(5, Types.DECIMAL);

                cstmt.execute();

                // Get overpaid amount if needed
                double overpaid = cstmt.getDouble(5);
                if (overpaid > 0) {
                    showAlert("Overpayment", "This payment exceeds the total due by ₱" + String.format("%.2f", overpaid));
                }
            }
        }
    }
    private double getCurrentTotalDue(int reservationId) throws SQLException {
        String query = "SELECT COALESCE(SUM(ri.price_per_unit), 0) + COALESCE(r.overdue_charges, 0) AS total_due " +
                "FROM rental_items ri " +
                "LEFT JOIN rentals r ON ri.rental_id = r.rental_id " +
                "WHERE ri.rental_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, reservationId);
            ResultSet rs = pst.executeQuery();
            return rs.next() ? rs.getDouble("total_due") : 0.0;
        }
    }

    private void showAlert(String invalidInput, String s) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(invalidInput);
        alert.setHeaderText(null);
        alert.setContentText(s);
        alert.showAndWait();

    }

}
