# Coffee Shop POS System - Complete Documentation

## Table of Contents

1. [Project Overview](#project-overview)
2. [System Requirements](#system-requirements)
3. [Installation Guide](#installation-guide)
4. [Database Setup](#database-setup)
5. [Configuration](#configuration)
6. [Running the Application](#running-the-application)
7. [User Guide](#user-guide)
8. [Mobile Barcode Scanner](#mobile-barcode-scanner)
9. [Network Configuration](#network-configuration)
10. [Troubleshooting](#troubleshooting)
11. [Project Structure](#project-structure)
12. [Development Guide](#development-guide)

---

## Project Overview

The Coffee Shop POS System is a comprehensive Point of Sale application designed for coffee shop operations. It provides complete inventory management, sales processing, user authentication, and mobile barcode scanning capabilities.

### Key Features

- **User Management**: Role-based access control (Admin/Cashier)
- **Product Management**: Full CRUD operations with automatic barcode generation
- **Sales Processing**: Complete transaction handling with cart management
- **Inventory Tracking**: Real-time stock management
- **Mobile Scanner**: Wireless barcode scanning via smartphone
- **Sales Reporting**: Comprehensive sales reports and analytics
- **Receipt Generation**: Transaction receipt printing

### Technology Stack

- **Language**: Java 17+
- **UI Framework**: JavaFX 20.0.2
- **Database**: MySQL 8.0+
- **Build Tool**: Eclipse IDE
- **Module System**: Java Platform Module System (JPMS)

---

## System Requirements

### Minimum Requirements

- **Operating System**: Windows 10/11, macOS 10.14+, or Linux
- **Java**: JDK 17 or higher
- **JavaFX**: SDK 20.0.2
- **Database**: MySQL 8.0 or higher
- **RAM**: 4 GB minimum (8 GB recommended)
- **Storage**: 500 MB free space
- **Network**: Wi-Fi connection for mobile scanner feature

### Development Requirements

- **IDE**: Eclipse IDE (or IntelliJ IDEA)
- **MySQL Connector**: mysql-connector-j-9.4.0.jar or later
- **JavaFX SDK**: 20.0.2

---

## Installation Guide

### Step 1: Install Prerequisites

1. **Install Java JDK 17+**
   - Download from Oracle or Eclipse Adoptium
   - Verify installation: `java -version`

2. **Install JavaFX SDK 20.0.2**
   - Download from https://openjfx.io/
   - Extract to a location (e.g., `C:\javafx-sdk-20.0.2`)

3. **Install MySQL Server 8.0+**
   - Download from https://dev.mysql.com/downloads/mysql/
   - Install with default settings
   - Note your root password

4. **Install Eclipse IDE**
   - Download Eclipse IDE for Java Developers
   - Install JavaFX plugin if needed

### Step 2: Import Project

1. Open Eclipse IDE
2. File → Import → Existing Projects into Workspace
3. Browse to the CoffeeShopPOS folder
4. Select the project and click Finish

### Step 3: Configure Build Path

1. Right-click project → Properties → Java Build Path
2. Add JavaFX SDK to Modulepath:
   - Libraries → Add Library → User Library
   - Create new library named "JavaFX"
   - Add external JARs from JavaFX SDK lib folder
3. Add MySQL Connector:
   - Libraries → Add External JARs
   - Select mysql-connector-j-9.4.0.jar

---

## Database Setup

### Step 1: Create Database

Open MySQL command line or MySQL Workbench and run:

```sql
CREATE DATABASE pos_db;
USE pos_db;
```

### Step 2: Create Tables

Run the SQL script located at `src/docs/database_setup.sql` or execute the following:

```sql
-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('admin', 'cashier') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Products table
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock INT NOT NULL CHECK (stock >= 0),
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_available BOOLEAN DEFAULT TRUE,
    barcode VARCHAR(50)
);

-- Sales table
CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cashier_id INT,
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    final_amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('cash', 'card', 'mobile') DEFAULT 'cash',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cashier_id) REFERENCES users(id)
);

-- Sale items table
CREATE TABLE sale_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT,
    product_id INT,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Inventory logs table
CREATE TABLE inventory_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    change_type ENUM('sale', 'restock', 'adjustment'),
    quantity_change INT NOT NULL,
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

### Step 3: Create Default Admin User

```sql
INSERT INTO users (username, password_hash, role) 
VALUES ('admin', 
        SHA2('admin123', 256),
        'admin');
```

**Default Credentials:**
- Username: `admin`
- Password: `admin123`

⚠️ **Important**: Change the default password immediately in production!

---

## Configuration

### Database Connection

Edit `src/utils/DBConnection.java` to configure database connection:

```java
private static final String URL = "jdbc:mysql://localhost:3306/pos_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
```

Replace `your_mysql_password` with your actual MySQL root password.

---

## Running the Application

### From Eclipse

1. Ensure MySQL server is running
2. Right-click `src/main/Main.java`
3. Select Run As → Java Application
4. The login screen should appear

### First Login

Use the default credentials:
- **Username**: `admin`
- **Password**: `admin123`

After login, you'll be directed to the Admin Dashboard.

---

## User Guide

### Admin Dashboard

The Admin Dashboard provides full system control:

#### User Management
- **Add User**: Create new admin or cashier accounts
- **Delete User**: Remove user accounts
- **View Users**: See all registered users

#### Product Management
- **Add Product**: Create new products with automatic ID and barcode generation
- **Edit Product**: Update product details, prices, and stock
- **Delete Product**: Remove products from inventory
- **Search Products**: Find products by name, ID, or barcode
- **Update Stock**: Manually adjust inventory levels

#### Sales Reports
- Generate sales reports by date range
- Filter by cashier
- View detailed transaction history
- Export reports

### Cashier Dashboard

The Cashier Dashboard is designed for daily sales operations:

#### Product Search
- Search by product name (partial match)
- Search by product ID (exact match)
- Search by barcode (exact match)

#### Shopping Cart
- Add products to cart with quantity
- View cart total and itemized list
- Apply discounts (percentage or fixed amount)
- Remove items from cart
- Clear entire cart

#### Sales Processing
- **Checkout**: Complete sale transaction
- **Receipt**: Generate and print receipt
- **Stock Update**: Automatic inventory reduction
- **Transaction History**: View recent sales

---

## Mobile Barcode Scanner

### Overview

The system includes a wireless barcode scanner that uses your smartphone's camera. No additional hardware or apps required - just use your phone's web browser.

### How It Works

1. When a cashier logs in, the POS application starts an HTTPS server
2. The server generates an SSL certificate automatically
3. Your phone connects to the server via Wi-Fi
4. The phone's browser displays a scanner interface
5. Camera scans barcodes and sends them to the POS system

### Setup Instructions

#### Step 1: Start the Application

1. Run the POS application
2. Login as Cashier (or Admin)
3. Check the console output for the scanner URL

The console will display:
```
========================================
Barcode Receiver Server Started (HTTPS)!
========================================
Open on your phone:
https://192.168.1.100:8088/scanner
========================================
```

#### Step 2: Connect Your Phone

1. Ensure your phone is on the **same Wi-Fi network** as the computer
2. Open Chrome (Android) or Safari (iOS) browser
3. Type the URL shown in the console exactly
4. Accept the security warning:
   - Tap "Advanced"
   - Tap "Proceed to [IP address] (unsafe)"
   - This is safe - it's your own computer's certificate

#### Step 3: Grant Camera Permission

1. The scanner page will load
2. Tap "Start Camera" button
3. Grant camera permission when prompted
4. Camera view will appear

#### Step 4: Scan Barcodes

1. Position barcode horizontally in the camera view
2. Hold phone 6-12 inches from barcode
3. Ensure good lighting
4. Wait for beep sound and vibration
5. Barcode is automatically added to cart

### Manual Entry Mode

If camera scanning doesn't work, you can use manual entry:
1. Type the barcode number in the manual entry field
2. Tap "Send" or press Enter
3. Product is added to cart

### Troubleshooting Scanner

**Cannot connect to server:**
- Verify both devices on same Wi-Fi network
- Check firewall allows Java (port 8088)
- Ensure POS application is running
- Try the IP address shown in console

**Certificate error:**
- This is normal for self-signed certificates
- Click "Advanced" → "Proceed"
- Only do this for your own IP address

**Camera not working:**
- Grant camera permission in browser settings
- Use HTTPS (not HTTP) - camera requires HTTPS
- Try different browser (Chrome/Safari recommended)

**Barcode not detected:**
- Improve lighting (most common issue)
- Hold barcode horizontally
- Move closer (6-8 inches)
- Hold phone steady for 2-3 seconds

---

## Network Configuration

### Automatic IP Detection

The system automatically detects your network IP address and includes it in the SSL certificate. This allows the scanner to work on any Wi-Fi network.

### Supported Networks

- ✅ Home Wi-Fi
- ✅ Office Wi-Fi
- ✅ Mobile Hotspot
- ✅ Ethernet connection

### Changing Networks

If you move to a different network:

1. Close the POS application
2. Delete the file `barcode-server.keystore` (in project root)
3. Restart the POS application
4. New certificate will be generated with new IP
5. Use the new URL from console

### Firewall Configuration

**Windows Firewall:**
1. When first running, Windows will prompt to allow Java
2. Click "Allow Access" for both Private and Public networks
3. Or manually add Java through Windows Security → Firewall

**Port Configuration:**
- Default port: 8088
- If port is busy, system tries 8089, 8090, etc.
- Check console for actual port being used

---

## Troubleshooting

### Database Connection Issues

**Error: Cannot connect to database**

Solutions:
- Verify MySQL server is running
- Check database name is `pos_db`
- Verify credentials in `DBConnection.java`
- Ensure MySQL port 3306 is not blocked
- Test connection: `mysql -u root -p`

### Application Won't Start

**Error: JavaFX not found**

Solutions:
- Add JavaFX SDK to build path
- Verify JavaFX version is 20.0.2
- Check module-info.java includes javafx modules

**Error: Module not found**

Solutions:
- Clean and rebuild project
- Verify module-info.java is correct
- Check all required modules are available

### Scanner Connection Issues

**Cannot connect from phone**

Solutions:
- Verify same Wi-Fi network
- Check firewall settings
- Use IP from console (not from memory)
- Try HTTP if HTTPS fails (check console)

**Certificate errors**

Solutions:
- Delete `barcode-server.keystore` and restart
- Accept certificate warning (click Advanced → Proceed)
- Verify IP matches console output

### Product Not Found

**Barcode scanned but product not found**

Solutions:
- Verify product exists in database
- Check barcode matches database value
- Ensure product is available (not disabled)
- Check stock level is sufficient

---

## Project Structure

```
CoffeeShopPOS/
├── src/
│   ├── main/
│   │   ├── Main.java              # Application entry point
│   │   ├── application.css        # Global styles
│   │   └── resources/
│   │       ├── application.css
│   │       └── icon.png
│   │
│   ├── controllers/
│   │   ├── LoginController.java   # Authentication
│   │   ├── AdminController.java   # Admin dashboard
│   │   └── CashierController.java # Cashier dashboard
│   │
│   ├── models/
│   │   ├── Product.java           # Product entity
│   │   ├── User.java              # User entity
│   │   ├── Sale.java              # Sale transaction
│   │   ├── SaleItem.java          # Sale line item
│   │   ├── CartItem.java          # Cart item
│   │   └── SalesReport.java       # Report data
│   │
│   ├── utils/
│   │   ├── DBConnection.java      # Database connection pool
│   │   ├── SessionManager.java    # User session
│   │   ├── BarcodeGenerator.java  # EAN-13 barcode generation
│   │   ├── BarcodeReceiver.java   # Mobile scanner server
│   │   ├── SimpleSSLUtil.java     # SSL certificate generation
│   │   ├── SSLUtil.java           # SSL utilities
│   │   ├── ReportGenerator.java   # Sales reports
│   │   └── XMLLoader.java         # XML UI parser
│   │
│   ├── xml/
│   │   ├── login.xml              # Login screen layout
│   │   ├── admin_dashboard.xml    # Admin UI layout
│   │   └── cashier_dashboard.xml  # Cashier UI layout
│   │
│   ├── docs/
│   │   └── database_setup.sql     # Database schema
│   │
│   └── module-info.java           # Java module descriptor
│
└── barcode-server.keystore        # SSL certificate (auto-generated)
```

---

## Development Guide

### Architecture

The application follows MVC (Model-View-Controller) pattern:

- **Models**: Data entities (Product, User, Sale, etc.)
- **Views**: XML layout files
- **Controllers**: Business logic (LoginController, AdminController, CashierController)

### Key Components

**DBConnection**: Manages database connection pool (max 10 connections)

**BarcodeGenerator**: Generates EAN-13 barcodes for products

**BarcodeReceiver**: HTTP/HTTPS server for mobile scanner

**SessionManager**: Manages user session state

**XMLLoader**: Parses XML files and creates JavaFX UI

### Adding New Features

1. **New Model**: Create class in `src/models/`
2. **New Controller**: Create class in `src/controllers/`
3. **New UI**: Create XML file in `src/xml/`
4. **Update module-info.java**: Add any new module requirements

### Database Schema

**users**: User accounts with role-based access
**products**: Product inventory with barcodes
**sales**: Sales transactions
**sale_items**: Individual items in sales
**inventory_logs**: Inventory change history

### Barcode System

- **Format**: EAN-13 (13 digits)
- **Generation**: Automatic on product creation
- **Structure**: Prefix(3) + Product ID(6) + Random(3) + Check Digit(1)
- **Validation**: Check digit algorithm included

### Security

- **Passwords**: SHA-256 hashing
- **HTTPS**: SSL/TLS encryption for mobile scanner
- **Session**: User session management
- **Access Control**: Role-based permissions

---

## Class Documentation

This section provides detailed information about each class in the application, explaining their purpose, responsibilities, and how they work together.

### Main Package

#### `Main.java`
**Purpose**: Application entry point and window manager

**Responsibilities**:
- Extends `javafx.application.Application` to launch the JavaFX application
- Initializes the primary stage (main window) in full-screen mode
- Loads and displays the login screen on startup
- Manages application icon loading
- Provides static method `showLoginScreen()` for returning to login from other screens
- Handles error scenarios with fallback UI

**Key Methods**:
- `start(Stage)`: Called by JavaFX framework when application launches
- `showLoginScreen()`: Loads login XML and initializes LoginController
- `setApplicationIcon(Stage)`: Sets application icon for taskbar/window
- `showErrorScreen(String)`: Displays error fallback UI if loading fails

**How It Works**:
1. JavaFX calls `start()` when application launches
2. Sets full-screen mode and loads login screen
3. Uses `XMLLoader` to parse login.xml and create JavaFX Scene
4. Retrieves UI components from XMLLoader's component registry
5. Initializes `LoginController` with UI components
6. User interacts with login screen to authenticate

---

### Controllers Package

#### `LoginController.java`
**Purpose**: Handles user authentication and navigation to appropriate dashboards

**Responsibilities**:
- Validates user credentials against database
- Manages login UI interactions (username, password fields)
- Routes users to Admin or Cashier dashboard based on role
- Sets up user session via `SessionManager`
- Handles logout functionality
- Initializes AdminController and CashierController after successful login

**Key Methods**:
- `login()`: Main login handler triggered by button press or Enter key
- `tryDatabaseLogin(String, String)`: Attempts database authentication with SHA-256 password hashing
- `handleDemoLogin(String, String)`: Fallback demo login (admin/admin123, cashier/cashier123)
- `navigateToDashboard(String)`: Routes to admin or cashier dashboard based on role
- `initializeAdminController(Scene)`: Sets up admin dashboard with all UI components
- `initializeCashierController(Scene)`: Sets up cashier dashboard with all UI components
- `logout()`: Clears session and returns to login screen

**Authentication Flow**:
1. User enters username and password
2. `tryDatabaseLogin()` queries database for matching user
3. Password is hashed using SHA-256 and compared with stored hash
4. If valid, `SessionManager.setCurrentUser()` is called
5. `navigateToDashboard()` loads appropriate dashboard XML
6. Dashboard controller is initialized with UI components

**Relationships**:
- Uses: `DBConnection`, `SessionManager`, `XMLLoader`, `User` model
- Creates: `AdminController`, `CashierController`
- Called by: `Main` (initialization)

---

#### `AdminController.java`
**Purpose**: Manages users, products, and sales reporting for administrators

**Responsibilities**:
- User management (add, delete, refresh users)
- Product management (add, delete, update stock, refresh products)
- Sales report generation and display
- Automatic barcode generation for new products
- Welcome message personalization

**Key Methods**:
- `loadUsers()`: Loads all users from database into table
- `addUser()`: Creates new user with hashed password
- `deleteUser()`: Removes user from database
- `loadProducts()`: Loads all products from database into table
- `addProduct()`: Creates new product with auto-generated EAN-13 barcode
- `deleteProduct()`: Removes product from database
- `reduceStock()`: Manually reduces product stock quantity
- `generateSalesReport()`: Creates and displays sales report with filtering
- `setupUserTableStructure()`: Configures user table columns
- `setupProductTableStructure()`: Configures product table columns

**Barcode Integration**:
- When adding a product, `BarcodeGenerator.generateEAN13(productId)` is called
- Generated barcode is stored in database `products.barcode` column
- Barcode column is automatically added if missing from database schema

**Relationships**:
- Uses: `DBConnection`, `BarcodeGenerator`, `Product`, `User`, `SalesReport`, `ReportGenerator`
- Created by: `LoginController` after admin login
- UI: `admin_dashboard.xml`

---

#### `CashierController.java`
**Purpose**: Handles sales transactions, cart management, product search, and barcode scanning

**Responsibilities**:
- Product browsing and searching (by name, ID, or barcode)
- Cart management (add, update, clear items)
- Checkout processing with transaction management
- Receipt printing
- Barcode scanning integration (phone scanner)
- Stock validation before adding to cart
- Discount application (percentage or fixed amount)
- Welcome message personalization

**Key Methods**:
- `loadProducts()`: Loads products from database
- `searchProducts()`: Searches products by name (partial), ID (exact), or barcode (exact)
- `addToCart()`: Adds product to cart with quantity validation
- `addProductToCartDirectly(Product, int)`: Optimized direct cart addition bypassing redundant lookups
- `updateTotals()`: Calculates and displays cart totals (subtotal, discount, final)
- `checkout()`: Processes sale transaction with database updates
- `printReceipt()`: Generates and displays receipt
- `clearCart()`: Empties shopping cart
- `findProductInList(String)`: Fast in-memory product lookup
- `findProductByBarcodeOrId(String)`: Database product lookup
- `handleReceivedBarcode(String)`: Processes barcode from phone scanner
- `startBarcodeReceiver()`: Initializes phone scanner server
- `stopBarcodeReceiver()`: Stops scanner server on logout

**Barcode Scanning Flow**:
1. Cashier logs in → `BarcodeReceiver` server starts automatically
2. Phone connects to server URL (HTTPS/HTTP)
3. User scans barcode with phone camera
4. JavaScript sends barcode to server via POST request
5. `BarcodeHandler` receives barcode
6. `handleReceivedBarcode()` is called via callback
7. Product is found and automatically added to cart
8. Success notification shown on phone

**Optimizations**:
- Fast in-memory product lookup before database query
- Direct cart addition bypasses redundant lookups
- 5-second cooldown prevents duplicate scans
- Single-pass product search (checks barcode and ID simultaneously)

**Relationships**:
- Uses: `DBConnection`, `BarcodeReceiver`, `Product`, `SessionManager`
- Created by: `LoginController` after cashier login
- UI: `cashier_dashboard.xml`

---

### Models Package

#### `Product.java`
**Purpose**: Represents a product/item in the inventory with barcode support

**Fields**:
- `id` (int): Unique product identifier (auto-generated by database)
- `name` (String): Product name
- `description` (String): Product description
- `price` (double): Product price per unit
- `stock` (int): Current stock quantity
- `category` (String): Product category
- `isAvailable` (boolean): Availability status
- `barcode` (String): Product barcode (EAN-13 format or null)

**Constructors**:
1. `Product(id, name, description, price, stock, category, isAvailable)`: Without barcode
2. `Product(id, name, description, price, stock, category, isAvailable, barcode)`: With barcode

**Key Methods**:
- `isValid()`: Validates product data (name not empty, price/stock >= 0)
- `getBarcode()`: Returns barcode string (may be null)
- `setBarcode(String)`: Sets barcode value

**Relationships**:
- Used by: `AdminController`, `CashierController`, `CartItem`
- Related to: `BarcodeGenerator` (generates barcodes for products)
- Stored in: `products` database table

---

#### `User.java`
**Purpose**: Represents a system user (admin or cashier) with password security

**Fields**:
- `id` (int): Unique user identifier
- `username` (String): Login username
- `passwordHash` (String): Hashed password (SHA-256)
- `role` (String): User role ("admin" or "cashier")
- `isActive` (boolean): Account active status

**Key Methods**:
- `hashPassword(String)`: Static method to hash passwords using SHA-256
- `verifyPassword(String)`: Verifies password against stored hash
- `toString()`: Returns formatted string representation

**Security**:
- Passwords are hashed using SHA-256 algorithm
- Never stored in plain text
- Hash comparison for authentication

**Relationships**:
- Used by: `LoginController`, `AdminController`, `SessionManager`
- Stored in: `users` database table

---

#### `Sale.java`
**Purpose**: Represents a completed sales transaction

**Fields**:
- `id` (int): Unique sale identifier
- `cashierId` (int): ID of cashier who processed the sale
- `totalAmount` (double): Total before discount
- `discountAmount` (double): Discount applied
- `finalAmount` (double): Final amount after discount
- `paymentMethod` (String): Payment method (cash/card/mobile)
- `createdAt` (LocalDateTime): Transaction timestamp
- `items` (List<SaleItem>): List of items in the sale

**Relationships**:
- Contains: `SaleItem` objects
- Used by: `CashierController`, `AdminController` (for reports)
- Stored in: `sales` database table

---

#### `SaleItem.java`
**Purpose**: Represents a single item within a sale transaction

**Fields**:
- `id` (int): Unique sale item identifier
- `saleId` (int): Reference to parent sale
- `productId` (int): Reference to product
- `quantity` (int): Quantity purchased
- `unitPrice` (double): Price per unit at time of sale
- `subtotal` (double): Total for this item (quantity × unitPrice)

**Relationships**:
- Belongs to: `Sale`
- References: `Product`
- Stored in: `sale_items` database table

---

### Utils Package

#### `DBConnection.java`
**Purpose**: Manages database connections using a connection pool pattern

**Responsibilities**:
- Connection pool management (max 10 connections)
- Connection lifecycle (create, reuse, release)
- Connection validation
- Resource cleanup

**Key Methods**:
- `getConnection()`: Gets connection from pool or creates new one
- `releaseConnection(Connection)`: Returns connection to pool
- `closeResources(Connection, PreparedStatement, ResultSet)`: Safely closes all resources
- `testConnection()`: Tests database connectivity
- `isDatabaseAvailable()`: Checks if database is accessible

**Connection Pool Pattern**:
- Pool initialized with 3 connections on startup
- Connections are reused for better performance
- Invalid connections are replaced automatically
- Pool size limited to 10 connections

**Configuration**:
```java
URL: "jdbc:mysql://localhost:3306/pos_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
USER: "root"
PASSWORD: "Azerty123@2003"  // Change to your MySQL password
```

**Relationships**:
- Used by: All controllers and models that need database access
- Provides: Connection management for entire application

---

#### `SessionManager.java`
**Purpose**: Manages user session data (ID, username, role) across the application

**Responsibilities**:
- Store current user information
- Provide session data to controllers
- Clear session on logout

**Key Methods**:
- `setCurrentUser(int, String, String)`: Sets current session
- `clearSession()`: Clears session data
- `getCurrentUserId()`: Returns current user ID
- `getCurrentUsername()`: Returns current username
- `getCurrentUserRole()`: Returns current user role
- `isLoggedIn()`: Checks if user is logged in

**Design Pattern**: Singleton (static methods, no instance)

**Relationships**:
- Used by: `LoginController`, `AdminController`, `CashierController`
- Set by: `LoginController` (on successful login)
- Cleared by: `LoginController` (on logout)

---

#### `BarcodeGenerator.java`
**Purpose**: Utility class for generating and validating EAN-13 barcodes

**Key Methods**:
- `generateEAN13(int)`: Generates EAN-13 barcode from product ID
- `generateSimple(int)`: Generates simple barcode (PRODXXXXXX format)
- `calculateEAN13CheckDigit(String)`: Calculates EAN-13 check digit
- `validateEAN13(String)`: Validates EAN-13 barcode format
- `extractProductIdFromEAN13(String)`: Extracts product ID from EAN-13 barcode
- `extractProductIdFromSimple(String)`: Extracts product ID from simple barcode

**EAN-13 Format**:
- **Structure**: Prefix(3) + Product ID(6) + Random(3) + Check Digit(1) = 13 digits
- **Prefix**: "200" (internal use, not a real country code)
- **Product ID**: Zero-padded 6 digits (positions 3-8)
- **Random**: 3 random digits for uniqueness (positions 9-11)
- **Check Digit**: Calculated using EAN-13 algorithm (position 13)

**Check Digit Algorithm** (EAN-13 Standard):
1. Sum digits at odd positions (1st, 3rd, 5th, etc.) - multiply by 1
2. Sum digits at even positions (2nd, 4th, 6th, etc.) - multiply by 3
3. Total sum modulo 10
4. Check digit = (10 - (sum % 10)) % 10

**Relationships**:
- Used by: `AdminController` (when adding products)
- Related to: `Product` (barcode field)
- Standard: ISO/IEC 15420 (EAN-13 specification)

---

#### `BarcodeReceiver.java`
**Purpose**: HTTP/HTTPS server that receives barcode data from mobile devices and serves the scanner web page

**Responsibilities**:
- Creates HTTP/HTTPS server on local network
- Serves scanner web page to mobile devices
- Receives barcode data via POST requests
- Processes barcodes and calls callback function
- Handles CORS for cross-origin requests
- Automatic port detection (tries 8080, 8081, etc.)
- HTTPS with self-signed certificate (falls back to HTTP)

**Key Methods**:
- `start()`: Starts the server (tries HTTPS first, falls back to HTTP)
- `stop()`: Stops the server
- `isRunning()`: Checks if server is running
- `getServerURL()`: Returns server URL (https:// or http://)
- `getLocalIP()`: Gets local network IP address
- `createSSLContext()`: Creates SSL context for HTTPS
- `isPortAvailable(int)`: Checks if port is available

**Inner Classes**:
- `BarcodeHandler`: Handles POST requests to `/barcode`
- `ScannerPageHandler`: Serves HTML/JavaScript scanner page
- `RootHandler`: Redirects `/` to `/scanner`

**Scanner Web Page Features**:
- Camera-based barcode scanning using html5-qrcode library
- Manual barcode entry fallback
- Success/error notifications
- 5-second cooldown to prevent duplicate scans
- Browser notification support
- Responsive design for mobile devices

**HTTPS Implementation**:
- Uses `SimpleSSLUtil` (keytool-based, works on all Java versions)
- Falls back to `SSLUtil` (internal API, Java 8-11)
- Auto-generates keystore file (`barcode-server.keystore`)
- Self-signed certificate (browser warning is normal)

**Network Configuration**:
- Prefers non-APIPA IP addresses (avoids 169.254.x.x)
- Falls back to localhost if no network found
- Automatic port selection if default port is busy

**Relationships**:
- Used by: `CashierController` (started on cashier login)
- Uses: `SimpleSSLUtil`, `SSLUtil` (for HTTPS)
- Serves: Mobile web browser (scanner page)

---

#### `SimpleSSLUtil.java`
**Purpose**: Simplified SSL utility that works on all Java versions using keytool command

**Responsibilities**:
- Creates SSL keystore using keytool command-line tool
- Loads existing keystore if available
- Generates self-signed certificate
- Works on all Java versions (8+)

**Key Methods**:
- `createSSLContext()`: Creates SSL context with keystore
- `loadOrCreateKeystore()`: Loads or creates keystore file
- `createKeystoreWithKeytool()`: Executes keytool to generate keystore
- `getLocalNetworkIP()`: Detects current network IP address

**Keystore File**:
- **Name**: `barcode-server.keystore`
- **Password**: "changeit"
- **Location**: Project root directory
- **Format**: JKS (Java KeyStore)

**Certificate Details**:
- **CN**: Barcode Scanner
- **SAN Entries**: localhost, 127.0.0.1, [Your Network IP]
- **Validity**: 365 days
- **Algorithm**: RSA 2048-bit

**Relationships**:
- Used by: `BarcodeReceiver` (for HTTPS support)
- Creates: `barcode-server.keystore` file

---

#### `XMLLoader.java`
**Purpose**: Parses XML layout files and creates JavaFX Scene objects

**Responsibilities**:
- Parses XML files using DOM parser
- Creates JavaFX UI components from XML tags
- Maps `fx:id` attributes to component registry
- Handles nested layouts and components

**Key Methods**:
- `loadScene(String, Stage)`: Loads XML and creates Scene
- `getComponent(String)`: Gets component by fx:id
- `getComponents()`: Returns all loaded components
- `parseElement(Element)`: Recursively parses XML elements

**Supported XML Tags**:
- Scene, VBox, HBox, BorderPane, TabPane, Tab
- Label, Button, TextField, PasswordField, ComboBox
- TableView, TableColumn
- Region (spacer)

**Component Registry**:
- All components with `fx:id` are stored in `nodeMap`
- Controllers retrieve components by fx:id
- Enables loose coupling between XML and Java code

**Relationships**:
- Used by: `Main`, `LoginController`
- Parses: `login.xml`, `admin_dashboard.xml`, `cashier_dashboard.xml`

---

## Mobile Barcode Scanning Process - Detailed Flow

This section explains in detail how the mobile barcode scanning system works, from initialization to product addition in the cart.

### System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    MOBILE BARCODE SCANNING SYSTEM                │
└─────────────────────────────────────────────────────────────────┘

┌─────────────┐                    ┌─────────────┐
│   Phone     │                    │  Computer   │
│  (Browser)  │                    │ POS System  │
│             │                    │             │
│  Camera     │                    │  Java App   │
│  JavaScript │                    │  HTTPS      │
│  Scanner    │                    │  Server     │
└──────┬──────┘                    └──────┬──────┘
       │                                   │
       │         Wi-Fi Network             │
       │         (Same Network)           │
       │                                   │
       └─────────── HTTPS ────────────────┘
              Port: 8088 (default)
```

### Complete Process Flow

#### Phase 1: Server Initialization

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 1: Cashier Login                                        │
└─────────────────────────────────────────────────────────────┘
CashierController Constructor
    │
    ├─► startBarcodeReceiver()
    │
    └─► BarcodeReceiver.start()
            │
            ├─► Check if port 8088 is available
            │   │
            │   ├─► YES: Use port 8088
            │   └─► NO: Try 8089, 8090, etc. (up to 10 attempts)
            │
            ├─► Try HTTPS Setup:
            │   │
            │   ├─► SimpleSSLUtil.createSSLContext()
            │   │   │
            │   │   ├─► Check if barcode-server.keystore exists
            │   │   │   │
            │   │   │   ├─► EXISTS: Load existing keystore
            │   │   │   └─► NOT EXISTS: Create new keystore
            │   │   │       │
            │   │   │       ├─► getLocalNetworkIP()
            │   │   │       │   │
            │   │   │       │   ├─► Scan network interfaces
            │   │   │       │   ├─► Filter loopback (127.0.0.1)
            │   │   │       │   ├─► Filter APIPA (169.254.x.x)
            │   │   │       │   └─► Return first valid IP
            │   │   │       │
            │   │   │       └─► createKeystoreWithKeytool()
            │   │   │           │
            │   │   │           ├─► Build SAN extension:
            │   │   │           │   "SAN=DNS:localhost,IP:127.0.0.1,IP:[NetworkIP]"
            │   │   │           │
            │   │   │           └─► Execute keytool command
            │   │   │               Generate RSA 2048-bit certificate
            │   │   │               Save to barcode-server.keystore
            │   │   │
            │   │   └─► Load keystore and create SSLContext
            │   │
            │   ├─► Create HttpsServer on detected port
            │   ├─► Configure SSL context
            │   ├─► Create HTTP contexts:
            │   │   ├─► /barcode → BarcodeHandler (POST endpoint)
            │   │   ├─► /scanner → ScannerPageHandler (GET endpoint)
            │   │   └─► / → RootHandler (redirects to /scanner)
            │   │
            │   └─► Start server
            │
            └─► If HTTPS fails → Fallback to HTTP
                │
                └─► Create HttpServer (same contexts)
                    Note: Camera may not work with HTTP

┌─────────────────────────────────────────────────────────────┐
│ STEP 2: Console Output                                       │
└─────────────────────────────────────────────────────────────┘
Console displays:
    ════════════════════════════════════════
    Barcode Receiver Server Started (HTTPS)!
    ════════════════════════════════════════
    Port: 8088
    Open on your phone:
    https://192.168.1.100:8088/scanner
    ════════════════════════════════════════
```

#### Phase 2: Phone Connection

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 3: User Opens Browser on Phone                         │
└─────────────────────────────────────────────────────────────┘
User Action:
    Open Chrome/Safari browser
    Type: https://192.168.1.100:8088/scanner
    Press Enter/Go

┌─────────────────────────────────────────────────────────────┐
│ STEP 4: Network Connection                                  │
└─────────────────────────────────────────────────────────────┘
Phone Browser
    │
    ├─► DNS Resolution (if needed)
    │   └─► Already IP address, skip DNS
    │
    ├─► TCP Connection
    │   │
    │   ├─► [SYN] ───────────────────► Computer:8088
    │   ├─► [SYN-ACK] ◄────────────── Computer:8088
    │   └─► [ACK] ───────────────────► Computer:8088
    │
    │   TCP Connection Established ✓
    │
    └─► TLS Handshake (HTTPS)
        │
        ├─► ClientHello ────────────► Computer
        │   (Supported ciphers, TLS version)
        │
        ├─► ServerHello ────────────◄ Computer
        │   Certificate ────────────◄ Computer
        │   (SSL certificate with SAN)
        │
        ├─► Certificate Validation
        │   │
        │   ├─► ✓ Algorithm: RSA 2048-bit
        │   ├─► ✓ Not expired (< 365 days)
        │   ├─► ✓ Hostname match:
        │   │     Connecting to: 192.168.1.100
        │   │     SAN includes: IP:192.168.1.100 ✓
        │   └─► ⚠ Issuer not trusted (self-signed)
        │
        ├─► Browser shows security warning
        │   "Your connection is not private"
        │
        ├─► User clicks "Advanced" → "Proceed"
        │
        └─► TLS Handshake Complete ✓
            Encrypted channel established

┌─────────────────────────────────────────────────────────────┐
│ STEP 5: HTTP GET Request                                     │
└─────────────────────────────────────────────────────────────┘
Phone Browser
    │
    └─► GET /scanner HTTP/1.1
        Host: 192.168.1.100:8088
        │
        └─► Computer: BarcodeReceiver
            │
            └─► ScannerPageHandler.handle()
                │
                └─► Sends HTML page with:
                    ├─► Camera interface
                    ├─► html5-qrcode library (CDN)
                    ├─► JavaScript scanner code
                    ├─► Manual entry field
                    └─► Status messages
```

#### Phase 3: Scanner Page Loading

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 6: Scanner Page Renders                                 │
└─────────────────────────────────────────────────────────────┘
Phone Browser displays:
    ┌─────────────────────────────┐
    │   📱 Barcode Scanner        │
    │                             │
    │   Status: Ready             │
    │                             │
    │   [Camera Preview Area]     │
    │                             │
    │   [Start Camera] [Stop]     │
    │                             │
    │   Manual Entry:             │
    │   [____________] [Send]     │
    └─────────────────────────────┘

User Action:
    Tap "Start Camera" button

┌─────────────────────────────────────────────────────────────┐
│ STEP 7: Camera Permission                                    │
└─────────────────────────────────────────────────────────────┘
Browser
    │
    └─► Request camera permission
        │
        ├─► User grants permission
        │   │
        │   └─► Camera starts
        │       │
        │       ├─► Video stream begins
        │       ├─► html5-qrcode library initialized
        │       ├─► Scanner starts (30 FPS)
        │       └─► Ready beep sound
        │
        └─► User denies permission
            │
            └─► Manual entry mode only
```

#### Phase 4: Barcode Scanning

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 8: User Scans Barcode                                   │
└─────────────────────────────────────────────────────────────┘
Physical Barcode
    │
    └─► Phone Camera captures image
        │
        └─► html5-qrcode library processes frame
            │
            ├─► Pattern recognition
            ├─► Decode barcode
            │
            └─► Barcode detected: "2000000123456"
                │
                └─► JavaScript callback triggered
                    │
                    ├─► Check cooldown (5 seconds)
                    │   │
                    │   ├─► In cooldown: Show message, ignore
                    │   └─► Not in cooldown: Continue
                    │
                    └─► Send barcode to server

┌─────────────────────────────────────────────────────────────┐
│ STEP 9: Send Barcode to Server                               │
└─────────────────────────────────────────────────────────────┘
JavaScript (Phone Browser)
    │
    └─► fetch("/barcode", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                barcode: "2000000123456"
            })
        })
        │
        └─► HTTPS POST Request
            │
            └─► Computer: BarcodeReceiver
                │
                └─► BarcodeHandler.handle(HttpExchange)
                    │
                    ├─► Parse JSON body
                    │   Extract barcode: "2000000123456"
                    │
                    ├─► Set CORS headers
                    │   Access-Control-Allow-Origin: *
                    │
                    ├─► Execute callback on JavaFX thread
                    │   Platform.runLater(() -> {
                    │       barcodeCallback.accept("2000000123456");
                    │   })
                    │
                    └─► Send HTTP 200 response
                        {"status": "success"}
```

#### Phase 5: Process Barcode in POS

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 10: CashierController Processes Barcode                 │
└─────────────────────────────────────────────────────────────┘
CashierController.handleReceivedBarcode("2000000123456")
    │
    ├─► Fast in-memory lookup
    │   findProductInList("2000000123456")
    │   │
    │   ├─► Found in memory: Use cached product
    │   └─► Not found: Continue to database lookup
    │
    ├─► Database lookup
    │   findProductByBarcodeOrId("2000000123456")
    │   │
    │   ├─► Query: SELECT * FROM products 
    │   │         WHERE barcode = ? OR id = ?
    │   │
    │   ├─► Product found: Return Product object
    │   └─► Product not found: Show error message
    │
    ├─► Validation
    │   │
    │   ├─► Check product.isAvailable() == true
    │   ├─► Check product.getStock() > 0
    │   └─► All valid: Continue
    │
    └─► Add to cart
        addProductToCartDirectly(product, quantity=1)
        │
        ├─► Create cart item
        ├─► Add to cartList (ObservableList)
        ├─► Update cart table (JavaFX UI)
        ├─► updateTotals()
        │   │
        │   ├─► Calculate subtotal
        │   ├─► Apply discount
        │   └─► Calculate final total
        │
        └─► Show success notification
            "Product added to cart"
```

#### Phase 6: User Feedback

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 11: Success Confirmation                                │
└─────────────────────────────────────────────────────────────┘
Phone Browser
    │
    └─► Receive HTTP 200 response
        │
        ├─► Display success message
        │   "✓ Sent: 2000000123456"
        │
        ├─► Vibrate phone (double pulse)
        │
        ├─► Play success beep sound
        │
        └─► Highlight barcode in status

Computer POS Application
    │
    └─► Cart updated
        │
        ├─► Product appears in cart table
        ├─► Totals updated
        └─► Ready for next scan or checkout
```

### Complete Data Flow Diagram

```
┌──────────────────────────────────────────────────────────────┐
│              MOBILE BARCODE SCANNING DATA FLOW               │
└──────────────────────────────────────────────────────────────┘

[Physical Barcode]
        │
        ▼
[Phone Camera] ──► Video Stream (30 FPS)
        │
        ▼
[html5-qrcode Library] ──► Pattern Recognition
        │
        ▼
[Decoded String] ──► "2000000123456"
        │
        ▼
[JavaScript Application]
        │
        ├─► Validation
        ├─► Cooldown Check
        └─► Format JSON
        │
        ▼
[HTTPS POST Request]
    POST /barcode
    Content-Type: application/json
    Body: {"barcode": "2000000123456"}
        │
        ▼
[BarcodeReceiver Server]
        │
        ├─► BarcodeHandler.handle()
        ├─► Parse JSON
        ├─► Extract barcode
        └─► Execute Callback
        │
        ▼
[CashierController]
    handleReceivedBarcode("2000000123456")
        │
        ├─► findProductInList() [Fast lookup]
        │   │
        │   └─► Not found? Continue ▼
        │
        ├─► findProductByBarcodeOrId() [Database]
        │   │
        │   ├─► DBConnection.getConnection()
        │   ├─► SQL Query: SELECT * FROM products 
        │   │            WHERE barcode = ? OR id = ?
        │   └─► Return Product object
        │
        ├─► Validation
        │   ├─► isAvailable() == true?
        │   └─► getStock() > 0?
        │
        └─► addProductToCartDirectly()
            │
            ├─► Create cart item
            ├─► Add to ObservableList
            ├─► Update JavaFX TableView
            └─► updateTotals()
                │
                ├─► Calculate subtotal
                ├─► Apply discount
                └─► Update UI labels
```

### Sequence Diagram

```
┌──────────┐    ┌──────────────┐    ┌─────────────┐    ┌──────────────┐
│  Phone   │    │ BarcodeRecv │    │ CashierCtrl │    │   Database   │
└────┬─────┘    └──────┬───────┘    └──────┬──────┘    └──────┬───────┘
     │                 │                   │                   │
     │ 1. GET /scanner │                   │                   │
     │────────────────►│                   │                   │
     │                 │                   │                   │
     │ 2. HTML Page    │                   │                   │
     │◄────────────────│                   │                   │
     │                 │                   │                   │
     │ 3. Start Camera │                   │                   │
     │                 │                   │                   │
     │ 4. Scan Barcode │                   │                   │
     │                 │                   │                   │
     │ 5. POST /barcode│                   │                   │
     │    {"barcode":  │                   │                   │
     │     "200...6"}  │                   │                   │
     │────────────────►│                   │                   │
     │                 │                   │                   │
     │                 │ 6. Callback      │                   │
     │                 │──────────────────►                   │
     │                 │                   │                   │
     │                 │                   │ 7. Query Product  │
     │                 │                   │───────────────────►│
     │                 │                   │                   │
     │                 │                   │ 8. Product Data   │
     │                 │                   │◄──────────────────│
     │                 │                   │                   │
     │                 │                   │ 9. Add to Cart    │
     │                 │                   │    Update UI      │
     │                 │                   │                   │
     │ 10. HTTP 200    │                   │                   │
     │     {"status":  │                   │                   │
     │      "success"} │                   │                   │
     │◄────────────────│                   │                   │
     │                 │                   │                   │
     │ 11. Show Success│                   │                   │
     │     Vibrate     │                   │                   │
     │     Beep        │                   │                   │
```

### Key Components Interaction

```
┌─────────────────────────────────────────────────────────────┐
│ Component Responsibilities                                   │
└─────────────────────────────────────────────────────────────┘

BarcodeReceiver:
  ├─► HTTP/HTTPS Server
  ├─► SSL Certificate Management
  ├─► Network IP Detection
  ├─► Port Management
  └─► Request Routing

ScannerPageHandler:
  ├─► Serves HTML Page
  ├─► Includes JavaScript Libraries
  └─► Provides Scanner Interface

BarcodeHandler:
  ├─► Receives POST Requests
  ├─► Parses JSON
  ├─► Executes Callback
  └─► Sends Response

CashierController:
  ├─► Starts/Stops BarcodeReceiver
  ├─► Handles Received Barcodes
  ├─► Product Lookup
  ├─► Cart Management
  └─► UI Updates

DBConnection:
  ├─► Database Queries
  ├─► Connection Pooling
  └─► Resource Management
```

### Error Handling Flow

```
┌─────────────────────────────────────────────────────────────┐
│ Error Scenarios and Handling                                 │
└─────────────────────────────────────────────────────────────┘

1. HTTPS Setup Fails
   └─► Fallback to HTTP
       └─► Camera may not work, but manual entry available

2. Port Already in Use
   └─► Try next port (8089, 8090, etc.)
       └─► Up to 10 attempts

3. No Network IP Found
   └─► Use localhost
       └─► Only works on same computer

4. Barcode Not Found in Database
   └─► Show error message on phone
       └─► User can try again or use manual entry

5. Product Out of Stock
   └─► Show error message
       └─► Prevent adding to cart

6. Camera Permission Denied
   └─► Disable camera button
       └─► Manual entry still available

7. Network Connection Lost
   └─► Show error on phone
       └─► User can reconnect
```

---

## Production Deployment

### Pre-Deployment Checklist

- [ ] Change default admin password
- [ ] Create cashier accounts for all staff
- [ ] Add all products with correct prices
- [ ] Test barcode scanner on production network
- [ ] Configure firewall rules
- [ ] Set static IP (recommended)
- [ ] Setup database backups
- [ ] Test all features end-to-end
- [ ] Train staff on system usage

### Database Backups

Regular backups are essential:

```bash
# Backup command
mysqldump -u root -p pos_db > backup_YYYY-MM-DD.sql

# Restore command
mysql -u root -p pos_db < backup_YYYY-MM-DD.sql
```

### Static IP Configuration

For production, configure a static IP on the computer:

**Windows:**
1. Settings → Network & Internet → Wi-Fi/Ethernet
2. Click your connection → Edit IP settings
3. Change to "Manual"
4. Enter: IP, Subnet mask, Gateway, DNS

This ensures the scanner URL remains consistent.

---

## Support and Maintenance

### Logs

Check console output for:
- Server startup messages
- Database connection status
- Scanner URL and port
- Error messages

### Common Maintenance Tasks

1. **Regular Backups**: Daily database backups
2. **Stock Updates**: Monitor and update inventory
3. **User Management**: Add/remove users as needed
4. **Certificate Renewal**: Delete keystore if IP changes
5. **Software Updates**: Keep Java and MySQL updated

### Getting Help

1. Check console output for error messages
2. Verify database connection
3. Check network connectivity
4. Review firewall settings
5. Test with manual entry mode

---

## Version Information

**Current Version**: 1.0  
**Last Updated**: December 2024  
**Java Version**: 17+  
**JavaFX Version**: 20.0.2  
**MySQL Version**: 8.0+

---

## License

This project is provided as-is for educational and commercial use.

---

**End of Documentation**

