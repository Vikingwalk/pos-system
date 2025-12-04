package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.Product;
import models.PromoCode;
import utils.DBConnection;
import utils.SessionManager;
import utils.BarcodeReceiver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class CashierController {
    private TableView<Product> productTable;
    private TextField searchField, productIdField, quantityField;
    private Button searchButton, addToCartButton, checkoutButton, printReceiptButton, clearCartButton, logoutButton;
    private TableView<Product> cartTable;
    private Label totalLabel, discountLabel, finalTotalLabel, promoCodeLabel;
    
    private ObservableList<Product> productList;
    private ObservableList<Product> cartList;
    private double totalAmount = 0;
    private double discountAmount = 0;
    private PromoCode appliedPromoCode = null;
    private boolean productTableInitialized = false;
    
    // Barcode receiver for phone scanning
    private BarcodeReceiver barcodeReceiver;
    
    public CashierController(TableView<Product> productTable, TextField searchField,
            Button searchButton, TableView<Product> cartTable,
            TextField productIdField, TextField quantityField,
            Button addToCartButton, Label totalLabel, Label discountLabel, 
            Label finalTotalLabel, Label promoCodeLabel,
            Button checkoutButton, Button printReceiptButton,
            Button clearCartButton, Button logoutButton) {
        
        initializeFields(productTable, searchField, productIdField, quantityField,
                        searchButton, addToCartButton, checkoutButton,
                        printReceiptButton, clearCartButton, cartTable, totalLabel,
                        discountLabel, finalTotalLabel, promoCodeLabel);
        
        initEvents();
        loadProducts();
        setupCartTable();
        updateTotals();
        
        // Start barcode receiver server for phone scanning
        startBarcodeReceiver();
    }
    
    private void initializeFields(TableView<Product> productTable, TextField searchField,
                                 TextField productIdField, TextField quantityField,
                                 Button searchButton,
                                 Button addToCartButton, Button checkoutButton,
                                 Button printReceiptButton, Button clearCartButton,
                                 TableView<Product> cartTable, Label totalLabel,
                                 Label discountLabel, Label finalTotalLabel, Label promoCodeLabel) {
        
        this.productTable = productTable;
        this.searchField = searchField;
        this.productIdField = productIdField;
        this.quantityField = quantityField;
        this.searchButton = searchButton;
        this.addToCartButton = addToCartButton;
        this.checkoutButton = checkoutButton;
        this.printReceiptButton = printReceiptButton;
        this.clearCartButton = clearCartButton;
        this.cartTable = cartTable;
        this.totalLabel = totalLabel;
        this.discountLabel = discountLabel;
        this.finalTotalLabel = finalTotalLabel;
        this.promoCodeLabel = promoCodeLabel;
        this.logoutButton = logoutButton;
        
        this.productList = FXCollections.observableArrayList();
        this.cartList = FXCollections.observableArrayList();
    }
    
    private void initEvents() {
        searchButton.setOnAction(e -> searchProducts());
        searchField.setOnAction(e -> searchProducts());
        
        addToCartButton.setOnAction(e -> addToCart());
        quantityField.setOnAction(e -> addToCart());
        
        checkoutButton.setOnAction(e -> checkout());
        printReceiptButton.setOnAction(e -> printReceipt());
        clearCartButton.setOnAction(e -> clearCart());
        
        if (logoutButton != null) {
            logoutButton.setOnAction(e -> {
                stopBarcodeReceiver();
                // Logout will be handled by LoginController
            });
        }
        
        // Product table selection
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Prefer barcode if available, otherwise use ID
                if (newVal.getBarcode() != null && !newVal.getBarcode().isEmpty()) {
                    productIdField.setText(newVal.getBarcode());
                } else {
                    productIdField.setText(String.valueOf(newVal.getId()));
                }
                quantityField.requestFocus();
            }
        });
    }
    
    private void loadProducts() {
        productList.clear();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            if (conn == null) {
                System.err.println("CRITICAL: Database connection is NULL!");
                showAlert("Database Error", "Cannot connect to database. Please check MySQL server.");
                return;
            }
            
            System.out.println("Loading products from database...");
            pst = conn.prepareStatement("SELECT * FROM products");
            rs = pst.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                String barcode = null;
                try {
                    barcode = rs.getString("barcode");
                } catch (Exception e) {
                    barcode = null;
                }
                
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("category"),
                    rs.getBoolean("is_available"),
                    barcode
                );
                productList.add(product);
                count++;
            }
            
            System.out.println("Cashier: Loaded " + productList.size() + " products total");
            
            if (count == 0) {
                System.err.println("WARNING: No products found in database!");
                showAlert("No Products", "No products found in database. Please add products in Admin dashboard.");
            }
            
            // Update the table with loaded products
            setupProductTable();
            
        } catch (Exception e) {
            System.err.println("ERROR loading products: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load products: " + e.getMessage());
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    private void setupProductTable() {
        if (!productTableInitialized) {
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
            
            TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
            priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
            priceCol.setPrefWidth(70);
            
            TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
            stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
            stockCol.setPrefWidth(50);
            
            productTable.getColumns().addAll(idCol, barcodeCol, nameCol, priceCol, stockCol);
            productTableInitialized = true;
        }
        
        refreshProductTableData();
    }
    
    private void refreshProductTableData() {
        productTable.setItems(productList);
        productTable.refresh();
    }
    
    private void refreshProductsAfterCheckout() {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            if (conn == null) {
                System.err.println("CRITICAL: Database connection is NULL during refresh!");
                return;
            }
            
            pst = conn.prepareStatement("SELECT * FROM products");
            rs = pst.executeQuery();
            
            ObservableList<Product> updatedList = FXCollections.observableArrayList();
            
            while (rs.next()) {
                String barcode = null;
                try {
                    barcode = rs.getString("barcode");
                } catch (Exception e) {
                    barcode = null;
                }
                
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("category"),
                    rs.getBoolean("is_available"),
                    barcode
                );
                updatedList.add(product);
            }
            
            productList.clear();
            productList.addAll(updatedList);
            productTable.refresh();
            
        } catch (Exception e) {
            System.err.println("ERROR refreshing products: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    private void setupCartTable() {
        cartTable.getItems().clear();
        cartTable.getColumns().clear();
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(80);
        priceCol.setCellFactory(col -> new TableCell<Product, Double>() {
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
        
        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Qty");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        quantityCol.setPrefWidth(60);
        
        TableColumn<Product, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getPrice() * cellData.getValue().getStock()
            ).asObject()
        );
        subtotalCol.setPrefWidth(80);
        subtotalCol.setCellFactory(col -> new TableCell<Product, Double>() {
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
        
        TableColumn<Product, Void> removeCol = new TableColumn<>("Remove");
        removeCol.setPrefWidth(80);
        removeCol.setCellFactory(param -> new TableCell<Product, Void>() {
            private final Button removeButton = new Button("Remove");
            
            {
                removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: #ffffff; -fx-font-size: 10px;");
                removeButton.setOnAction(event -> {
                    Product item = getTableView().getItems().get(getIndex());
                    removeItemFromCart(item);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        
        cartTable.getColumns().addAll(nameCol, priceCol, quantityCol, subtotalCol, removeCol);
        cartTable.setItems(cartList);
    }
    
    private void removeItemFromCart(Product item) {
        if (item == null) return;
        
        int currentQuantity = item.getStock();
        
        TextInputDialog dialog = new TextInputDialog(String.valueOf(currentQuantity));
        dialog.setTitle("Remove from Cart");
        dialog.setHeaderText("Remove: " + item.getName());
        dialog.setContentText("Current quantity: " + currentQuantity + "\nEnter quantity to remove:");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2c3e50;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #ecf0f1;");
        
        dialog.showAndWait().ifPresent(input -> {
            try {
                int quantityToRemove = Integer.parseInt(input.trim());
                
                if (quantityToRemove <= 0) {
                    showAlert("Input Error", "Quantity must be greater than 0");
                    return;
                }
                
                if (quantityToRemove > currentQuantity) {
                    showAlert("Input Error", "Cannot remove more than current quantity (" + currentQuantity + ")");
                    return;
                }
                
                if (quantityToRemove == currentQuantity) {
                    cartList.remove(item);
                } else {
                    int newQuantity = currentQuantity - quantityToRemove;
                    for (int i = 0; i < cartList.size(); i++) {
                        Product cartItem = cartList.get(i);
                        if (cartItem.getId() == item.getId()) {
                            Product updatedItem = new Product(
                                cartItem.getId(),
                                cartItem.getName(),
                                cartItem.getDescription(),
                                cartItem.getPrice(),
                                newQuantity,
                                cartItem.getCategory(),
                                true
                            );
                            cartList.set(i, updatedItem);
                            break;
                        }
                    }
                }
                
                cartTable.refresh();
                updateTotals();
                
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Please enter a valid number");
            }
        });
    }
    
    private void searchProducts() {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            productTable.setItems(productList);
            return;
        }
        
        ObservableList<Product> filteredList = FXCollections.observableArrayList();
        for (Product product : productList) {
            boolean matchName = product.getName().toLowerCase().contains(searchTerm.toLowerCase());
            boolean matchId = String.valueOf(product.getId()).equals(searchTerm);
            boolean matchBarcode = (product.getBarcode() != null && product.getBarcode().equals(searchTerm));
            
            if (matchName || matchId || matchBarcode) {
                filteredList.add(product);
            }
        }
        
        productTable.setItems(filteredList);
        
        if (filteredList.isEmpty()) {
            showAlert("No Results", "No products found matching: '" + searchTerm + "'");
        }
    }
    
    /**
     * UPDATED: Main entry point for adding items via manual input or barcode
     * Automatically applies ANY active promo code when product is scanned
     */
    private void addToCart() {
        try {
            String input = productIdField.getText().trim();
            
            if (input.isEmpty()) {
                showAlert("Input Error", "Please enter a barcode or product ID");
                return;
            }
            
            // Get quantity (default to 1 if empty or invalid)
            int quantity = 1;
            String quantityText = quantityField.getText().trim();
            
            if (!quantityText.isEmpty()) {
                try {
                    quantity = Integer.parseInt(quantityText);
                    if (quantity <= 0) {
                        showAlert("Input Error", "Quantity must be greater than 0");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert("Input Error", "Invalid quantity. Using 1 as default.");
                    quantity = 1;
                }
            }
            
            System.out.println("Adding to cart: Input=" + input + ", Quantity=" + quantity);
            
            // === STEP 1: Check if input is a product ===
            System.out.println("Step 1: Checking for product...");
            Product selectedProduct = findProductByBarcodeOrId(input);
            
            if (selectedProduct != null) {
                System.out.println("✓ Product found: " + selectedProduct.getName() + " (ID: " + selectedProduct.getId() + ")");
                
                // Validate product
                if (!selectedProduct.isAvailable()) {
                    showAlert("Product Unavailable", 
                        "Product '" + selectedProduct.getName() + "' is currently unavailable.");
                    return;
                }
                
                if (selectedProduct.getStock() <= 0) {
                    showAlert("Out of Stock", 
                        "Product '" + selectedProduct.getName() + "' is out of stock.");
                    return;
                }
                
                if (selectedProduct.getStock() < quantity) {
                    showAlert("Stock Error", 
                        "Insufficient stock. Available: " + selectedProduct.getStock());
                    return;
                }
                
                // Add product to cart
                addProductToCartDirectly(selectedProduct, quantity);
                
                // === STEP 2: Automatically check for ANY active promo code ===
                System.out.println("Step 2: Checking for active promo codes...");
                PromoCode activePromoCode = findAnyActivePromoCode();
                
                if (activePromoCode != null) {
                    System.out.println("✓ Promo code selected: " + activePromoCode.getCode() + " (" + activePromoCode.getDiscountPercent() + "%)");
                    applyPromoCode(activePromoCode);
                    
                    // Don't show alert if promo was auto-selected (only one active)
                    // Alert only shows when user manually chooses from multiple promos
                } else {
                    System.out.println("✗ No promo code applied");
                }
                
                // Clear fields for next scan
                productIdField.clear();
                quantityField.clear();
                productIdField.requestFocus();
                
            } else {
                // === STEP 3: Check if input is a promo code (manual override) ===
                System.out.println("Step 2: Not a product, checking if it's a promo code...");
                PromoCode promoCode = findPromoCodeByCode(input);
                
                if (promoCode != null) {
                    System.out.println("✓ Promo code found: " + promoCode.getCode());
                    applyPromoCode(promoCode);
                    showAlert("Promo Code Applied", 
                        "✓ Promo code applied: " + promoCode.getCode() + " (" + promoCode.getDiscountPercent() + "% off)\n" +
                        "Discount will be applied to current cart total.");
                    
                    // Clear fields
                    productIdField.clear();
                    quantityField.clear();
                    productIdField.requestFocus();
                } else {
                    // Nothing found
                    showAlert("Not Found", 
                        "Barcode/Code '" + input + "' not found.\n" +
                        "Not a product or promo code.");
                }
            }
            
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for Product ID and Quantity");
        }
    }
    
    /**
     * NEW METHOD: Find ALL active promo codes and let user choose
     * Returns the selected promo code or null if cancelled
     */
    private PromoCode findAnyActivePromoCode() {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        java.util.List<PromoCode> activePromoCodes = new java.util.ArrayList<>();
        
        try {
            conn = DBConnection.getConnection();
            
            // Find ALL active promo codes
            String sql = "SELECT id, code, discount_percent, is_active FROM promo_codes " +
                        "WHERE is_active = TRUE ORDER BY discount_percent DESC";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            
            while (rs.next()) {
                PromoCode promoCode = new PromoCode(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getDouble("discount_percent"),
                    rs.getBoolean("is_active")
                );
                activePromoCodes.add(promoCode);
            }
            
            System.out.println("Found " + activePromoCodes.size() + " active promo code(s)");
            
            // If no promo codes, return null
            if (activePromoCodes.isEmpty()) {
                System.out.println("✗ No active promo codes found");
                return null;
            }
            
            // If only one promo code, use it automatically
            if (activePromoCodes.size() == 1) {
                PromoCode promoCode = activePromoCodes.get(0);
                System.out.println("✓ Auto-selected (only one active): " + promoCode.getCode() + " (" + promoCode.getDiscountPercent() + "%)");
                return promoCode;
            }
            
            // If multiple promo codes, let user choose
            System.out.println("⚠ Multiple active promo codes found - showing selection dialog");
            return showPromoCodeSelectionDialog(activePromoCodes);
            
        } catch (Exception e) {
            System.err.println("Error finding active promo codes: " + e.getMessage());
            return null;
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    /**
     * Show dialog to select from multiple active promo codes
     */
    private PromoCode showPromoCodeSelectionDialog(java.util.List<PromoCode> promoCodes) {
        // Create choice dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Multiple Promo Codes Available");
        alert.setHeaderText("Multiple active promo codes found!");
        alert.setContentText("Please select which promo code to apply:");
        
        // Apply dark mode styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a1a;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #e0e0e0;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #2c3e50;");
        
        // Create custom content with radio buttons
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
        vbox.setStyle("-fx-padding: 10; -fx-background-color: #1a1a1a;");
        
        javafx.scene.control.ToggleGroup group = new javafx.scene.control.ToggleGroup();
        
        // Add info label
        Label infoLabel = new Label("Select a promo code to apply to this transaction:");
        infoLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12; -fx-font-weight: bold;");
        vbox.getChildren().add(infoLabel);
        
        // Add separator
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        separator.setStyle("-fx-background-color: #444444;");
        vbox.getChildren().add(separator);
        
        // Add radio button for each promo code
        for (PromoCode promo : promoCodes) {
            javafx.scene.control.RadioButton rb = new javafx.scene.control.RadioButton(
                promo.getCode() + " - " + promo.getDiscountPercent() + "% OFF"
            );
            rb.setToggleGroup(group);
            rb.setUserData(promo);
            rb.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13;");
            
            // Select first one by default
            if (promoCodes.indexOf(promo) == 0) {
                rb.setSelected(true);
            }
            
            vbox.getChildren().add(rb);
        }
        
        // Add "No Promo" option
        javafx.scene.control.RadioButton noPromoRb = new javafx.scene.control.RadioButton(
            "No Promo Code - Full Price"
        );
        noPromoRb.setToggleGroup(group);
        noPromoRb.setUserData(null);
        noPromoRb.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 13; -fx-font-weight: bold;");
        vbox.getChildren().add(noPromoRb);
        
        dialogPane.setContent(vbox);
        
        // Show dialog and wait for response
        java.util.Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            javafx.scene.control.RadioButton selectedRb = (javafx.scene.control.RadioButton) group.getSelectedToggle();
            if (selectedRb != null) {
                PromoCode selectedPromo = (PromoCode) selectedRb.getUserData();
                if (selectedPromo != null) {
                    System.out.println("✓ User selected: " + selectedPromo.getCode() + " (" + selectedPromo.getDiscountPercent() + "%)");
                    return selectedPromo;
                } else {
                    System.out.println("✗ User selected: No Promo Code");
                    return null;
                }
            }
        }
        
        System.out.println("✗ User cancelled promo selection");
        return null;
    }
    
    /**
     * Apply promo code and update UI
     */
    private void applyPromoCode(PromoCode promoCode) {
        if (promoCode == null || !promoCode.isActive()) {
            appliedPromoCode = null;
            discountAmount = 0;
            updateTotals();
            return;
        }
        
        // Store the promo code
        appliedPromoCode = promoCode;
        
        // Update the label immediately
        if (promoCodeLabel != null) {
            promoCodeLabel.setText(promoCode.getCode() + " (" + promoCode.getDiscountPercent() + "%)");
            promoCodeLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
        }
        
        // Recalculate totals
        updateTotals();
        
        System.out.println("✓ Promo code applied: " + promoCode.getCode() + " (" + promoCode.getDiscountPercent() + "%)");
    }
    
    /**
     * Find promo code by code (case-insensitive)
     */
    private PromoCode findPromoCodeByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        String searchCode = code.trim().toUpperCase();
        
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        PromoCode promoCode = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Use case-insensitive search
            String sql = "SELECT id, code, discount_percent, is_active FROM promo_codes " +
                        "WHERE UPPER(TRIM(code)) = UPPER(TRIM(?)) AND is_active = TRUE";
            pst = conn.prepareStatement(sql);
            pst.setString(1, searchCode);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                promoCode = new PromoCode(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getDouble("discount_percent"),
                    rs.getBoolean("is_active")
                );
                System.out.println("✓ Promo code found: " + promoCode.getCode());
            }
            
        } catch (Exception e) {
            System.err.println("Error finding promo code: " + e.getMessage());
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
        
        return promoCode;
    }
    
    /**
     * Update totals with promo code discount
     */
    private void updateTotals() {
        // Calculate total from cart items
        totalAmount = 0;
        for (Product item : cartList) {
            totalAmount += item.getPrice() * item.getStock();
        }
        
        // Recalculate discount if promo code is applied
        if (appliedPromoCode != null && appliedPromoCode.isActive()) {
            discountAmount = totalAmount * (appliedPromoCode.getDiscountPercent() / 100.0);
        } else {
            discountAmount = 0;
        }
        
        // Update UI labels
        if (totalLabel != null) {
            totalLabel.setText(String.format("$%.2f", totalAmount));
        }
        if (discountLabel != null) {
            discountLabel.setText(String.format("$%.2f", discountAmount));
        }
        if (finalTotalLabel != null) {
            finalTotalLabel.setText(String.format("$%.2f", totalAmount - discountAmount));
        }
        
        // Update promo code label
        if (promoCodeLabel != null) {
            if (appliedPromoCode != null) {
                promoCodeLabel.setText(appliedPromoCode.getCode() + " (" + appliedPromoCode.getDiscountPercent() + "%)");
                promoCodeLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
            } else {
                promoCodeLabel.setText("None");
                promoCodeLabel.setStyle("-fx-text-fill: #cccccc;");
            }
        }
    }
    
    private void checkout() {
        if (cartList.isEmpty()) {
            showAlert("Cart Empty", "Please add items to cart before checkout");
            return;
        }
        
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Insert sale record
            String saleSql = "INSERT INTO sales (cashier_id, total_amount, discount_amount, final_amount) VALUES (?, ?, ?, ?)";
            pst = conn.prepareStatement(saleSql, PreparedStatement.RETURN_GENERATED_KEYS);
            
            Integer cashierId = SessionManager.getCurrentUserId();
            if (cashierId == null) {
                showAlert("Error", "No user session found. Please log in again.");
                return;
            }
            
            pst.setInt(1, cashierId);
            pst.setDouble(2, totalAmount);
            pst.setDouble(3, discountAmount);
            pst.setDouble(4, totalAmount - discountAmount);
            pst.executeUpdate();
            
            int saleId;
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    saleId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to get sale ID");
                }
            }
            
            // Insert sale items and update stock
            for (Product item : cartList) {
                String itemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
                pst = conn.prepareStatement(itemSql);
                pst.setInt(1, saleId);
                pst.setInt(2, item.getId());
                pst.setInt(3, item.getStock());
                pst.setDouble(4, item.getPrice());
                pst.setDouble(5, item.getPrice() * item.getStock());
                pst.executeUpdate();
                
                String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ?";
                pst = conn.prepareStatement(updateStockSql);
                pst.setInt(1, item.getStock());
                pst.setInt(2, item.getId());
                pst.executeUpdate();
            }
            
            conn.commit();
            
            // Store sale info for receipt
            int finalSaleId = saleId;
            double finalTotalAmount = totalAmount;
            double finalDiscountAmount = discountAmount;
            double finalFinalAmount = totalAmount - discountAmount;
            PromoCode receiptPromoCode = appliedPromoCode;
            ObservableList<Product> receiptItems = FXCollections.observableArrayList();
            receiptItems.addAll(cartList);
            
            // Clear cart
            clearCart();
            
            // Refresh products
            refreshProductsAfterCheckout();
            
            // Show success
            showAlert("Success", "Sale #" + finalSaleId + " completed successfully!\n\nTotal: $" + 
                     String.format("%.2f", finalFinalAmount));
            
            // Print receipt
            printReceipt(finalSaleId, receiptItems, finalTotalAmount, finalDiscountAmount, 
                       finalFinalAmount, receiptPromoCode);
            
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            showAlert("Error", "Checkout failed: " + e.getMessage());
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    private void printReceipt(int saleId, ObservableList<Product> items, 
                             double subtotal, double discount, double finalTotal, 
                             PromoCode promoCode) {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("================================\n");
        receipt.append("        COFFEE SHOP POS\n");
        receipt.append("        Mondalak Coffee\n");
        receipt.append("================================\n");
        receipt.append("Sale ID: #").append(saleId).append("\n");
        receipt.append("Date: ").append(now.format(formatter)).append("\n");
        receipt.append("Cashier: ").append(SessionManager.getCurrentUsername()).append("\n");
        receipt.append("--------------------------------\n");
        receipt.append(String.format("%-20s %6s %10s\n", "Item", "Qty", "Amount"));
        receipt.append("--------------------------------\n");
        
        for (Product item : items) {
            receipt.append(String.format("%-20s %6d %10.2f\n", 
                item.getName(), item.getStock(), item.getPrice() * item.getStock()));
        }
        
        receipt.append("--------------------------------\n");
        receipt.append(String.format("%-20s %16.2f\n", "Subtotal:", subtotal));
        if (promoCode != null) {
            receipt.append(String.format("%-20s %16s\n", "Promo Code:", promoCode.getCode()));
            receipt.append(String.format("%-20s %16.2f\n", 
                "Discount (" + promoCode.getDiscountPercent() + "%):", discount));
        } else {
            receipt.append(String.format("%-20s %16.2f\n", "Discount:", discount));
        }
        receipt.append("--------------------------------\n");
        receipt.append(String.format("%-20s %16.2f\n", "TOTAL:", finalTotal));
        receipt.append("================================\n");
        receipt.append("        Thank You!\n");
        receipt.append("   Come Again Soon!\n");
        receipt.append("================================\n");
        
        System.out.println("\n" + receipt.toString() + "\n");
        
        TextArea receiptArea = new TextArea(receipt.toString());
        receiptArea.setEditable(false);
        receiptArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 11; " +
                           "-fx-background-color: #000000; -fx-text-fill: #cccccc;");
        
        Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
        receiptAlert.setTitle("Receipt - Sale #" + saleId);
        receiptAlert.setHeaderText("Transaction Receipt");
        receiptAlert.getDialogPane().setContent(receiptArea);
        receiptAlert.getDialogPane().setPrefSize(400, 500);
        
        DialogPane dialogPane = receiptAlert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #000000;");
        
        receiptAlert.show();
    }
    
    private void printReceipt() {
        if (cartList.isEmpty()) {
            showAlert("Cart Empty", "No items to print in receipt");
            return;
        }
        
        printReceipt(0, cartList, totalAmount, discountAmount, 
                    totalAmount - discountAmount, appliedPromoCode);
    }
    
    private void clearCart() {
        cartList.clear();
        cartTable.refresh();
        
        if (productIdField != null) {
            productIdField.clear();
        }
        if (quantityField != null) {
            quantityField.clear();
        }
        
        totalAmount = 0;
        discountAmount = 0;
        appliedPromoCode = null;
        updateTotals();
        
        if (productIdField != null) {
            productIdField.requestFocus();
        }
    }
    
    private Product findProductByBarcodeOrId(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        
        System.out.println("Searching for product with input: '" + input + "'");
        
        // First check in memory (fast lookup)
        for (Product product : productList) {
            // Check by barcode
            if (product.getBarcode() != null && product.getBarcode().equals(input)) {
                System.out.println("✓ Found in memory by BARCODE: " + product.getName() + " (Barcode: " + product.getBarcode() + ")");
                return product;
            }
            // Check by ID
            try {
                int productId = Integer.parseInt(input);
                if (product.getId() == productId) {
                    System.out.println("✓ Found in memory by ID: " + product.getName() + " (ID: " + product.getId() + ")");
                    return product;
                }
            } catch (NumberFormatException e) {
                // Not a number, skip ID check
            }
        }
        
        System.out.println("Not found in memory, checking database...");
        
        // If not in memory, query database (works for ANY product)
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        Product product = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Search by barcode OR id - works for ANY product in database
            String sql = "SELECT * FROM products WHERE barcode = ? OR id = ?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, input);
            
            // Try to parse as integer for ID search
            try {
                pst.setInt(2, Integer.parseInt(input));
            } catch (NumberFormatException e) {
                pst.setInt(2, -1); // Invalid ID
            }
            
            rs = pst.executeQuery();
            
            if (rs.next()) {
                String barcode = null;
                try {
                    barcode = rs.getString("barcode");
                } catch (Exception e) {
                    barcode = null;
                }
                
                product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("category"),
                    rs.getBoolean("is_available"),
                    barcode
                );
                
                System.out.println("✓ Found in DATABASE: " + product.getName() + 
                                 " (ID: " + product.getId() + 
                                 ", Barcode: " + (product.getBarcode() != null ? product.getBarcode() : "N/A") + 
                                 ", Price: $" + product.getPrice() + 
                                 ", Stock: " + product.getStock() + ")");
                
                // Add to list for future lookups (optimization)
                boolean alreadyInList = false;
                for (Product p : productList) {
                    if (p.getId() == product.getId()) {
                        alreadyInList = true;
                        break;
                    }
                }
                if (!alreadyInList) {
                    productList.add(product);
                    System.out.println("Added to memory cache for faster future lookups");
                }
            } else {
                System.out.println("✗ Product NOT found in database for input: '" + input + "'");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error finding product: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
        
        return product;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #000000;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #cccccc;");
        alert.showAndWait();
    }
    
    private void startBarcodeReceiver() {
        try {
            if (barcodeReceiver != null && barcodeReceiver.isRunning()) {
                barcodeReceiver.stop();
            }
            
            barcodeReceiver = new BarcodeReceiver(8080, this::handleReceivedBarcode);
            barcodeReceiver.start();
            
            String serverURL = barcodeReceiver.getServerURL() + "/scanner";
            showAlert("Phone Scanner Ready", 
                "Barcode scanner server is running!\n\n" +
                "Open this URL on your phone:\n" +
                serverURL + "\n\n" +
                "Make sure your phone is on the same Wi-Fi network.");
            
        } catch (Exception e) {
            System.err.println("Failed to start barcode receiver: " + e.getMessage());
        }
    }
    
    /**
     * UPDATED: Handle barcode from phone scanner
     * Automatically applies ANY active promo code when product is scanned
     */
    private void handleReceivedBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return;
        }
        
        try {
            // Update UI to show scanning
            Platform.runLater(() -> {
                productIdField.setText(barcode);
            });
            
            // === STEP 1: Check for product ===
            Product foundProduct = findProductByBarcodeOrId(barcode);
            
            // === STEP 2: Process on UI thread ===
            Platform.runLater(() -> {
                if (foundProduct != null) {
                    // Product found - validate and add
                    if (!foundProduct.isAvailable()) {
                        showAlert("Product Unavailable", 
                            "Product '" + foundProduct.getName() + "' is currently unavailable.");
                        return;
                    }
                    
                    if (foundProduct.getStock() <= 0) {
                        showAlert("Out of Stock", 
                            "Product '" + foundProduct.getName() + "' is out of stock.");
                        return;
                    }
                    
                    // Add product to cart
                    addProductToCartDirectly(foundProduct, 1);
                    
                    // === STEP 3: Automatically check for ANY active promo code ===
                    PromoCode activePromoCode = findAnyActivePromoCode();
                    
                    if (activePromoCode != null) {
                        applyPromoCode(activePromoCode);
                        showAlert("Success", 
                            "✓ Product added: " + foundProduct.getName() + "\n" +
                            "✓ Promo code auto-applied: " + activePromoCode.getCode() + " (" + activePromoCode.getDiscountPercent() + "% off)");
                    }
                    
                } else {
                    // Check if it's a promo code (manual override)
                    PromoCode promoCode = findPromoCodeByCode(barcode);
                    
                    if (promoCode != null) {
                        applyPromoCode(promoCode);
                        showAlert("Promo Code Applied", 
                            "✓ Promo code: " + promoCode.getCode() + " (" + promoCode.getDiscountPercent() + "% off)");
                    } else {
                        showAlert("Not Found", 
                            "Barcode '" + barcode + "' not found.\n" +
                            "Not a product or promo code.");
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error processing barcode: " + e.getMessage());
            Platform.runLater(() -> {
                productIdField.setText(barcode);
            });
        }
    }
    
    private void addProductToCartDirectly(Product product, int quantity) {
        if (product == null || quantity <= 0) return;
        
        if (product.getStock() < quantity) {
            showAlert("Stock Error", "Insufficient stock. Available: " + product.getStock());
            return;
        }
        
        boolean found = false;
        for (int i = 0; i < cartList.size(); i++) {
            Product cartItem = cartList.get(i);
            if (cartItem.getId() == product.getId()) {
                int newQuantity = cartItem.getStock() + quantity;
                if (newQuantity > product.getStock()) {
                    showAlert("Stock Error", "Total quantity exceeds available stock");
                    return;
                }
                Product updatedItem = new Product(cartItem.getId(), cartItem.getName(), 
                                     cartItem.getDescription(), cartItem.getPrice(),
                                     newQuantity, cartItem.getCategory(), true);
                cartList.set(i, updatedItem);
                cartTable.refresh();
                found = true;
                break;
            }
        }
        
        if (!found) {
            Product cartItem = new Product(product.getId(), product.getName(),
                                         product.getDescription(), product.getPrice(),
                                         quantity, product.getCategory(), true);
            cartList.add(cartItem);
        }
        
        updateTotals();
    }
    
    public void stopBarcodeReceiver() {
        if (barcodeReceiver != null) {
            barcodeReceiver.stop();
            barcodeReceiver = null;
        }
    }
}