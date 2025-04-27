package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.data.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PaymentController implements Initializable {
    public PieChart methodPiechart;
    // FXML Injections
    @FXML private ComboBox<ReservationWrapper> reservationCombo;
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

    private ObservableList<ReservationWrapper> unpaidReservations = FXCollections.observableArrayList();
    private ObservableList<PaymentSummary> paymentSummaries = FXCollections.observableArrayList();
    private ObservableList<PaymentHistory> paymentHistories = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupPaymentMethodCombo();
        setupReservationCombo();
        setupSummaryTable();
        setupHistoryTable();
        loadUnpaidReservations();
        loadPaymentMethodData();

        // Add listener to reservation combo
        reservationCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updatePaymentInfo(newVal.getReservation());
            }
        });
    }
    private void setupPaymentMethodCombo() {
        methodCombo.getItems().addAll("Cash", "GCash", "Bank Transfer", "Other");
        methodCombo.getSelectionModel().selectFirst();
    }

    private void setupReservationCombo() {
        reservationCombo.setCellFactory(param -> new ListCell<ReservationWrapper>() {
            @Override
            protected void updateItem(ReservationWrapper item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        reservationCombo.setButtonCell(new ListCell<ReservationWrapper>() {
            @Override
            protected void updateItem(ReservationWrapper item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Reservation");
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    private void setupSummaryTable() {
        reservationIdCol.setCellValueFactory(cellData -> cellData.getValue().reservationIdProperty().asObject());
        totalAmountCol.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());
        amountPaidCol.setCellValueFactory(cellData -> cellData.getValue().amountPaidProperty().asObject());
        balanceCol.setCellValueFactory(cellData -> cellData.getValue().balanceProperty().asObject());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        paymentSummaryTable.setItems(paymentSummaries);
    }

    private void setupHistoryTable() {
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        amountCol.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        methodCol.setCellValueFactory(cellData -> cellData.getValue().methodProperty());
        recordedByCol.setCellValueFactory(cellData -> cellData.getValue().recordedByProperty());

        paymentHistoryTable.setItems(paymentHistories);
    }
    public static class ReservationWrapper {
        private final Reservation reservation;

        public ReservationWrapper(Reservation reservation) {
            this.reservation = reservation;
        }

        public Reservation getReservation() {
            return reservation;
        }

        @Override
        public String toString() {
            return String.format("%s (%s - %s) - ₱%.2f",
                    reservation.getCustomer().getName(),
                    reservation.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd")),
                    reservation.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd")),
                    reservation.getTotalAmount() - reservation.getPaidAmount());
        }
    }
    private void loadUnpaidReservations() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT r.*, c.name AS customer_name " +
                    "FROM reservations r " +
                    "JOIN customers c ON r.customer_id = c.customer_id " +
                    "WHERE r.payment_status != 'Paid'";

            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            unpaidReservations.clear();
            while (rs.next()) {
                Reservation reservation = new Reservation();
                // Set reservation properties from result set
                unpaidReservations.add(new ReservationWrapper(reservation));
            }
            reservationCombo.setItems(unpaidReservations);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePaymentInfo(Reservation reservation) {
        // Update summary table
        paymentSummaries.clear();
        paymentSummaries.add(new PaymentSummary(
                reservation.getReservationId(),
                reservation.getTotalAmount(),
                reservation.getPaidAmount(),
                reservation.getPaymentStatus()
        ));

        // Update history table
        paymentHistories.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM payments WHERE reservation_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, reservation.getReservationId());
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

        // Update UI elements
        balanceLabel.setText(String.format("Balance: ₱%.2f",
                reservation.getTotalAmount() - reservation.getPaidAmount()));
        paymentProgress.setProgress(
                reservation.getPaidAmount() / reservation.getTotalAmount());
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

}
