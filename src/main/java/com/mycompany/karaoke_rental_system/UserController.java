package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UserController implements Initializable {
    @FXML
    private TextField usernameTextField;
    @FXML private PasswordField passwordTextField;
    @FXML private PasswordField confirmPasswordTextField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button saveButton;
    @FXML private TableView<UserModel> usersTable;
    @FXML private TableColumn<UserModel, Integer> idCol;
    @FXML private TableColumn<UserModel, String> usernameCol;
    @FXML private TableColumn<UserModel, String> roleCol;
    @FXML private TableColumn<UserModel, Timestamp> lastLoginCol;
    @FXML private TableColumn<UserModel, String> createdByCol;
    @FXML private TableColumn<UserModel, Timestamp> createdAtCol;
    @FXML private TableView<ActivityLog> activityLogTable;
    @FXML private TableColumn<ActivityLog, Timestamp> timestampCol;
    @FXML private TableColumn<ActivityLog, String> usernameLogCol;
    @FXML private TableColumn<ActivityLog, String> actionCol;
    @FXML private TableColumn<ActivityLog, String> detailsCol;
    @FXML private ListView<String> rankingList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupUserTable();
        setupRoleComboBox();
        setupActivityLogTable();
        loadRankings();  // Add this line
        refreshTables();
    }
    private void setupUserTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        lastLoginCol.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));
        createdByCol.setCellValueFactory(new PropertyValueFactory<>("createdBy"));
        createdAtCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        usersTable.setItems(getUsers());
    }

    private void setupRoleComboBox() {
        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Staff"));
    }

    private void setupActivityLogTable() {
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        usernameLogCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));

        // Add this line to ensure the table refreshes
        activityLogTable.setItems(getActivityLogs());
    }

    @FXML
    private void saveUser(ActionEvent event) {
        if (!validateForm()) return;

        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        String role = roleComboBox.getValue();

        // Hash password using BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseConnection.getConnection();

            // Check if username exists
            if (usernameExists(username)) {
                showError("Username Taken", "Username already exists");
                return;
            }

            // Insert user with hashed password
            String sql = "INSERT INTO users (username, password, role, created_by) VALUES (?, ?, ?, ?)";
            pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);  // Store hashed password
            pstmt.setString(3, role);
            pstmt.setInt(4, Model.getInstance().getcurrentuserid());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                showError("Creation Failed", "Creating user failed, no rows affected.");
                return;
            }

            // Get generated user ID
            generatedKeys = pstmt.getGeneratedKeys();
            int userId = -1;
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            }

            // Log activity
            logActivity(userId, "CREATE", "users", userId, "User created via UI");

            // Refresh tables
            refreshTables();

            // Clear form
            clearForm();

        } catch (SQLException e) {
            showError("Database Error", "Failed to create user");
            e.printStackTrace();
        } finally {
            // Close resources in finally block
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (pstmt != null) pstmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean validateForm() {
        if (usernameTextField.getText().isEmpty() ||
                passwordTextField.getText().isEmpty() ||
                confirmPasswordTextField.getText().isEmpty() ||
                roleComboBox.getValue() == null) {
            showError("Validation Error", "All fields are required");
            return false;
        }

        if (!passwordTextField.getText().equals(confirmPasswordTextField.getText())) {
            showError("Validation Error", "Passwords do not match");
            return false;
        }

        if (passwordTextField.getText().length() < 8) {
            showError("Validation Error", "Password must be at least 8 characters");
            return false;
        }

        return true;
    }

    private boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    private void clearForm() {
        usernameTextField.clear();
        passwordTextField.clear();
        confirmPasswordTextField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }
    private ObservableList<UserModel> getUsers() {
        ObservableList<UserModel> users = FXCollections.observableArrayList();
        String sql = "SELECT * FROM users ORDER BY user_id DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new UserModel(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getTimestamp("last_login"),
                        getCreatedByUserName(rs.getObject("created_by", Integer.class)),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    private String getCreatedByUserName(Integer userId) {
        if (userId == null || userId == 0) return "System";
        String sql = "SELECT username FROM users WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
    private void logActivity(int userId, String actionType, String tableAffected, int recordId, String details) {
        String sql = "INSERT INTO activity_log (user_id, action_type, table_affected, record_id, action_details) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, tableAffected);
            pstmt.setInt(4, recordId);
            pstmt.setString(5, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private ObservableList<ActivityLog> getActivityLogs() {
        ObservableList<ActivityLog> logs = FXCollections.observableArrayList();
        String sql = "SELECT l.*, u.username FROM activity_log l JOIN users u ON l.user_id = u.user_id ORDER BY l.action_timestamp DESC LIMIT 50";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(new ActivityLog(
                        rs.getTimestamp("action_timestamp"),
                        rs.getString("username"),
                        rs.getString("action_type") + " " + rs.getString("table_affected"),
                        rs.getString("action_details")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message to user
            showError("Database Error", "Failed to load activity logs");
        }
        return logs;
    }
    private void refreshTables() {
        usersTable.setItems(getUsers());
        activityLogTable.setItems(getActivityLogs());
    }

        private void showError(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    // Ranking icons (Unicode emojis as fallback)
    private void loadRankings() {
        rankingList.getItems().clear();
        String sql = "SELECT u.user_id, u.username, COUNT(*) AS total_actions " +
                "FROM activity_log l " +
                "JOIN users u ON l.user_id = u.user_id " +
                "GROUP BY u.user_id, u.username " +
                "ORDER BY total_actions DESC " +
                "LIMIT 3";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int rank = 0;
            while (rs.next()) {
                String username = rs.getString("username");
                int count = rs.getInt("total_actions");

                // Use FontAwesome icons for ranking
                String icon = getRankIcon(rank);
                String rankingEntry = String.format("%s %s \u2022 %d actions", icon, username, count);
                rankingList.getItems().add(rankingEntry);
                rank++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            rankingList.getItems().addAll(
                    "\uf091 No data available",
                    "\uf091 Check database connection",
                    "\uf091 Try refreshing"
            );
        }
    }

    private String getRankIcon(int rank) {
        switch (rank) {
            case 0: return "🥇"; // 1st place
            case 1: return "🥈"; // 2nd place
            case 2: return "🥉"; // 3rd place
            default: return "⚪";
        }
    }
}
