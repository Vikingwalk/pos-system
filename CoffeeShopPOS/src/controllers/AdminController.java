package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.application.Platform;
import models.Product;
import models.User;
import models.SalesReport;
import models.PromoCode;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class AdminController {
    private TableView<User> userTable;
    private TextField usernameField, passwordField;
    private ComboBox<String> roleComboBox;
    private Button addUserButton, deleteUserButton, logoutButton, refreshUsersButton;
    
    private TableView<Product> productTable;
    private TextField productNameField, productDescriptionField, productPriceField, 
                     productStockField, productCategoryField;
    private Button addProductButton, deleteProductButton, reduceStockButton, refreshProductsButton;
    
    private TableView<PromoCode> promoCodeTable;
    private TextField promoCodeField, promoDiscountField;
    private Button addPromoCodeButton, deletePromoCodeButton, togglePromoCodeButton, refreshPromoCodesButton, clearAllPromoCodesButton;
    
    private Button salesReportButton;  // NEW: Sales report button
    
    private ObservableList<User> userList;
    private ObservableList<Product> productList;
    private ObservableList<PromoCode> promoCodeList;
    
    private boolean userTableInitialized = false;
    private boolean productTableInitialized = false;
    private boolean promoCodeTableInitialized = false;
    
    @SuppressWarnings("exports")
	public AdminController(TableView<User> userTable, TextField usernameField, 
                          TextField passwordField, ComboBox<String> roleComboBox,
                          Button addUserButton, Button deleteUserButton, Button logoutButton, Button refreshUsersButton,
                          TableView<Product> productTable, TextField productNameField,
                          TextField productDescriptionField, TextField productPriceField,
                          TextField productStockField, TextField productCategoryField,
                          Button addProductButton, Button deleteProductButton,
                          Button reduceStockButton, Button refreshProductsButton,
                          Button salesReportButton) {
        
        // Initialize fields
        this.userTable = userTable;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.roleComboBox = roleComboBox;
        this.addUserButton = addUserButton;
        this.deleteUserButton = deleteUserButton;
        this.logoutButton = logoutButton;
        this.refreshUsersButton = refreshUsersButton;
        this.productTable = productTable;
        this.productNameField = productNameField;
        this.productDescriptionField = productDescriptionField;
        this.productPriceField = productPriceField;
        this.productStockField = productStockField;
        this.productCategoryField = productCategoryField;
        this.addProductButton = addProductButton;
        this.deleteProductButton = deleteProductButton;
        this.reduceStockButton = reduceStockButton;
        this.refreshProductsButton = refreshProductsButton;
        this.salesReportButton = salesReportButton;
        
        // Initialize promo code components from XMLLoader
        try {
            Map<String, javafx.scene.Node> components = utils.XMLLoader.getComponents();
            this.promoCodeTable = (TableView<PromoCode>) components.get("promoCodeTable");
            this.promoCodeField = (TextField) components.get("promoCodeField");
            this.promoDiscountField = (TextField) components.get("promoDiscountField");
            this.addPromoCodeButton = (Button) components.get("addPromoCodeButton");
            this.deletePromoCodeButton = (Button) components.get("deletePromoCodeButton");
            this.togglePromoCodeButton = (Button) components.get("togglePromoCodeButton");
            this.refreshPromoCodesButton = (Button) components.get("refreshPromoCodesButton");
            this.clearAllPromoCodesButton = (Button) components.get("clearAllPromoCodesButton");
            
            if (promoCodeTable != null) {
                System.out.println("Promo code components loaded successfully");
            }
        } catch (Exception e) {
            System.out.println("Promo code components not found: " + e.getMessage());
        }
        
        this.userList = FXCollections.observableArrayList();
        this.productList = FXCollections.observableArrayList();
        this.promoCodeList = FXCollections.observableArrayList();
        
        // Setup role combo box
        if (roleComboBox != null) {
            roleComboBox.getItems().addAll("admin", "cashier");
        }
        
        initEvents();
        initializeTables();
        loadUsers();
        loadProducts();
        if (promoCodeTable != null) {
            loadPromoCodes();
        }
        setupTableSelection();
        
        System.out.println("AdminController initialized successfully");
    }
    
    private void initializeTables() {
        if (userTable != null && !userTableInitialized) {
            setupUserTableStructure();
            userTableInitialized = true;
        }
        
        if (productTable != null && !productTableInitialized) {
            setupProductTableStructure();
            productTableInitialized = true;
        }
        
        if (promoCodeTable != null && !promoCodeTableInitialized) {
            setupPromoCodeTableStructure();
            promoCodeTableInitialized = true;
        }
    }
    
    private void initEvents() {
        // User management events
        if (addUserButton != null) {
            addUserButton.setOnAction(e -> addUser());
        }
        
        if (deleteUserButton != null) {
            deleteUserButton.setOnAction(e -> deleteUser());
        }
        
        if (refreshUsersButton != null) {
            refreshUsersButton.setOnAction(e -> refreshUsers());
        }
        
        // Product management events
        if (addProductButton != null) {
            addProductButton.setOnAction(e -> addProduct());
        }
        
        if (deleteProductButton != null) {
            deleteProductButton.setOnAction(e -> deleteProduct());
        }
        
        if (reduceStockButton != null) {
            reduceStockButton.setOnAction(e -> reduceStock());
        }
        
        if (refreshProductsButton != null) {
            refreshProductsButton.setOnAction(e -> refreshProducts());
        }
        
        // Promo code management events
        if (addPromoCodeButton != null) {
            addPromoCodeButton.setOnAction(e -> addPromoCode());
        }
        
        if (deletePromoCodeButton != null) {
            deletePromoCodeButton.setOnAction(e -> deletePromoCode());
        }
        
        if (togglePromoCodeButton != null) {
            togglePromoCodeButton.setOnAction(e -> togglePromoCode());
        }
        
        if (refreshPromoCodesButton != null) {
            refreshPromoCodesButton.setOnAction(e -> refreshPromoCodes());
        }
        
        if (clearAllPromoCodesButton != null) {
            clearAllPromoCodesButton.setOnAction(e -> clearAllPromoCodes());
        }
        
        if (logoutButton != null) {
            logoutButton.setOnAction(e -> logout());
        }
        
        if (salesReportButton != null) {
            salesReportButton.setOnAction(e -> showSalesReportDialog());
        }
        
        System.out.println("AdminController events initialized");
    }
    
    private void logout() {
        try {
            // Get the current stage and close it
            javafx.stage.Stage stage = (javafx.stage.Stage) logoutButton.getScene().getWindow();
            stage.close();
            
            // Show login window again
            main.Main.showLoginWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupTableSelection() {
        if (userTable != null) {
            userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && usernameField != null && roleComboBox != null) {
                    usernameField.setText(newVal.getUsername());
                    roleComboBox.setValue(newVal.getRole());
                    passwordField.clear();
                }
            });
        }
        
        if (productTable != null) {
            productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && productNameField != null && productDescriptionField != null && 
                    productPriceField != null && productStockField != null && productCategoryField != null) {
                    productNameField.setText(newVal.getName());
                    productDescriptionField.setText(newVal.getDescription());
                    productPriceField.setText(String.valueOf(newVal.getPrice()));
                    productStockField.setText(String.valueOf(newVal.getStock()));
                    productCategoryField.setText(newVal.getCategory());
                }
            });
        }
        
        if (promoCodeTable != null) {
            promoCodeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && promoCodeField != null && promoDiscountField != null) {
                    promoCodeField.setText(newVal.getCode());
                    promoDiscountField.setText(String.valueOf(newVal.getDiscountPercent()));
                }
            });
        }
    }
    
    private void setupUserTableStructure() {
        userTable.getColumns().clear();
        
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);
        
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);
        
        TableColumn<User, Boolean> statusCol = new TableColumn<>("Active");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusCol.setPrefWidth(80);
        
        userTable.getColumns().addAll(idCol, usernameCol, roleCol, statusCol);
    }
    
    @SuppressWarnings("unchecked")
	private void setupProductTableStructure() {
        productTable.getColumns().clear();
        
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<Product, String> barcodeCol = new TableColumn<>("Barcode");
        barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        barcodeCol.setPrefWidth(120);
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(130);
        
        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(150);
        
        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(70);
        
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setPrefWidth(60);
        
        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(100);
        
        productTable.getColumns().addAll(idCol, barcodeCol, nameCol, descCol, priceCol, stockCol, categoryCol);
    }
    
    private void loadUsers() {
        userList.clear();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            pst = conn.prepareStatement("SELECT id, username, role, is_active FROM users");
            rs = pst.executeQuery();
            
            while (rs.next()) {
                userList.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getBoolean("is_active")
                ));
            }
            
            if (userTable != null) {
                userTable.setItems(userList);
            }
            
            System.out.println("Users loaded successfully: " + userList.size() + " users");
            
        } catch (Exception e) {
            showAlert("Error", "Failed to load users: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    private void deleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Selection Error", "Please select a user to delete");
            return;
        }
        
        // Prevent deleting the current admin user
        if ("admin".equals(selectedUser.getUsername())) {
            showAlert("Cannot Delete", "The main admin user cannot be deleted for system security.");
            return;
        }
        
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Check if user has any sales history
            String checkSalesSql = "SELECT COUNT(*) as sale_count FROM sales WHERE id = ?";
            pst = conn.prepareStatement(checkSalesSql);
            pst.setInt(1, selectedUser.getId());
            rs = pst.executeQuery();
            
            boolean hasSalesHistory = false;
            if (rs.next()) {
                hasSalesHistory = rs.getInt("sale_count") > 0;
            }
            
            // Create confirmation dialog
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete User");
            confirmation.setHeaderText("Delete User: " + selectedUser.getUsername());
            // Apply dark mode styling
            DialogPane dialogPane = confirmation.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #000000;");
            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #e0e0e0;");
            
            String contentText = "This will PERMANENTLY DELETE the user from the system.\n\n" +
                               "User: " + selectedUser.getUsername() + "\n" +
                               "Role: " + selectedUser.getRole() + "\n" +
                               "ID: " + selectedUser.getId() + "\n\n";
            
            if (hasSalesHistory) {
                contentText += "⚠  WARNING: This user has sales history!\n" +
                             "The user account will be deactivated instead of deleted\n" +
                             "to preserve sales records.\n\n";
            } else {
                contentText += "This user has no sales history and will be completely removed.\n\n";
            }
            
            contentText += "This action cannot be undone!\n\nAre you sure?";
            
            confirmation.setContentText(contentText);
            
            java.util.Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                
                if (hasSalesHistory) {
                    // User has sales history - deactivate instead of delete
                    String deactivateSql = "UPDATE users SET is_active = false WHERE id = ?";
                    pst = conn.prepareStatement(deactivateSql);
                    pst.setInt(1, selectedUser.getId());
                    
                    int rowsAffected = pst.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert("User Deactivated", 
                            "User '" + selectedUser.getUsername() + "' has been deactivated!\n\n" +
                            "✓ Account disabled for login\n" +
                            "✓ Sales history preserved\n" +
                            "✓ User remains in database for audit purposes");
                        System.out.println("Deactivated user: " + selectedUser.getUsername() + " (ID: " + selectedUser.getId() + ")");
                    }
                } else {
                    // No sales history - safe to delete completely
                    String deleteSql = "DELETE FROM users WHERE id = ?";
                    pst = conn.prepareStatement(deleteSql);
                    pst.setInt(1, selectedUser.getId());
                    
                    int rowsAffected = pst.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert("User Deleted", 
                            "User '" + selectedUser.getUsername() + "' has been permanently deleted from the system!");
                        System.out.println("Deleted user: " + selectedUser.getUsername() + " (ID: " + selectedUser.getId() + ")");
                    }
                }
                
                // Refresh data immediately after deletion/deactivation
                loadUsers();
                clearUserFields();
            }
            
        } catch (Exception e) {
            showAlert("Error", "Failed to delete user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    private void loadProducts() {
        productList.clear();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // First, ensure barcode column exists
            ensureBarcodeColumnExists(conn);
            
            pst = conn.prepareStatement("SELECT * FROM products");
            rs = pst.executeQuery();
            
            while (rs.next()) {
                String barcode = null;
                try {
                    barcode = rs.getString("barcode");
                } catch (Exception e) {
                    // Column might not exist yet
                    barcode = null;
                }
                
                productList.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("category"),
                    rs.getBoolean("is_available"),
                    barcode
                ));
            }
            
            if (productTable != null) {
                productTable.setItems(productList);
            }
            
            System.out.println("Products loaded successfully: " + productList.size() + " products");
            
        } catch (Exception e) {
            showAlert("Error", "Failed to load products: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    private void ensureBarcodeColumnExists(Connection conn) {
        PreparedStatement pst = null;
        try {
            // Try to add barcode column if it doesn't exist
            String alterSql = "ALTER TABLE products ADD COLUMN barcode VARCHAR(50)";
            pst = conn.prepareStatement(alterSql);
            pst.execute();
            System.out.println("Barcode column added to products table");
        } catch (Exception e) {
            // Column likely already exists, which is fine
            System.out.println("Barcode column check: " + e.getMessage());
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void refreshUsers() {
        System.out.println("Refreshing users...");
        loadUsers();
        if (userTable != null) {
            userTable.refresh();
        }
        showAlert("Refresh", "User list updated successfully!");
    }
    
    private void refreshProducts() {
        System.out.println("Refreshing products...");
        loadProducts();
        if (productTable != null) {
            productTable.refresh();
        }
        showAlert("Refresh", "Product list updated successfully!");
    }
    
    private void addUser() {
        if (usernameField == null || passwordField == null || roleComboBox == null) {
            showAlert("Error", "User form components not available");
            return;
        }
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();
        
        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Validation Error", "Please fill all fields");
            return;
        }
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Check if username already exists
            String checkSql = "SELECT id FROM users WHERE username = ?";
            pst = conn.prepareStatement(checkSql);
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                showAlert("Username Exists", "Username '" + username + "' already exists. Please choose a different username.");
                return;
            }
            
            // Insert new user
            String insertSql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
            pst = conn.prepareStatement(insertSql);
            pst.setString(1, username);
            pst.setString(2, User.hashPassword(password));
            pst.setString(3, role);
            
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                loadUsers();
                clearUserFields();
                showAlert("Success", "User added successfully!\n\nUsername: " + username + "\nRole: " + role);
            }
            
        } catch (Exception e) {
            showAlert("Error", "Failed to add user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(conn, pst, null);
        }
    }
    
    private void addProduct() {
        if (productNameField == null || productPriceField == null || productStockField == null) {
            showAlert("Error", "Product form components not available");
            return;
        }

        try {
            String name = productNameField.getText().trim();
            String description = productDescriptionField != null ? productDescriptionField.getText().trim() : "";
            double price = Double.parseDouble(productPriceField.getText());
            int stock = Integer.parseInt(productStockField.getText());
            String category = productCategoryField != null ? productCategoryField.getText().trim() : "";

            if (name.isEmpty() || price < 0 || stock < 0) {
                showAlert("Validation Error", "Please check product details");
                return;
            }

            Connection conn = null;
            PreparedStatement pst = null;
            ResultSet rs = null;

            try {
                conn = DBConnection.getConnection();
                
                // Check if product with same name already exists
                String checkSql = "SELECT id, name, stock, price FROM products WHERE LOWER(name) = LOWER(?)";
                pst = conn.prepareStatement(checkSql);
                pst.setString(1, name);
                rs = pst.executeQuery();

                if (rs.next()) {
                    // Product exists - UPDATE STOCK and other details
                    int existingId = rs.getInt("id");
                    String existingName = rs.getString("name");
                    int existingStock = rs.getInt("stock");
                    double existingPrice = rs.getDouble("price");
                    
                    // Update the existing product
                    String updateSql = "UPDATE products SET stock = stock + ?, price = ?, description = ?, category = ? WHERE id = ?";
                    pst = conn.prepareStatement(updateSql);
                    pst.setInt(1, stock);
                    pst.setDouble(2, price);
                    pst.setString(3, description);
                    pst.setString(4, category);
                    pst.setInt(5, existingId);
                    
                    int rowsUpdated = pst.executeUpdate();
                    if (rowsUpdated > 0) {
                        int newTotalStock = existingStock + stock;
                        showAlert("Product Updated", 
                                 "Product '" + existingName + "' already exists!\n\n" +
                                 "✓ Added " + stock + " to existing stock\n" +
                                 "✓ Updated price from $" + existingPrice + " to $" + price + "\n" +
                                 "✓ Total stock now: " + newTotalStock + "\n" +
                                 "✓ Product ID: " + existingId);
                    }
                } else {
                    // Product doesn't exist - INSERT new product with auto-generated barcode
                    String insertSql = "INSERT INTO products (name, description, price, stock, category) VALUES (?, ?, ?, ?, ?)";
                    pst = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS);
                    pst.setString(1, name);
                    pst.setString(2, description);
                    pst.setDouble(3, price);
                    pst.setInt(4, stock);
                    pst.setString(5, category);
                    
                    int rowsInserted = pst.executeUpdate();
                    if (rowsInserted > 0) {
                        // Get the auto-generated ID
                        ResultSet generatedKeys = pst.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int newProductId = generatedKeys.getInt(1);
                            
                            // Generate barcode using the new product ID
                            String barcode = utils.BarcodeGenerator.generateEAN13(newProductId);
                            
                            // Update the product with the generated barcode
                            String updateBarcodeSql = "UPDATE products SET barcode = ? WHERE id = ?";
                            PreparedStatement updatePst = conn.prepareStatement(updateBarcodeSql);
                            updatePst.setString(1, barcode);
                            updatePst.setInt(2, newProductId);
                            updatePst.executeUpdate();
                            updatePst.close();
                            
                            showAlert("New Product Added", "New product '" + name + "' added successfully!\n\nProduct ID: " + newProductId + "\nBarcode: " + barcode + "\nStock: " + stock + "\nPrice: $" + price);
                        }
                        generatedKeys.close();
                    }
                }
                
                // Refresh data immediately
                loadProducts();
                clearProductFields();

            } finally {
                DBConnection.closeResources(conn, pst, rs);
            }

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for price and stock");
        } catch (Exception e) {
            showAlert("Error", "Failed to add/update product: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deleteProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Selection Error", "Please select a product to delete");
            return;
        }
        
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Check if product has any sales history
            String checkSalesSql = "SELECT COUNT(*) as sale_count FROM sale_items WHERE product_id = ?";
            pst = conn.prepareStatement(checkSalesSql);
            pst.setInt(1, selectedProduct.getId());
            rs = pst.executeQuery();
            
            if (rs.next() && rs.getInt("sale_count") > 0) {
                showAlert("Cannot Delete - Has Sales History", 
                    "Product '" + selectedProduct.getName() + "' has sales history and cannot be deleted.\n\n" +
                    "✓ Product remains in database for sales records\n" +
                    "✓ You can reduce stock to zero instead\n" +
                    "✓ Use 'Reduce Stock' button to manage inventory");
                return;
            }
            
            // No sales history - proceed with DELETE
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Product");
            confirmation.setHeaderText("Delete Product: " + selectedProduct.getName());
            confirmation.setContentText(
                "This will PERMANENTLY DELETE the product from the database.\n\n" +
                "Product: " + selectedProduct.getName() + "\n" +
                "ID: " + selectedProduct.getId() + "\n" +
                "Stock: " + selectedProduct.getStock() + "\n\n" +
                "This action cannot be undone!\n\n" +
                "Are you sure?"
            );
            // Apply dark mode styling
            DialogPane dialogPane = confirmation.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #000000;");
            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #e0e0e0;");
            
            java.util.Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String deleteSql = "DELETE FROM products WHERE id = ?";
                pst = conn.prepareStatement(deleteSql);
                pst.setInt(1, selectedProduct.getId());
                
                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert("Success", "Product '" + selectedProduct.getName() + "' has been deleted from the database!");
                }
                
                // Refresh data immediately
                loadProducts();
                clearProductFields();
            }
            
        } catch (Exception e) {
            showAlert("Error", "Failed to delete product: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    private void reduceStock() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Selection Error", "Please select a product to reduce stock");
            return;
        }
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Reduce Portion of Stock", 
            "Reduce Portion of Stock", 
            "Remove All Stock (Set to Zero)");
        dialog.setTitle("Stock Reduction Options");
        dialog.setHeaderText("Reduce Stock for: " + selectedProduct.getName());
        dialog.setContentText("Current stock: " + selectedProduct.getStock() + "\nChoose reduction method:");
        // Apply dark mode styling
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a1a;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #e0e0e0;");
        
        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(choice -> {
            if ("Reduce Portion of Stock".equals(choice)) {
                reduceStockPortion(selectedProduct);
            } else if ("Remove All Stock (Set to Zero)".equals(choice)) {
                removeAllStock(selectedProduct);
            }
        });
    }
    
    private void reduceStockPortion(Product product) {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Reduce Stock Portion");
        dialog.setHeaderText("Reduce Stock for: " + product.getName());
        dialog.setContentText("Enter quantity to remove from current stock (" + product.getStock() + "):");
        
        dialog.showAndWait().ifPresent(quantityText -> {
            try {
                int reduceAmount = Integer.parseInt(quantityText);
                
                if (reduceAmount <= 0) {
                    showAlert("Input Error", "Reduction amount must be greater than 0");
                    return;
                }
                
                if (reduceAmount > product.getStock()) {
                    showAlert("Input Error", "Reduction amount cannot exceed current stock: " + product.getStock());
                    return;
                }
                
                Connection conn = null;
                PreparedStatement pst = null;
                
                try {
                    conn = DBConnection.getConnection();
                    
                    String reduceSql = "UPDATE products SET stock = stock - ? WHERE id = ?";
                    pst = conn.prepareStatement(reduceSql);
                    pst.setInt(1, reduceAmount);
                    pst.setInt(2, product.getId());
                    
                    int rowsAffected = pst.executeUpdate();
                    if (rowsAffected > 0) {
                        int newStock = product.getStock() - reduceAmount;
                        showAlert("Stock Reduced", 
                            "Stock removed successfully!\n\n" +
                            "Product: " + product.getName() + "\n" +
                            "Removed: " + reduceAmount + " units\n" +
                            "Previous stock: " + product.getStock() + "\n" +
                            "New stock: " + newStock);
                        
                        logStockReduction(product.getId(), reduceAmount, product.getStock(), newStock, "partial_removal");
                    }
                    
                    // Refresh data immediately
                    loadProducts();
                    
                } catch (Exception e) {
                    showAlert("Error", "Failed to reduce stock: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    DBConnection.closeResources(conn, pst, null);
                }
                
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Please enter a valid number");
            }
        });
    }
    
    private void removeAllStock(Product product) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Remove All Stock");
        confirmation.setHeaderText("Remove All Stock for: " + product.getName());
        confirmation.setContentText(
            "This will remove ALL stock and set quantity to ZERO.\n\n" +
            "Product: " + product.getName() + "\n" +
            "Current Stock: " + product.getStock() + "\n" +
            "New Stock: 0\n\n" +
            "Are you sure?"
        );
        // Apply dark mode styling
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a1a;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #e0e0e0;");
        
        java.util.Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Connection conn = null;
            PreparedStatement pst = null;
            
            try {
                conn = DBConnection.getConnection();
                
                String zeroStockSql = "UPDATE products SET stock = 0 WHERE id = ?";
                pst = conn.prepareStatement(zeroStockSql);
                pst.setInt(1, product.getId());
                
                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert("All Stock Removed", 
                        "All stock removed successfully!\n\n" +
                        "Product: " + product.getName() + "\n" +
                        "Previous stock: " + product.getStock() + "\n" +
                        "New stock: 0");
                    
                    logStockReduction(product.getId(), product.getStock(), product.getStock(), 0, "complete_removal");
                }
                
                // Refresh data immediately
                loadProducts();
                
            } catch (Exception e) {
                showAlert("Error", "Failed to remove stock: " + e.getMessage());
                e.printStackTrace();
            } finally {
                DBConnection.closeResources(conn, pst, null);
            }
        }
    }
    
    private void logStockReduction(int productId, int reductionAmount, int previousStock, int newStock, String adjustmentType) {
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String createTableSql = "CREATE TABLE IF NOT EXISTS stock_adjustments (" +
                                   "id INT PRIMARY KEY AUTO_INCREMENT, " +
                                   "product_id INT NOT NULL, " +
                                   "adjustment_type VARCHAR(50) NOT NULL, " +
                                   "quantity_change INT NOT NULL, " +
                                   "previous_stock INT NOT NULL, " +
                                   "new_stock INT NOT NULL, " +
                                   "reason TEXT, " +
                                   "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                   "FOREIGN KEY (product_id) REFERENCES products(id))";
            pst = conn.prepareStatement(createTableSql);
            pst.execute();
            pst.close();
            
            String logSql = "INSERT INTO stock_adjustments (product_id, adjustment_type, quantity_change, previous_stock, new_stock, reason) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";
            pst = conn.prepareStatement(logSql);
            pst.setInt(1, productId);
            pst.setString(2, adjustmentType);
            pst.setInt(3, -reductionAmount);
            pst.setInt(4, previousStock);
            pst.setInt(5, newStock);
            
            String reason = "";
            if ("partial_removal".equals(adjustmentType)) {
                reason = "Manual partial stock removal by admin";
            } else if ("complete_removal".equals(adjustmentType)) {
                reason = "Manual complete stock removal by admin";
            }
            
            pst.setString(6, reason);
            pst.executeUpdate();
            
        } catch (Exception e) {
            System.err.println("Error logging stock reduction: " + e.getMessage());
        } finally {
            DBConnection.closeResources(conn, pst, null);
        }
    }
    
    // Promo Code Management Methods
    private void setupPromoCodeTableStructure() {
        if (promoCodeTable == null) return;
        
        promoCodeTable.getColumns().clear();
        
        // Enable multi-select for the table
        promoCodeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        TableColumn<PromoCode, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<PromoCode, String> codeCol = new TableColumn<>("Promo Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(150);
        
        TableColumn<PromoCode, Double> discountCol = new TableColumn<>("Discount %");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discountPercent"));
        discountCol.setPrefWidth(100);
        discountCol.setCellFactory(col -> new TableCell<PromoCode, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f%%", item));
                }
                setStyle("-fx-text-fill: #cccccc;");
            }
        });
        
        TableColumn<PromoCode, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(80);
        activeCol.setCellFactory(col -> new TableCell<PromoCode, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                }
                setStyle("-fx-text-fill: " + (item != null && item ? "#4ade80" : "#e74c3c") + ";");
            }
        });
        
        promoCodeTable.getColumns().addAll(idCol, codeCol, discountCol, activeCol);
    }
    
    private void loadPromoCodes() {
        if (promoCodeTable == null) return;
        
        promoCodeList.clear();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Create table if it doesn't exist
            try {
                String createTableSql = "CREATE TABLE IF NOT EXISTS promo_codes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "code VARCHAR(50) UNIQUE NOT NULL, " +
                    "discount_percent DECIMAL(5,2) NOT NULL CHECK (discount_percent >= 0 AND discount_percent <= 100), " +
                    "is_active BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                pst = conn.prepareStatement(createTableSql);
                pst.execute();
                pst.close();
            } catch (Exception e) {
                // Table might already exist
            }
            
            pst = conn.prepareStatement("SELECT id, code, discount_percent, is_active FROM promo_codes ORDER BY code");
            rs = pst.executeQuery();
            
            while (rs.next()) {
                promoCodeList.add(new PromoCode(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getDouble("discount_percent"),
                    rs.getBoolean("is_active")
                ));
            }
            
            promoCodeTable.setItems(promoCodeList);
            System.out.println("Promo codes loaded: " + promoCodeList.size());
            
        } catch (Exception e) {
            showAlert("Error", "Failed to load promo codes: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    private void addPromoCode() {
        if (promoCodeField == null || promoDiscountField == null) return;
        
        String code = promoCodeField.getText().trim().toUpperCase();
        String discountText = promoDiscountField.getText().trim();
        
        if (code.isEmpty()) {
            showAlert("Input Error", "Please enter a promo code");
            return;
        }
        
        if (discountText.isEmpty()) {
            showAlert("Input Error", "Please enter a discount percentage");
            return;
        }
        
        try {
            double discountPercent = Double.parseDouble(discountText);
            
            if (discountPercent < 0 || discountPercent > 100) {
                showAlert("Input Error", "Discount must be between 0 and 100");
                return;
            }
            
            Connection conn = null;
            PreparedStatement pst = null;
            
            try {
                conn = DBConnection.getConnection();
                
                // Create table if it doesn't exist
                try {
                    String createTableSql = "CREATE TABLE IF NOT EXISTS promo_codes (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "code VARCHAR(50) UNIQUE NOT NULL, " +
                        "discount_percent DECIMAL(5,2) NOT NULL CHECK (discount_percent >= 0 AND discount_percent <= 100), " +
                        "is_active BOOLEAN DEFAULT TRUE, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                    pst = conn.prepareStatement(createTableSql);
                    pst.execute();
                    pst.close();
                } catch (Exception e) {
                    // Table might already exist
                }
                
                String insertSql = "INSERT INTO promo_codes (code, discount_percent, is_active) VALUES (?, ?, ?)";
                pst = conn.prepareStatement(insertSql);
                pst.setString(1, code);
                pst.setDouble(2, discountPercent);
                pst.setBoolean(3, true);
                
                int rowsInserted = pst.executeUpdate();
                if (rowsInserted > 0) {
                    showAlert("Success", "Promo code '" + code + "' added successfully!\nDiscount: " + discountPercent + "%");
                    loadPromoCodes();
                    clearPromoCodeFields();
                }
                
            } catch (SQLException e) {
                if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("UNIQUE")) {
                    showAlert("Error", "Promo code '" + code + "' already exists!");
                } else {
                    showAlert("Error", "Failed to add promo code: " + e.getMessage());
                }
            } finally {
                DBConnection.closeResources(conn, pst, null);
            }
            
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid discount percentage");
        }
    }
    
    private void deletePromoCode() {
        if (promoCodeTable == null) return;
        
        // Get all selected promo codes (supports both single and multiple selection)
        ObservableList<PromoCode> selectedItems = promoCodeTable.getSelectionModel().getSelectedItems();
        
        if (selectedItems == null || selectedItems.isEmpty()) {
            showAlert("Selection Error", "Please select one or more promo codes to delete");
            return;
        }
        
        int count = selectedItems.size();
        String confirmationMessage;
        if (count == 1) {
            confirmationMessage = "Delete Promo Code: " + selectedItems.get(0).getCode() + "\n\n" +
                                 "This will permanently delete the promo code.\nAre you sure?";
        } else {
            confirmationMessage = "Delete " + count + " Promo Codes\n\n" +
                                 "This will permanently delete the selected promo codes:\n";
            for (int i = 0; i < Math.min(count, 5); i++) {
                confirmationMessage += "• " + selectedItems.get(i).getCode() + "\n";
            }
            if (count > 5) {
                confirmationMessage += "... and " + (count - 5) + " more\n";
            }
            confirmationMessage += "\nAre you sure?";
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Promo Code" + (count > 1 ? "s" : ""));
        confirmation.setHeaderText(count == 1 ? "Delete Promo Code" : "Delete Multiple Promo Codes");
        confirmation.setContentText(confirmationMessage);
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #000000;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #e0e0e0;");
        
        java.util.Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Connection conn = null;
            PreparedStatement pst = null;
            
            try {
                conn = DBConnection.getConnection();
                int totalDeleted = 0;
                
                // Delete each selected promo code
                for (PromoCode promoCode : selectedItems) {
                    String deleteSql = "DELETE FROM promo_codes WHERE id = ?";
                    pst = conn.prepareStatement(deleteSql);
                    pst.setInt(1, promoCode.getId());
                    
                    int rowsAffected = pst.executeUpdate();
                    if (rowsAffected > 0) {
                        totalDeleted++;
                    }
                }
                
                if (totalDeleted > 0) {
                    showAlert("Success", "Successfully deleted " + totalDeleted + " promo code(s)!");
                    loadPromoCodes();
                    clearPromoCodeFields();
                } else {
                    showAlert("Error", "Failed to delete promo codes");
                }
                
            } catch (Exception e) {
                showAlert("Error", "Failed to delete promo code(s): " + e.getMessage());
                e.printStackTrace();
            } finally {
                DBConnection.closeResources(conn, pst, null);
            }
        }
    }
    
    private void togglePromoCode() {
        if (promoCodeTable == null) return;
        
        PromoCode selected = promoCodeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a promo code to toggle");
            return;
        }
        
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DBConnection.getConnection();
            String updateSql = "UPDATE promo_codes SET is_active = ? WHERE id = ?";
            pst = conn.prepareStatement(updateSql);
            pst.setBoolean(1, !selected.isActive());
            pst.setInt(2, selected.getId());
            
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Promo code '" + selected.getCode() + "' is now " + 
                         (!selected.isActive() ? "active" : "inactive"));
                loadPromoCodes();
            }
            
        } catch (Exception e) {
            showAlert("Error", "Failed to toggle promo code: " + e.getMessage());
        } finally {
            DBConnection.closeResources(conn, pst, null);
        }
    }
    
    private void refreshPromoCodes() {
        loadPromoCodes();
    }
    
    private void clearAllPromoCodes() {
        // Show confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Clear All Promo Codes");
        confirmation.setHeaderText("Delete All Promo Codes");
        confirmation.setContentText("This will permanently delete ALL promo codes from the database.\n\n" +
                                   "This action cannot be undone!\n\n" +
                                   "Are you sure you want to continue?");
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a1a;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #e0e0e0;");
        
        java.util.Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Connection conn = null;
            PreparedStatement pst = null;
            
            try {
                conn = DBConnection.getConnection();
                
                // Step 1: Delete all promo codes
                String deleteSql = "DELETE FROM promo_codes";
                pst = conn.prepareStatement(deleteSql);
                int rowsAffected = pst.executeUpdate();
                pst.close();
                
                // Step 2: Reset AUTO_INCREMENT counter to 1
                if (rowsAffected > 0 || rowsAffected == 0) {
                    try {
                        String resetAutoIncrementSql = "ALTER TABLE promo_codes AUTO_INCREMENT = 1";
                        pst = conn.prepareStatement(resetAutoIncrementSql);
                        pst.executeUpdate();
                        pst.close();
                        System.out.println("AUTO_INCREMENT counter reset to 1");
                    } catch (Exception resetEx) {
                        System.err.println("Warning: Could not reset AUTO_INCREMENT: " + resetEx.getMessage());
                        // Continue even if reset fails
                    }
                }
                
                if (rowsAffected > 0) {
                    showAlert("Success", "All promo codes have been deleted successfully!\n\n" +
                             "Deleted: " + rowsAffected + " promo code(s)\n\n" +
                             "ID counter has been reset to 1.");
                    loadPromoCodes(); // Refresh the table
                    clearPromoCodeFields();
                } else {
                    // Even if no rows were deleted, reset the counter
                    showAlert("Info", "No promo codes found to delete.\n\n" +
                             "ID counter has been reset to 1.");
                    loadPromoCodes(); // Refresh the table
                }
                
            } catch (Exception e) {
                showAlert("Error", "Failed to delete all promo codes: " + e.getMessage());
                e.printStackTrace();
            } finally {
                DBConnection.closeResources(conn, pst, null);
            }
        }
    }
    
    private void clearPromoCodeFields() {
        if (promoCodeField != null) promoCodeField.clear();
        if (promoDiscountField != null) promoDiscountField.clear();
    }
    
    private void clearUserFields() {
        if (usernameField != null) usernameField.clear();
        if (passwordField != null) passwordField.clear();
        if (roleComboBox != null) roleComboBox.setValue(null);
    }
    
    private void clearProductFields() {
        if (productNameField != null) productNameField.clear();
        if (productDescriptionField != null) productDescriptionField.clear();
        if (productPriceField != null) productPriceField.clear();
        if (productStockField != null) productStockField.clear();
        if (productCategoryField != null) productCategoryField.clear();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Apply dark mode styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a1a;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #e0e0e0;");
        alert.showAndWait();
    }
    
    /**
     * Show sales report dialog with daily/weekly options
     */
    private void showSalesReportDialog() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Mondalak Coffee - Sales Report");
        // Set icon on the stage
        main.Main.setIconOnStage(reportStage);
        
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(10));
        mainLayout.setStyle("-fx-background-color: #000000;");
        
        Label titleLabel = new Label("Generate Sales Report");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #cccccc;");
        
        // Info label explaining date range
        Label infoLabel = new Label("Select a date range to generate sales report between two dates (dates can have gaps)");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888; -fx-font-style: italic;");
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(400);
        
        // Report type selection
        HBox reportTypeBox = new HBox(10);
        reportTypeBox.setAlignment(Pos.CENTER);
        Label reportTypeLabel = new Label("Report Type:");
        reportTypeLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-weight: bold;");
        ComboBox<String> reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll("Date Range (Custom)", "Weekly", "Monthly", "All Time");
        reportTypeCombo.setValue("Date Range (Custom)");
        reportTypeCombo.setStyle("-fx-min-width: 180px; -fx-background-color: #000000; -fx-text-fill: #cccccc; -fx-border-color: #333333;");
        reportTypeBox.getChildren().addAll(reportTypeLabel, reportTypeCombo);
        
        // Date pickers - show different options based on report type
        VBox dateContainer = new VBox(10);
        dateContainer.setAlignment(Pos.CENTER);
        
        // From Date (for Date Range)
        HBox fromDateBox = new HBox(10);
        fromDateBox.setAlignment(Pos.CENTER);
        Label fromDateLabel = new Label("From Date:");
        fromDateLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-weight: bold; -fx-min-width: 80px;");
        DatePicker fromDatePicker = new DatePicker(LocalDate.now().minusDays(7)); // Default to 7 days ago
        fromDatePicker.setStyle("-fx-min-width: 150px; -fx-background-color: #000000; -fx-text-fill: #cccccc; -fx-border-color: #333333;");
        fromDatePicker.setPromptText("Start date");
        javafx.application.Platform.runLater(() -> {
            if (fromDatePicker.getEditor() != null) {
                fromDatePicker.getEditor().setStyle("-fx-background-color: #000000; -fx-text-fill: #cccccc; -fx-border-color: #333333;");
            }
        });
        fromDateBox.getChildren().addAll(fromDateLabel, fromDatePicker);
        
        // To Date (for Date Range)
        HBox toDateBox = new HBox(10);
        toDateBox.setAlignment(Pos.CENTER);
        Label toDateLabel = new Label("To Date:");
        toDateLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-weight: bold; -fx-min-width: 80px;");
        DatePicker toDatePicker = new DatePicker(LocalDate.now()); // Default to today
        toDatePicker.setStyle("-fx-min-width: 150px; -fx-background-color: #000000; -fx-text-fill: #cccccc; -fx-border-color: #333333;");
        toDatePicker.setPromptText("End date");
        javafx.application.Platform.runLater(() -> {
            if (toDatePicker.getEditor() != null) {
                toDatePicker.getEditor().setStyle("-fx-background-color: #000000; -fx-text-fill: #cccccc; -fx-border-color: #333333;");
            }
        });
        toDateBox.getChildren().addAll(toDateLabel, toDatePicker);
        
        // Single Date (for Weekly/Monthly)
        HBox singleDateBox = new HBox(10);
        singleDateBox.setAlignment(Pos.CENTER);
        Label dateLabel = new Label("Select Date:");
        dateLabel.setStyle("-fx-text-fill: #cccccc;");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-min-width: 150px; -fx-background-color: #000000; -fx-text-fill: #cccccc; -fx-border-color: #333333;");
        javafx.application.Platform.runLater(() -> {
            if (datePicker.getEditor() != null) {
                datePicker.getEditor().setStyle("-fx-background-color: #000000; -fx-text-fill: #cccccc; -fx-border-color: #333333;");
            }
        });
        singleDateBox.getChildren().addAll(dateLabel, datePicker);
        singleDateBox.setVisible(false); // Hidden by default
        
        // Show/hide date pickers based on report type
        reportTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Date Range (Custom)".equals(newVal)) {
                fromDateBox.setVisible(true);
                toDateBox.setVisible(true);
                singleDateBox.setVisible(false);
                infoLabel.setVisible(true);
            } else if ("All Time".equals(newVal)) {
                fromDateBox.setVisible(false);
                toDateBox.setVisible(false);
                singleDateBox.setVisible(false);
                infoLabel.setVisible(false);
            } else {
                fromDateBox.setVisible(false);
                toDateBox.setVisible(false);
                singleDateBox.setVisible(true);
                infoLabel.setVisible(false);
            }
        });
        
        // Initially show date range for Date Range (Custom)
        fromDateBox.setVisible(true);
        toDateBox.setVisible(true);
        
        dateContainer.getChildren().addAll(fromDateBox, toDateBox, singleDateBox);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button generateButton = new Button("Generate Report");
        generateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: #cccccc; -fx-font-weight: bold; -fx-min-width: 120px;");
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: #cccccc; -fx-font-weight: bold; -fx-min-width: 120px;");
        buttonBox.getChildren().addAll(generateButton, closeButton);
        
        mainLayout.getChildren().addAll(titleLabel, infoLabel, reportTypeBox, dateContainer, buttonBox);
        
        // Event handlers
        generateButton.setOnAction(e -> {
            String reportType = reportTypeCombo.getValue();
            if ("Date Range (Custom)".equals(reportType)) {
                LocalDate fromDate = fromDatePicker.getValue();
                LocalDate toDate = toDatePicker.getValue();
                if (fromDate == null || toDate == null) {
                    showAlert("Input Error", "Please select both From Date and To Date");
                    return;
                }
                if (fromDate.isAfter(toDate)) {
                    showAlert("Input Error", "From Date cannot be after To Date");
                    return;
                }
                // Calculate gap between dates
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate);
                System.out.println("Generating report for date range: " + fromDate + " to " + toDate + 
                                 " (" + (daysBetween + 1) + " days)");
                generateSalesReport(reportType, fromDate, toDate);
            } else if ("All Time".equals(reportType)) {
                // All time report doesn't need dates
                generateSalesReport(reportType, null, null);
            } else {
                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate == null) {
                    showAlert("Input Error", "Please select a date");
                    return;
                }
                generateSalesReport(reportType, selectedDate, null);
            }
        });
        
        closeButton.setOnAction(e -> reportStage.close());
        
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
        Scene scene = new Scene(mainLayout, bounds.getWidth(), bounds.getHeight());
        try {
            scene.getStylesheets().add(AdminController.class.getResource("/application.css").toExternalForm());
        } catch (Exception e) {
            // CSS file not found, continue without it
        }
        reportStage.setScene(scene);
        reportStage.setFullScreen(true);
        reportStage.setFullScreenExitHint("");
        reportStage.show();
    }
    
    /**
     * Generate and display sales report based on type and date(s)
     */
    private void generateSalesReport(String reportType, LocalDate selectedDate, LocalDate toDate) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Build query based on report type
            String dateFilter = buildDateFilter(reportType, selectedDate, toDate);
            
            // First ensure payment_method column exists
            try {
                String alterSql = "ALTER TABLE sales ADD COLUMN payment_method VARCHAR(50) DEFAULT 'Cash'";
                PreparedStatement alterPst = conn.prepareStatement(alterSql);
                alterPst.execute();
                alterPst.close();
                System.out.println("Added payment_method column to sales table");
            } catch (Exception e) {
                // Column already exists, continue
                System.out.println("payment_method column check: " + e.getMessage());
            }
            
            String sql = "SELECT s.id as sale_id, s.cashier_id, u.username as cashier_username, " +
                        "s.total_amount, s.discount_amount, s.final_amount, " +
                        "IFNULL(s.payment_method, 'Cash') as payment_method, " +
                        "s.created_at, COUNT(si.id) as item_count " +
                        "FROM sales s " +
                        "LEFT JOIN users u ON s.cashier_id = u.id " +
                        "LEFT JOIN sale_items si ON s.id = si.sale_id " +
                        dateFilter +
                        " GROUP BY s.id, s.cashier_id, u.username, s.total_amount, " +
                        "s.discount_amount, s.final_amount, s.payment_method, s.created_at " +
                        "ORDER BY s.created_at DESC";
            
            System.out.println("Executing sales report query: " + sql);
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            
            ObservableList<SalesReport> reportData = FXCollections.observableArrayList();
            double totalSales = 0.0;
            double totalDiscount = 0.0;
            int transactionCount = 0;
            
            while (rs.next()) {
                int saleId = rs.getInt("sale_id");
                int cashierId = rs.getInt("cashier_id");
                String cashierUsername = rs.getString("cashier_username");
                if (cashierUsername == null) cashierUsername = "Unknown";
                
                double totalAmount = rs.getDouble("total_amount");
                double discountAmount = rs.getDouble("discount_amount");
                double finalAmount = rs.getDouble("final_amount");
                String paymentMethod = rs.getString("payment_method");
                Timestamp timestamp = rs.getTimestamp("created_at");
                LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();
                int itemCount = rs.getInt("item_count");
                
                SalesReport report = new SalesReport(saleId, cashierId, cashierUsername,
                                                    totalAmount, discountAmount, finalAmount,
                                                    paymentMethod, createdAt, itemCount);
                reportData.add(report);
                
                totalSales += finalAmount;
                totalDiscount += discountAmount;
                transactionCount++;
                
                System.out.println("Sale #" + saleId + ": Cashier=" + cashierUsername + 
                                 ", Amount=$" + finalAmount + ", Items=" + itemCount);
            }
            
            System.out.println("Report generated: " + transactionCount + " transactions, Total: $" + totalSales);
            
            // Display report in new window
            displaySalesReport(reportData, reportType, selectedDate, toDate, totalSales, totalDiscount, transactionCount);
            
        } catch (Exception e) {
            System.err.println("Error generating sales report: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to generate sales report: " + e.getMessage());
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    /**
     * Build SQL date filter based on report type
     */
    private String buildDateFilter(String reportType, LocalDate selectedDate, LocalDate toDate) {
        switch (reportType) {
            case "Date Range (Custom)":
                if (selectedDate != null && toDate != null) {
                    // Date range: from selectedDate to toDate (can have gaps)
                    return " WHERE DATE(s.created_at) BETWEEN '" + selectedDate.toString() + 
                           "' AND '" + toDate.toString() + "'";
                } else if (selectedDate != null) {
                    // Single date fallback
                    return " WHERE DATE(s.created_at) = '" + selectedDate.toString() + "'";
                }
                return "";
            case "Weekly":
                if (selectedDate == null) return "";
                LocalDate weekStart = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() - 1);
                LocalDate weekEnd = weekStart.plusDays(6);
                return " WHERE DATE(s.created_at) BETWEEN '" + weekStart.toString() + 
                       "' AND '" + weekEnd.toString() + "'";
            case "Monthly":
                if (selectedDate == null) return "";
                return " WHERE YEAR(s.created_at) = " + selectedDate.getYear() +
                       " AND MONTH(s.created_at) = " + selectedDate.getMonthValue();
            case "All Time":
            default:
                return "";
        }
    }
    
    /**
     * Display sales report in a new window with table view
     */
    private void displaySalesReport(ObservableList<SalesReport> reportData, String reportType, 
                                   LocalDate selectedDate, LocalDate toDate, double totalSales, 
                                   double totalDiscount, int transactionCount) {
        Stage reportWindow = new Stage();
        reportWindow.setTitle("Mondalak Coffee - Sales Report - " + reportType);
        // Set icon on the stage
        main.Main.setIconOnStage(reportWindow);
        
        VBox layout = new VBox(5);
        layout.setPadding(new Insets(5));
        layout.setStyle("-fx-background-color: #000000;");
        
        // Header
        Label headerLabel = new Label("Sales Report - " + reportType);
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #cccccc;");
        
        // Date label - show range for Date Range (Custom), single date for others
        Label dateLabel;
        if ("Date Range (Custom)".equals(reportType) && selectedDate != null && toDate != null) {
            // Calculate days in range
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(selectedDate, toDate) + 1;
            dateLabel = new Label("Period: " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + 
                                 " to " + toDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                                 " (" + daysBetween + " days)");
        } else if ("All Time".equals(reportType)) {
            dateLabel = new Label("Period: All Time (All Sales)");
        } else if (selectedDate != null) {
            dateLabel = new Label("Date: " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        } else {
            dateLabel = new Label("Period: All Records");
        }
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc; -fx-font-weight: bold;");
        
        // Summary section
        VBox summaryBox = new VBox(5);
        summaryBox.setStyle("-fx-background-color: #000000; -fx-padding: 5; -fx-background-radius: 0;");
        
        Label summaryTitle = new Label("Summary");
        summaryTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #cccccc;");
        
        Label transactionLabel = new Label("Total Transactions: " + transactionCount);
        transactionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc;");
        
        Label discountLabel = new Label(String.format("Total Discount: $%.2f", totalDiscount));
        discountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc;");
        
        Label totalLabel = new Label(String.format("Total Sales: $%.2f", totalSales));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        summaryBox.getChildren().addAll(summaryTitle, transactionLabel, discountLabel, totalLabel);
        
        // Table
        TableView<SalesReport> reportTable = new TableView<>();
        reportTable.setItems(reportData);
        reportTable.setStyle("-fx-background-color: #000000;");
        
        TableColumn<SalesReport, Integer> saleIdCol = new TableColumn<>("Sale ID");
        saleIdCol.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        saleIdCol.setPrefWidth(70);
        
        TableColumn<SalesReport, String> cashierCol = new TableColumn<>("Cashier");
        cashierCol.setCellValueFactory(new PropertyValueFactory<>("cashierUsername"));
        cashierCol.setPrefWidth(120);
        
        TableColumn<SalesReport, Double> totalAmountCol = new TableColumn<>("Total");
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalAmountCol.setPrefWidth(80);
        totalAmountCol.setCellFactory(col -> new TableCell<SalesReport, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
                setStyle("-fx-text-fill: #cccccc;");
            }
        });
        
        TableColumn<SalesReport, Double> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
        discountCol.setPrefWidth(80);
        discountCol.setCellFactory(col -> new TableCell<SalesReport, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
                setStyle("-fx-text-fill: #cccccc;");
            }
        });
        
        TableColumn<SalesReport, Double> finalAmountCol = new TableColumn<>("Final Amount");
        finalAmountCol.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));
        finalAmountCol.setPrefWidth(100);
        finalAmountCol.setCellFactory(col -> new TableCell<SalesReport, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
                setStyle("-fx-text-fill: #cccccc; -fx-font-weight: bold;");
            }
        });
        
        TableColumn<SalesReport, String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentCol.setPrefWidth(80);
        
        TableColumn<SalesReport, Integer> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(new PropertyValueFactory<>("itemCount"));
        itemsCol.setPrefWidth(60);
        
        TableColumn<SalesReport, LocalDateTime> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setPrefWidth(150);
        dateCol.setCellFactory(col -> new TableCell<SalesReport, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
                }
                setStyle("-fx-text-fill: #cccccc;");
            }
        });
        
        reportTable.getColumns().addAll(saleIdCol, cashierCol, totalAmountCol, discountCol, 
                                        finalAmountCol, paymentCol, itemsCol, dateCol);
        
        VBox.setVgrow(reportTable, Priority.ALWAYS);
        
        // Close button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: #cccccc; -fx-font-weight: bold; -fx-min-width: 100px;");
        closeButton.setOnAction(e -> reportWindow.close());
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(closeButton);
        
        layout.getChildren().addAll(headerLabel, dateLabel, summaryBox, reportTable, buttonBox);
        
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
        Scene scene = new Scene(layout, bounds.getWidth(), bounds.getHeight());
        try {
            scene.getStylesheets().add(AdminController.class.getResource("/application.css").toExternalForm());
        } catch (Exception e) {
            // CSS file not found, continue without it
        }
        reportWindow.setScene(scene);
        reportWindow.setFullScreen(true);
        reportWindow.setFullScreenExitHint("");
        reportWindow.show();
    }
}