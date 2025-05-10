/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Neil
 */
public class DashboardController implements Initializable {


    public AreaChart Trends;
    @FXML private Label totalRevenueLabel, totalRentsLabel, confirmedLabel, cancelledLabel, overDueLabel, topCustomerLabel;
    @FXML private LineChart<String, Number> lineChartIncome;
    @FXML private BarChart<String, Number> mostRentedEquipment_Package;
    @FXML private CategoryAxis xAxisLine, xAxisBar;
    @FXML private NumberAxis yAxisLine, yAxisBar;
    @FXML private Text username_lbl;
    @FXML private Label date_lbl;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            loadBasicStats(connection);
            loadIncomeChart(connection);
            loadPopularItems(connection);
            loadCurrentUser(connection);
            loadRentalDurationHeatMap(connection);

            // Set current user and date
            date_lbl.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void loadCurrentUser(Connection conn) throws SQLException {
        int userId = Model.getInstance().getcurrentuserid();

        String sql = "SELECT username FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    username_lbl.setText(rs.getString("username"));
                } else {
                    username_lbl.setText("Unknown User");
                }
            }
        }
    }
    private void loadBasicStats(Connection conn) throws SQLException {
        String reservationStatsSQL = """
            SELECT 
                COUNT(*) AS total_rentals,
                SUM(CASE WHEN status = 'Confirmed' THEN 1 ELSE 0 END) AS confirmed,
                SUM(CASE WHEN status = 'Cancelled' THEN 1 ELSE 0 END) AS cancelled,
                SUM(CASE WHEN status = 'Overdue' THEN 1 ELSE 0 END) AS overdue
            FROM rentals
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(reservationStatsSQL);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                totalRentsLabel.setText(String.valueOf(rs.getInt("total_rentals")));
                confirmedLabel.setText(String.valueOf(rs.getInt("confirmed")));
                cancelledLabel.setText(String.valueOf(rs.getInt("cancelled")));
                overDueLabel.setText(String.valueOf(rs.getInt("overdue")));
            }
        }

        // Top Customer
        // Top Customer - Only Confirmed or Completed Reservations
        String topCustomerSQL = """
    SELECT c.name
    FROM rentals r
    JOIN customers c ON r.customer_id = c.customer_id
    WHERE r.status IN ('Confirmed', 'Completed')
    GROUP BY c.customer_id
    ORDER BY COUNT(*) DESC
    LIMIT 1
""";
        try (PreparedStatement pstmt = conn.prepareStatement(topCustomerSQL);
             ResultSet rs = pstmt.executeQuery()) {
            topCustomerLabel.setText(rs.next() ? rs.getString("name") : "N/A");
        }

        // Total Revenue
        String revenueSQL = "SELECT SUM(amount) AS total FROM payments";
        try (PreparedStatement pstmt = conn.prepareStatement(revenueSQL);
             ResultSet rs = pstmt.executeQuery()) {

            totalRevenueLabel.setText("₱" + (rs.next() && rs.getObject("total", Double.class) != null ?
                    rs.getDouble("total") : "0.00"));
        }
    }

    private void loadIncomeChart(Connection conn) throws SQLException {
        String incomeSQL = """
            SELECT DATE_FORMAT(payment_date, '%Y-%m-%d') AS day, SUM(amount) AS total
            FROM payments
            GROUP BY day
            ORDER BY day ASC
            LIMIT 7
        """;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Income");

        try (PreparedStatement pstmt = conn.prepareStatement(incomeSQL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("day"), rs.getDouble("total")));
            }
        }

        lineChartIncome.getData().clear();
        lineChartIncome.getData().add(series);
    }

    private void loadPopularItems(Connection conn) throws SQLException {
        String equipmentSQL = """
        SELECT e.name, COUNT(ri.equipment_id) AS count
        FROM rental_items ri
        JOIN equipment e ON ri.equipment_id = e.equipment_id
        GROUP BY e.equipment_id
        ORDER BY count DESC
        LIMIT 5
    """;

        String packageSQL = """
        SELECT p.name, COUNT(ri.package_id) AS count
        FROM rental_items ri
        JOIN packages p ON ri.package_id = p.package_id
        GROUP BY p.package_id
        ORDER BY count DESC
        LIMIT 5
    """;

        XYChart.Series<String, Number> equipmentSeries = new XYChart.Series<>();
        equipmentSeries.setName("Most Rented Equipment");

        try (PreparedStatement pstmt = conn.prepareStatement(equipmentSQL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                equipmentSeries.getData().add(new XYChart.Data<>(rs.getString("name"), rs.getInt("count")));
            }
        }

        XYChart.Series<String, Number> packageSeries = new XYChart.Series<>();
        packageSeries.setName("Most Rented Packages");

        try (PreparedStatement pstmt = conn.prepareStatement(packageSQL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                packageSeries.getData().add(new XYChart.Data<>(rs.getString("name"), rs.getInt("count")));
            }
        }

        mostRentedEquipment_Package.getData().clear();
        if (!equipmentSeries.getData().isEmpty()) {
            mostRentedEquipment_Package.getData().add(equipmentSeries);
        }
        if (!packageSeries.getData().isEmpty()) {
            mostRentedEquipment_Package.getData().add(packageSeries);
        }
    }
    private void loadRentalDurationHeatMap(Connection conn) throws SQLException {
        String sql = """
        SELECT 
            DAYNAME(start_datetime) AS day_of_week,
            ROUND(AVG(TIMESTAMPDIFF(MINUTE, start_datetime, end_datetime) / 60), 2) AS avg_hours
        FROM rentals
        WHERE status IN ('Completed', 'Overdue')
        GROUP BY day_of_week
        ORDER BY FIELD(day_of_week,
            'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday')
    """;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Avg Rental Duration (hrs)");

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("day_of_week"), rs.getDouble("avg_hours")));
            }
        }

        Trends.getData().clear();
        Trends.getData().add(series);
    }

}
