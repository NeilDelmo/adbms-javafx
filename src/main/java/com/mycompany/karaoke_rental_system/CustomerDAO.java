package com.mycompany.karaoke_rental_system;
import java.sql.*;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;

public class CustomerDAO {

    public static Customer getCustomerById(int customerId){
        Customer customer = null;
        String query = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getInt("created_by"),
                        rs.getInt("total_bookings"),
                        rs.getTimestamp("last_booking").toLocalDateTime(),
                        rs.getDouble("total_spent")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving customer: " + e.getMessage());
        }

        return customer;
    }

}
