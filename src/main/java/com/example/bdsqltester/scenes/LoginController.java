package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.DataSourceManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML
    private TextField passwordField;

    @FXML
    private ChoiceBox<String> selectRole;

    @FXML
    private TextField usernameField;

    private int userId;

    public int getUserId() {
        return userId;
    }

    boolean verifyCredentials(String username, String password, String role) throws SQLException {
        // Call the database to verify the credentials
        // This is insecure as this stores the password in plain text.
        // In a real application, you should hash the password and store it securely.

        // Get a connection to the database
        try (Connection c = DataSourceManager.getUserConnection()) {

            // Create a prepared statement to prevent SQL injection
            PreparedStatement stmt = c.prepareStatement("SELECT * FROM users WHERE username = ? AND role = ?");
            stmt.setString(1, username);
            stmt.setString(2, role.toLowerCase());

            System.out.println("in");

            // Execute the query
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // User found, check the password
                String dbPassword = rs.getString("password_hash");

                if (dbPassword.equals(password)) {
                    this.userId = rs.getInt("user_id");
                    return true; // Credentials are valid
                }
            }
        }

        // If we reach here, the credentials are invalid
        return false;
    }

    @FXML
    void initialize() {
        selectRole.getItems().addAll("User", "Admin Cabang", "Admin Pusat");
        if (selectRole.getItems().contains("User")) {
            selectRole.setValue("User");
        } else if (selectRole.getItems().contains("Admin Cabang")) {
            selectRole.setValue("Admin Cabang");
        } else if (selectRole.getItems().contains("Admin Pusat")) {
            selectRole.setValue("Admin Pusat");
        }
    }

    @FXML
    void onLoginClick(ActionEvent event) {
        // Get the username and password from the text fields
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = selectRole.getValue();

        // Verify the credentials
        try {
            if (verifyCredentials(username, password, role)) {
                HelloApplication app = HelloApplication.getApplicationInstance();
                app.setUserId(this.userId);// Store the user_id in HelloApplication
                app.getPrimaryStage().setTitle("User View");

                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("user-view.fxml"));
                Scene scene = new Scene(loader.load());
                app.getPrimaryStage().setScene(scene);
                app.getPrimaryStage().sizeToScene();
            } else {
                // Show an error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText("Invalid Credentials");
                alert.setContentText("Please check your username, password, and role.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Database Connection Failed");
            alert.setContentText("Could not connect to the database. Please try again later.");
            alert.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
