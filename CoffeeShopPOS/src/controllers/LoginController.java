package controllers;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import models.Product;
import utils.DBConnection;
import utils.XMLLoader;
import utils.SessionManager;
import main.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class LoginController {
    private TextField usernameField;
    private PasswordField passwordField;
    private Label messageLabel;
    private Button loginButton;
    private Stage stage;

    @SuppressWarnings("exports")
	public LoginController(Stage stage, TextField usernameField, PasswordField passwordField,
                          Label messageLabel, Button loginButton) {
        this.stage = stage;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.messageLabel = messageLabel;
        this.loginButton = loginButton;
        
        System.out.println("LoginController initialized");
        
        if (isValid()) {
            initEvents();
        } else {
            System.err.println("LoginController initialization failed!");
        }
    }

    private boolean isValid() {
        return usernameField != null && passwordField != null && 
               messageLabel != null && loginButton != null;
    }

    private void initEvents() {
        loginButton.setOnAction(e -> login());
        passwordField.setOnAction(e -> login());
        usernameField.setOnAction(e -> passwordField.requestFocus());
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Try database login
        if (tryDatabaseLogin(username, password)) {
            return;
        }
        
        // Fallback to demo login
        handleDemoLogin(username, password);
    }
    
    private boolean tryDatabaseLogin(String username, String password) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT id, username, role FROM users WHERE username=? AND password_hash=? AND is_active=true";
            pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, User.hashPassword(password));

            rs = pst.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String role = rs.getString("role");
                System.out.println("Login successful! User: " + username + ", Role: " + role);
                
                // Set session
                SessionManager.setCurrentUser(userId, username, role);
                
                navigateToDashboard(role);
                return true;
            }
        } catch (Exception ex) {
            System.err.println("Database login failed: " + ex.getMessage());
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
        return false;
    }
    
    private void handleDemoLogin(String username, String password) {
        System.out.println("Using demo login for: " + username);
        
        if (username.equals("admin") && password.equals("admin123")) {
            SessionManager.setCurrentUser(1, username, "admin");
            navigateToDashboard("admin");
        } else if (username.equals("cashier") && password.equals("cashier123")) {
            SessionManager.setCurrentUser(2, username, "cashier");
            navigateToDashboard("cashier");
        } else {
            messageLabel.setText("Invalid credentials Try again");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void navigateToDashboard(String role) {
        try {
            // Clear login fields
            usernameField.clear();
            passwordField.clear();
            messageLabel.setText("");
            
            switch (role.toLowerCase()) {
                case "admin":
                    Scene adminScene = XMLLoader.loadScene("src/xml/admin_dashboard.xml", stage);
                    // Apply dark mode CSS
                    try {
                        adminScene.getStylesheets().add(LoginController.class.getResource("/application.css").toExternalForm());
                    } catch (Exception e) {
                        // CSS not found, continue without it
                    }
                    stage.setScene(adminScene);
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");
                    initializeAdminController(adminScene);
                    break;
                    
                case "cashier":
                    Scene cashierScene = XMLLoader.loadScene("src/xml/cashier_dashboard.xml", stage);
                    // Apply dark mode CSS
                    try {
                        cashierScene.getStylesheets().add(LoginController.class.getResource("/application.css").toExternalForm());
                    } catch (Exception e) {
                        // CSS not found, continue without it
                    }
                    stage.setScene(cashierScene);
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");
                    initializeCashierController(cashierScene);
                    break;
                    
                default:
                    messageLabel.setText("Unknown user role: " + role);
                    messageLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            messageLabel.setText("Error loading dashboard");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @SuppressWarnings("unchecked")
	private void initializeAdminController(Scene adminScene) {
        try {
            Map<String, javafx.scene.Node> components = XMLLoader.getComponents();
            
            System.out.println("Available components: " + components.keySet());
            
            // Get admin components
            TableView<User> userTable = (TableView<User>) components.get("userTable");
            TextField adminUsernameField = (TextField) components.get("usernameField");
            TextField adminPasswordField = (TextField) components.get("passwordField");
            ComboBox<String> roleComboBox = (ComboBox<String>) components.get("roleComboBox");
            Button addUserButton = (Button) components.get("addUserButton");
            Button deleteUserButton = (Button) components.get("deleteUserButton"); // NEW
            Button logoutButton = (Button) components.get("logoutButton");
            Button refreshUsersButton = (Button) components.get("refreshUsersButton");
            Label welcomeLabel = (Label) components.get("welcomeLabel");
            
            TableView<Product> productTable = (TableView<Product>) components.get("productTable");
            TextField productNameField = (TextField) components.get("productNameField");
            TextField productDescriptionField = (TextField) components.get("productDescriptionField");
            TextField productPriceField = (TextField) components.get("productPriceField");
            TextField productStockField = (TextField) components.get("productStockField");
            TextField productCategoryField = (TextField) components.get("productCategoryField");
            Button addProductButton = (Button) components.get("addProductButton");
            Button deleteProductButton = (Button) components.get("deleteProductButton");
            Button reduceStockButton = (Button) components.get("reduceStockButton");
            Button refreshProductsButton = (Button) components.get("refreshProductsButton");
            Button salesReportButton = (Button) components.get("salesReportButton");
            
            // Set welcome message
            if (welcomeLabel != null) {
                String username = SessionManager.getCurrentUsername();
                if (username != null) {
                    // Capitalize first letter of username
                    String capitalizedName = username.substring(0, 1).toUpperCase() + 
                                             (username.length() > 1 ? username.substring(1) : "");
                    welcomeLabel.setText("Welcome, " + capitalizedName + "!");
                }
            }
            
            // Set logout handler
            if (logoutButton != null) {
                logoutButton.setOnAction(e -> logout());
            }
            
            // Initialize AdminController with all required parameters (including deleteUserButton and salesReportButton)
            new AdminController(userTable, adminUsernameField, adminPasswordField, roleComboBox,
                              addUserButton, deleteUserButton, logoutButton, refreshUsersButton,
                              productTable, productNameField, productDescriptionField, productPriceField,
                              productStockField, productCategoryField, addProductButton, deleteProductButton,
                              reduceStockButton, refreshProductsButton, salesReportButton);
                              
            System.out.println("AdminController initialized successfully");
            
        } catch (Exception e) {
            System.err.println("Error initializing AdminController: " + e.getMessage());
            e.printStackTrace();
            logout();
        }
    }
    
    private void initializeCashierController(Scene cashierScene) {
        try {
            Map<String, javafx.scene.Node> components = XMLLoader.getComponents();
            
            // Get all the components your CashierController expects
            TableView<Product> productTable = (TableView<Product>) components.get("productTable");
            TextField searchField = (TextField) components.get("searchField");
            Button searchButton = (Button) components.get("searchButton");
            TableView<Product> cartTable = (TableView<Product>) components.get("cartTable");
            TextField productIdField = (TextField) components.get("productIdField");
            TextField quantityField = (TextField) components.get("quantityField");
            Button addToCartButton = (Button) components.get("addToCartButton");
            Label totalLabel = (Label) components.get("totalLabel");
            Label discountLabel = (Label) components.get("discountLabel");
            Label finalTotalLabel = (Label) components.get("finalTotalLabel");
            Label promoCodeLabel = (Label) components.get("promoCodeLabel");
            Button checkoutButton = (Button) components.get("checkoutButton");
            Button printReceiptButton = (Button) components.get("printReceiptButton");
            Button clearCartButton = (Button) components.get("clearCartButton");
            Button logoutButton = (Button) components.get("logoutButton");
            Label welcomeLabel = (Label) components.get("welcomeLabel");
            
            // Set welcome message
            if (welcomeLabel != null) {
                String username = SessionManager.getCurrentUsername();
                if (username != null) {
                    // Capitalize first letter of username
                    String capitalizedName = username.substring(0, 1).toUpperCase() + 
                                             (username.length() > 1 ? username.substring(1) : "");
                    welcomeLabel.setText("Welcome, " + capitalizedName + "!");
                }
            }
            
            // Set logout handler
            if (logoutButton != null) {
                logoutButton.setOnAction(e -> logout());
            }
            
            // Initialize CashierController with all required parameters
            new CashierController(productTable, searchField, searchButton, cartTable,
                                productIdField, quantityField, addToCartButton,
                                totalLabel, discountLabel, finalTotalLabel, promoCodeLabel,
                                checkoutButton, printReceiptButton, clearCartButton, logoutButton);
            
            System.out.println("CashierController initialized successfully");
            
        } catch (Exception e) {
            System.err.println("Error initializing CashierController: " + e.getMessage());
            e.printStackTrace();
            logout();
        }
    }
    private void logout() {
        try {
            // Clear session
            SessionManager.clearSession();
            
            // Reload login scene
            Scene loginScene = XMLLoader.loadScene("src/xml/login.xml", stage);
            stage.setScene(loginScene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            
            // Get login components and reinitialize LoginController
            Map<String, javafx.scene.Node> components = XMLLoader.getComponents();
            TextField usernameField = (TextField) components.get("usernameField");
            PasswordField passwordField = (PasswordField) components.get("passwordField");
            Label messageLabel = (Label) components.get("messageLabel");
            Button loginButton = (Button) components.get("loginButton");
            
            new LoginController(stage, usernameField, passwordField, messageLabel, loginButton);
            
            System.out.println("Logout successful - returned to login screen");
            
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
