# Coffee Shop POS System - Complete Project Documentation

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Architecture Overview](#architecture-overview)
5. [File Documentation](#file-documentation)
6. [File Relationships & Data Flow](#file-relationships--data-flow)
7. [Database Schema](#database-schema)
8. [Key Features](#key-features)
9. [Setup & Configuration](#setup--configuration)

---

## 🎯 Project Overview

**Coffee Shop POS System** is a comprehensive Point of Sale application built with JavaFX for managing coffee shop operations. The system supports user authentication, product management, sales processing, inventory tracking, and barcode scanning via mobile devices.

### Core Capabilities
- **User Management**: Admin and Cashier roles with secure authentication
- **Product Management**: Full CRUD operations with barcode support
- **Sales Processing**: Transaction handling with cart management and discounts
- **Inventory Tracking**: Real-time stock management with automatic updates
- **Barcode Scanning**: Phone-based barcode scanner with HTTPS support
- **Sales Reporting**: Comprehensive sales reports with filtering
- **Receipt Printing**: Transaction receipt generation

---

## 🛠 Technology Stack

- **Language**: Java 17/21
- **UI Framework**: JavaFX 20.0.2
- **Database**: MySQL 8.0+
- **JDBC Driver**: mysql-connector-j
- **Module System**: Java Platform Module System (JPMS)
- **Build Tool**: Eclipse IDE
- **Network**: HTTPS/HTTP Server (com.sun.net.httpserver)
- **Security**: SHA-256 password hashing, SSL/TLS for HTTPS

---

## 📁 Project Structure

```
CoffeeShopPOS/
├── src/
│   ├── main/
│   │   ├── Main.java              # Application entry point
│   │   └── application.css        # Global styles
│   │
│   ├── models/                     # Data model classes
│   │   ├── Product.java           # Product entity with barcode
│   │   ├── User.java              # User entity (admin/cashier)
│   │   ├── Sale.java              # Sales transaction entity
│   │   ├── SaleItem.java          # Individual sale item
│   │   ├── CartItem.java          # Shopping cart item
│   │   └── SalesReport.java      # Sales report data
│   │
│   ├── controllers/                # Business logic controllers
│   │   ├── LoginController.java   # Authentication & navigation
│   │   ├── AdminController.java   # Admin dashboard logic
│   │   └── CashierController.java # Cashier dashboard logic
│   │
│   ├── utils/                      # Utility classes
│   │   ├── DBConnection.java      # Database connection pool
│   │   ├── SessionManager.java     # User session management
│   │   ├── BarcodeGenerator.java   # Barcode generation & validation
│   │   ├── BarcodeReceiver.java    # HTTP/HTTPS server for phone scanner
│   │   ├── SimpleSSLUtil.java      # SSL certificate generation (keytool)
│   │   ├── SSLUtil.java            # SSL certificate generation (internal API)
│   │   ├── ReportGenerator.java    # Sales report generation
│   │   └── XMLLoader.java          # XML UI layout parser
│   │
│   ├── xml/                        # UI layout definitions
│   │   ├── login.xml               # Login screen layout
│   │   ├── admin_dashboard.xml     # Admin dashboard layout
│   │   └── cashier_dashboard.xml   # Cashier dashboard layout
│   │
│   ├── docs/                       # Documentation
│   │   └── database_setup.sql      # Database schema script
│   │
│   └── module-info.java            # Java module descriptor
│
└── barcode-server.keystore         # SSL keystore (auto-generated)
```

---

## 🏗 Architecture Overview

### Application Flow

```
┌─────────────┐
│   Main.java │  ← Application Entry Point
└──────┬──────┘
       │
       ▼
┌──────────────────┐
│ LoginController  │  ← Authentication
└──────┬───────────┘
       │
       ├─── Admin ────► AdminController ──► Admin Dashboard
       │
       └─── Cashier ──► CashierController ──► Cashier Dashboard
                              │
                              ▼
                    ┌──────────────────┐
                    │ BarcodeReceiver  │  ← Phone Scanner Server
                    └──────────────────┘
```

### Key Design Patterns

1. **MVC (Model-View-Controller)**
   - **Models**: Data entities (Product, User, Sale, etc.)
   - **Views**: XML layout files
   - **Controllers**: Business logic (LoginController, AdminController, CashierController)

2. **Connection Pool Pattern**
   - `DBConnection` manages a pool of database connections
   - Reuses connections for better performance

3. **Session Management**
   - `SessionManager` maintains user session state
   - Static singleton pattern for global access

4. **Callback Pattern**
   - `BarcodeReceiver` uses Consumer callback for barcode processing
   - Decouples server from controller logic

---

## 📄 File Documentation

### Package: `main`

#### `Main.java`
**Purpose**: JavaFX Application entry point and window manager

**Key Responsibilities**:
- Extends `javafx.application.Application`
- Initializes the primary stage (main window)
- Loads login screen on startup
- Provides static method `showLoginScreen()` for returning to login
- Handles error scenarios with fallback UI
- Coordinates between XMLLoader and Controllers

**Key Methods**:
- `start(Stage)` - Application entry point, called by JavaFX
- `showLoginScreen()` - Loads and displays login screen
- `initializeLoginWithSceneLookup(Scene)` - Fallback initialization method
- `showErrorScreen(String)` - Displays error fallback UI
- `main(String[])` - Launches JavaFX application

**Relationships**:
- **Uses**: `XMLLoader`, `LoginController`
- **Called by**: `AdminController`, `CashierController` (on logout via LoginController)

**How It Works**:
1. JavaFX calls `start()` when application launches
2. `start()` calls `showLoginScreen()` to load login UI
3. `XMLLoader.loadScene()` parses XML and creates JavaFX Scene
4. Components are retrieved from XMLLoader's component registry
5. `LoginController` is initialized with UI components
6. User interacts with login screen to authenticate

---

### Package: `models`

#### `Product.java`
**Purpose**: Represents a product/item in the inventory with barcode support

**Fields**:
- `id` (int) - Unique product identifier (auto-generated by database)
- `name` (String) - Product name
- `description` (String) - Product description
- `price` (double) - Product price
- `stock` (int) - Current stock quantity
- `category` (String) - Product category
- `isAvailable` (boolean) - Availability status
- `barcode` (String) - Product barcode (EAN-13 format or null)

**Constructors**:
1. `Product(id, name, description, price, stock, category, isAvailable)` - Without barcode
2. `Product(id, name, description, price, stock, category, isAvailable, barcode)` - With barcode

**Key Methods**:
- `isValid()` - Validates product data (name not empty, price/stock >= 0)
- `getBarcode()` - Returns barcode string (may be null)
- `setBarcode(String)` - Sets barcode value

**Relationships**:
- **Used by**: `AdminController`, `CashierController`, `CartItem`
- **Related to**: `BarcodeGenerator` (generates barcodes for products)
- **Stored in**: `products` database table

**How It Works**:
- Product objects are created from database queries
- Barcode is optional (can be null for legacy products)
- Used throughout the application for product display and cart management

---

#### `User.java`
**Purpose**: Represents a system user (admin or cashier)

**Fields**:
- `id` (int) - Unique user identifier
- `username` (String) - Login username
- `passwordHash` (String) - Hashed password (SHA-256)
- `role` (String) - User role ("admin" or "cashier")
- `isActive` (boolean) - Account active status

**Key Methods**:
- `hashPassword(String)` - Static method to hash passwords using SHA-256
- `verifyPassword(String)` - Verifies password against hash
- `toString()` - Returns formatted string representation

**Security**:
- Passwords are hashed using SHA-256 algorithm
- Never stored in plain text
- Hash comparison for authentication

**Relationships**:
- **Used by**: `LoginController`, `AdminController`, `SessionManager`
- **Stored in**: `users` database table

**How It Works**:
1. When creating a user, password is hashed using `hashPassword()`
2. Hash is stored in database
3. During login, input password is hashed and compared with stored hash
4. User object is created from database query results

---

#### `Sale.java`
**Purpose**: Represents a completed sales transaction

**Fields**:
- `id` (int) - Unique sale identifier
- `cashierId` (int) - ID of cashier who processed the sale
- `totalAmount` (double) - Total before discount
- `discountAmount` (double) - Discount applied
- `finalAmount` (double) - Final amount after discount
- `paymentMethod` (String) - Payment method (cash/card/mobile)
- `createdAt` (LocalDateTime) - Transaction timestamp
- `items` (List<SaleItem>) - List of items in the sale

**Relationships**:
- **Contains**: `SaleItem` objects
- **Used by**: `CashierController`, `AdminController` (for reports)
- **Stored in**: `sales` database table

---

#### `SaleItem.java`
**Purpose**: Represents a single item within a sale transaction

**Fields**:
- `id` (int) - Unique sale item identifier
- `saleId` (int) - Reference to parent sale
- `productId` (int) - Reference to product
- `quantity` (int) - Quantity purchased
- `unitPrice` (double) - Price per unit at time of sale
- `subtotal` (double) - Total for this item (quantity × unitPrice)

**Relationships**:
- **Belongs to**: `Sale`
- **References**: `Product`
- **Stored in**: `sale_items` database table

---

#### `CartItem.java`
**Purpose**: Represents an item in the shopping cart (legacy, currently using Product directly)

**Note**: Currently, the cart uses `Product` objects directly with quantity stored in the `stock` field (repurposed for cart quantity).

---

#### `SalesReport.java`
**Purpose**: Model class for sales report data, combining sale information with cashier and product details

**Fields**:
- Sale information (ID, date, amounts)
- Cashier information (name, ID)
- Product details (name, quantity, price)

**Relationships**:
- **Used by**: `AdminController`, `ReportGenerator`
- **Created from**: Database JOIN queries on `sales`, `sale_items`, `products`, `users` tables

---

### Package: `controllers`

#### `LoginController.java`
**Purpose**: Handles user authentication, role-based access, and navigation to admin/cashier dashboards

**Key Responsibilities**:
- Validates user credentials
- Manages login UI interactions
- Routes users to appropriate dashboard based on role
- Sets up session via `SessionManager`
- Handles logout functionality
- Initializes AdminController and CashierController

**Key Methods**:
- `login()` - Main login handler
- `tryDatabaseLogin(String, String)` - Attempts database authentication
- `handleDemoLogin(String, String)` - Fallback demo login (admin/admin123, cashier/cashier123)
- `navigateToDashboard(String)` - Routes to admin or cashier dashboard
- `initializeAdminController(Scene)` - Sets up admin dashboard
- `initializeCashierController(Scene)` - Sets up cashier dashboard
- `logout()` - Clears session and returns to login

**Authentication Flow**:
1. User enters username and password
2. `tryDatabaseLogin()` queries database for matching user
3. Password is hashed and compared with stored hash
4. If valid, `SessionManager.setCurrentUser()` is called
5. `navigateToDashboard()` loads appropriate dashboard
6. Dashboard controller is initialized

**Relationships**:
- **Uses**: `DBConnection`, `SessionManager`, `XMLLoader`, `User`
- **Creates**: `AdminController`, `CashierController`
- **Called by**: `Main` (initialization)

**How It Works**:
1. `Main` creates `LoginController` with UI components from XML
2. User enters credentials and clicks login button
3. `login()` method is triggered
4. Database query checks username and password hash
5. On success, session is set and user is redirected
6. On failure, error message is displayed

---

#### `AdminController.java`
**Purpose**: Manages users and products, handles sales reporting

**Key Responsibilities**:
- User management (add, delete, refresh users)
- Product management (add, delete, update stock, refresh products)
- Sales report generation and display
- Automatic barcode generation for new products
- Welcome message personalization

**Key Methods**:
- `loadUsers()` - Loads all users from database
- `addUser()` - Creates new user with hashed password
- `deleteUser()` - Removes user from database
- `loadProducts()` - Loads all products from database
- `addProduct()` - Creates new product with auto-generated barcode
- `deleteProduct()` - Removes product from database
- `reduceStock()` - Manually reduces product stock
- `generateSalesReport()` - Creates and displays sales report
- `setupUserTableStructure()` - Configures user table columns
- `setupProductTableStructure()` - Configures product table columns
- `ensureBarcodeColumnExists()` - Adds barcode column if missing

**Barcode Integration**:
- When adding a product, `BarcodeGenerator.generateEAN13()` is called
- Generated barcode is stored in database
- Barcode column is automatically added if missing

**Relationships**:
- **Uses**: `DBConnection`, `BarcodeGenerator`, `Product`, `User`, `SalesReport`, `ReportGenerator`
- **Created by**: `LoginController`
- **UI**: `admin_dashboard.xml`

**How It Works**:
1. `LoginController` creates `AdminController` after admin login
2. Controller initializes tables and loads data
3. User interacts with UI to manage users/products
4. Database operations are performed via `DBConnection`
5. Changes are reflected immediately in tables

---

#### `CashierController.java`
**Purpose**: Handles sales transactions, cart management, product search, checkout, and barcode scanning

**Key Responsibilities**:
- Product browsing and searching
- Cart management (add, update, clear)
- Checkout processing with transaction management
- Receipt printing
- Barcode scanning integration (phone scanner)
- Stock validation
- Discount application
- Welcome message personalization

**Key Methods**:
- `loadProducts()` - Loads products from database
- `searchProducts()` - Searches products by name, ID, or barcode
- `addToCart()` - Adds product to cart with quantity validation
- `addProductToCartDirectly(Product, int)` - Optimized direct cart addition
- `updateTotals()` - Calculates and displays cart totals
- `checkout()` - Processes sale transaction
- `printReceipt()` - Generates and displays receipt
- `clearCart()` - Empties shopping cart
- `findProductInList(String)` - Fast in-memory product lookup
- `findProductByBarcodeOrId(String)` - Database product lookup
- `handleReceivedBarcode(String)` - Processes barcode from phone scanner
- `startBarcodeReceiver()` - Initializes phone scanner server
- `stopBarcodeReceiver()` - Stops scanner server on logout

**Barcode Scanning Flow**:
1. Cashier logs in → `BarcodeReceiver` server starts
2. Phone connects to server URL (HTTPS/HTTP)
3. User scans barcode with phone camera
4. JavaScript sends barcode to server via POST
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
- **Uses**: `DBConnection`, `BarcodeReceiver`, `Product`, `SessionManager`
- **Created by**: `LoginController`
- **UI**: `cashier_dashboard.xml`

**How It Works**:
1. `LoginController` creates `CashierController` after cashier login
2. Controller loads products and starts barcode receiver
3. Cashier can search products or scan barcodes
4. Products are added to cart with quantity validation
5. Checkout processes transaction and updates inventory
6. Receipt is generated and cart is cleared

---

### Package: `utils`

#### `DBConnection.java`
**Purpose**: Manages database connections using a connection pool

**Key Responsibilities**:
- Connection pool management (max 10 connections)
- Connection lifecycle (create, reuse, release)
- Connection validation
- Resource cleanup

**Key Methods**:
- `getConnection()` - Gets connection from pool or creates new one
- `releaseConnection(Connection)` - Returns connection to pool
- `closeResources(Connection, PreparedStatement, ResultSet)` - Safely closes all resources
- `testConnection()` - Tests database connectivity
- `isDatabaseAvailable()` - Checks if database is accessible

**Connection Pool Pattern**:
- Pool initialized with 3 connections on startup
- Connections are reused for better performance
- Invalid connections are replaced automatically
- Pool size limited to 10 connections

**Configuration**:
```java
URL: "jdbc:mysql://localhost:3306/pos_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
USER: "root"
PASSWORD: "Azerty123@2003"
```

**Relationships**:
- **Used by**: All controllers and models that need database access
- **Provides**: Connection management for entire application

**How It Works**:
1. Static initializer creates connection pool on class load
2. `getConnection()` checks pool for available connection
3. If pool is empty, new connection is created
4. Connection is validated before returning
5. `releaseConnection()` returns connection to pool
6. `closeResources()` ensures proper cleanup

---

#### `SessionManager.java`
**Purpose**: Manages user session data (ID, username, role) across the application

**Key Responsibilities**:
- Store current user information
- Provide session data to controllers
- Clear session on logout

**Key Methods**:
- `setCurrentUser(int, String, String)` - Sets current session
- `clearSession()` - Clears session data
- `getCurrentUserId()` - Returns current user ID
- `getCurrentUsername()` - Returns current username
- `getCurrentUserRole()` - Returns current user role
- `isLoggedIn()` - Checks if user is logged in

**Design Pattern**: Singleton (static methods, no instance)

**Relationships**:
- **Used by**: `LoginController`, `AdminController`, `CashierController`
- **Set by**: `LoginController` (on successful login)
- **Cleared by**: `LoginController` (on logout)

**How It Works**:
1. On login, `LoginController` calls `setCurrentUser()`
2. Session data is stored in static variables
3. Controllers access session via getter methods
4. On logout, `clearSession()` resets all values

---

#### `BarcodeGenerator.java`
**Purpose**: Utility class for generating and validating EAN-13 and simple barcodes

**Key Methods**:
- `generateEAN13(int)` - Generates EAN-13 barcode from product ID
- `generateSimple(int)` - Generates simple barcode (PRODXXXXXX format)
- `calculateEAN13CheckDigit(String)` - Calculates EAN-13 check digit
- `validateEAN13(String)` - Validates EAN-13 barcode format
- `extractProductIdFromEAN13(String)` - Extracts product ID from EAN-13 barcode
- `extractProductIdFromSimple(String)` - Extracts product ID from simple barcode

**Barcode Symbology Used: EAN-13**

**Overview of Linear Barcode Symbologies**:

Linear (1D) barcodes are one-dimensional barcodes that encode data in parallel lines of varying widths. Common linear symbologies include:

1. **EAN-13** (European Article Number) - **USED IN THIS PROJECT**
   - **Length**: 13 digits
   - **Structure**: Country code (2-3 digits) + Manufacturer code + Product code + Check digit
   - **Use Case**: Retail products worldwide
   - **Standard**: ISO/IEC 15420
   - **Advantages**: Global standard, widely supported, includes error detection

2. **UPC-A** (Universal Product Code)
   - **Length**: 12 digits
   - **Structure**: Similar to EAN-13 but 12 digits
   - **Use Case**: Primarily North America
   - **Relationship**: EAN-13 is an extension of UPC-A

3. **Code 128**
   - **Length**: Variable
   - **Structure**: Can encode alphanumeric characters
   - **Use Case**: Shipping, packaging, logistics
   - **Advantages**: High density, alphanumeric support

4. **Code 39**
   - **Length**: Variable
   - **Structure**: Alphanumeric encoding
   - **Use Case**: Industrial, automotive
   - **Advantages**: Simple, widely readable

5. **ITF-14** (Interleaved 2 of 5)
   - **Length**: 14 digits
   - **Structure**: Used for cartons/cases
   - **Use Case**: Shipping containers

6. **Code 93**
   - **Length**: Variable
   - **Structure**: More compact than Code 39
   - **Use Case**: Automotive, defense

**Why EAN-13 is Used in This Project**:
- ✅ Industry standard for retail products
- ✅ Widely supported by all barcode scanners
- ✅ Includes check digit for error detection
- ✅ 13 digits provide sufficient capacity
- ✅ Compatible with global retail systems
- ✅ Can embed product ID in the structure

**EAN-13 Format in This Project**:
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

**Example EAN-13 Barcode**:
```
Product ID: 42
Generated: 2000000421235
           │││││││││││││
           ││││││││││││└─ Check digit (5)
           │││││││││││└── Random (123)
           ││││││││││└─── Random
           │││││││││└──── Random
           ││││││││└───── Product ID (000042)
           │││││││└────── Product ID
           ││││││└─────── Product ID
           │││││└──────── Product ID
           ││││└───────── Product ID
           │││└────────── Product ID
           └─┴─┴───────── Prefix (200)
```

**Relationships**:
- **Used by**: `AdminController` (when adding products)
- **Related to**: `Product` (barcode field)
- **Standard**: ISO/IEC 15420 (EAN-13 specification)

**How It Works**:
1. `AdminController.addProduct()` calls `generateEAN13(productId)`
2. Barcode is generated with product ID embedded in positions 3-8
3. Random digits added for uniqueness (positions 9-11)
4. Check digit is calculated using EAN-13 algorithm
5. Complete 13-digit barcode is stored in database with product
6. Barcode can be scanned by any EAN-13 compatible scanner

---

#### `BarcodeReceiver.java`
**Purpose**: HTTP/HTTPS server that receives barcode data from mobile devices and serves the scanner web page

**Key Responsibilities**:
- Creates HTTP/HTTPS server on local network
- Serves scanner web page to mobile devices
- Receives barcode data via POST requests
- Processes barcodes and calls callback function
- Handles CORS for cross-origin requests
- Automatic port detection (tries 8080, 8081, etc.)
- HTTPS with self-signed certificate (falls back to HTTP)

**Key Methods**:
- `start()` - Starts the server (tries HTTPS first, falls back to HTTP)
- `stop()` - Stops the server
- `isRunning()` - Checks if server is running
- `getServerURL()` - Returns server URL (https:// or http://)
- `getLocalIP()` - Gets local network IP address
- `createSSLContext()` - Creates SSL context for HTTPS
- `isPortAvailable(int)` - Checks if port is available

**Inner Classes**:
- `BarcodeHandler` - Handles POST requests to `/barcode`
- `ScannerPageHandler` - Serves HTML/JavaScript scanner page
- `RootHandler` - Redirects `/` to `/scanner`

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
- **Used by**: `CashierController` (started on cashier login)
- **Uses**: `SimpleSSLUtil`, `SSLUtil` (for HTTPS)
- **Serves**: Mobile web browser (scanner page)

**How It Works**:
1. `CashierController` creates `BarcodeReceiver` with callback
2. `start()` tries to create HTTPS server
3. SSL context is created using `SimpleSSLUtil` or `SSLUtil`
4. Server binds to port (tries multiple ports if needed)
5. Three HTTP contexts are created:
   - `/barcode` - POST endpoint for barcode data
   - `/scanner` - GET endpoint for scanner web page
   - `/` - Redirects to `/scanner`
6. Phone connects to server URL
7. Scanner page loads with camera access
8. User scans barcode → JavaScript sends POST to `/barcode`
9. `BarcodeHandler` receives barcode and calls callback
10. `CashierController.handleReceivedBarcode()` processes barcode

---

#### `SimpleSSLUtil.java`
**Purpose**: Simplified SSL utility that works on all Java versions using keytool command

**Key Responsibilities**:
- Creates SSL keystore using keytool command-line tool
- Loads existing keystore if available
- Generates self-signed certificate
- Works on all Java versions (8+)

**Key Methods**:
- `createSSLContext()` - Creates SSL context with keystore
- `loadOrCreateKeystore()` - Loads or creates keystore file
- `createKeystoreWithKeytool()` - Executes keytool to generate keystore

**Keystore File**:
- **Name**: `barcode-server.keystore`
- **Password**: "changeit"
- **Location**: Project root directory
- **Format**: JKS (Java KeyStore)

**How It Works**:
1. Checks if `barcode-server.keystore` exists
2. If exists, loads keystore from file
3. If not, runs keytool command to create new keystore
4. Keytool generates RSA key pair and self-signed certificate
5. Keystore is saved and loaded
6. SSL context is created from keystore

**Relationships**:
- **Used by**: `BarcodeReceiver` (for HTTPS support)
- **Creates**: `barcode-server.keystore` file

---

#### `SSLUtil.java`
**Purpose**: SSL utility using Java internal APIs (fallback for SimpleSSLUtil)

**Key Responsibilities**:
- Creates SSL context using sun.security.x509 internal APIs
- Works on Java 8-11 (where internal APIs are accessible)
- Falls back if internal APIs are not available

**Key Methods**:
- `createSSLContext()` - Creates SSL context
- `generateSelfSignedCertificate(KeyPair)` - Generates X.509 certificate
- `createCertificateUsingInternalAPI(KeyPair)` - Uses reflection to access internal APIs

**Note**: This is a fallback method. `SimpleSSLUtil` is preferred as it works on all Java versions.

**Relationships**:
- **Used by**: `BarcodeReceiver` (as fallback)
- **Fallback for**: `SimpleSSLUtil`

---

#### `ReportGenerator.java`
**Purpose**: Generates sales reports from database queries

**Key Responsibilities**:
- Queries sales data with filters
- Combines sale, product, and user information
- Formats report data for display

**Relationships**:
- **Used by**: `AdminController`
- **Creates**: `SalesReport` objects

---

#### `XMLLoader.java`
**Purpose**: Parses XML layout files and creates JavaFX Scene objects

**Key Responsibilities**:
- Parses XML files using DOM parser
- Creates JavaFX UI components from XML tags
- Maps `fx:id` attributes to component registry
- Handles nested layouts and components

**Key Methods**:
- `loadScene(String, Stage)` - Loads XML and creates Scene
- `getComponent(String)` - Gets component by fx:id
- `getComponents()` - Returns all loaded components
- `parseElement(Element)` - Recursively parses XML elements

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
- **Used by**: `Main`, `LoginController`
- **Parses**: `login.xml`, `admin_dashboard.xml`, `cashier_dashboard.xml`

**How It Works**:
1. `loadScene()` reads XML file
2. DOM parser creates document tree
3. `parseElement()` recursively processes elements
4. JavaFX components are created based on tag names
5. Components with `fx:id` are registered in `nodeMap`
6. Scene is created with root node
7. Controllers retrieve components from registry

---

### Package: `xml`

#### `login.xml`
**Purpose**: Defines the login screen UI layout

**Components**:
- `usernameField` (TextField) - Username input
- `passwordField` (PasswordField) - Password input
- `loginButton` (Button) - Login trigger
- `messageLabel` (Label) - Status/error messages

**Layout**: VBox with centered form

---

#### `admin_dashboard.xml`
**Purpose**: Defines the admin dashboard UI layout

**Components**:
- `welcomeLabel` (Label) - Personalized welcome message
- `logoutButton` (Button) - Logout trigger
- `salesReportButton` (Button) - Sales report generator
- TabPane with two tabs:
  - **User Management Tab**:
    - `userTable` (TableView) - User list
    - `usernameField`, `passwordField` (TextField) - User input
    - `roleComboBox` (ComboBox) - Role selection
    - `addUserButton`, `deleteUserButton`, `refreshUsersButton` (Button)
  - **Product Management Tab**:
    - `productTable` (TableView) - Product list
    - `productNameField`, `productDescriptionField`, etc. (TextField) - Product input
    - `addProductButton`, `deleteProductButton`, `reduceStockButton`, `refreshProductsButton` (Button)

**Layout**: VBox with header HBox and TabPane

---

#### `cashier_dashboard.xml`
**Purpose**: Defines the cashier dashboard UI layout

**Components**:
- `welcomeLabel` (Label) - Personalized welcome message
- `logoutButton` (Button) - Logout trigger
- `productTable` (TableView) - Available products
- `searchField` (TextField) - Product search
- `searchButton` (Button) - Search trigger
- `cartTable` (TableView) - Shopping cart
- `productIdField` (TextField) - Product ID/barcode input
- `quantityField` (TextField) - Quantity input
- `addToCartButton` (Button) - Add to cart
- `discountField` (TextField) - Discount amount
- `totalLabel`, `discountLabel`, `finalTotalLabel` (Label) - Price displays
- `checkoutButton`, `printReceiptButton`, `clearCartButton` (Button)

**Layout**: VBox with header, HBox with product section and cart section

---

### Package: `docs`

#### `database_setup.sql`
**Purpose**: Database schema definition script

**Tables**:
1. **users** - User accounts (admin/cashier)
   - id, username, password_hash, role, created_at, is_active

2. **products** - Product inventory
   - id, name, description, price, stock, category, created_at, is_available, **barcode**

3. **sales** - Sales transactions
   - id, cashier_id, total_amount, discount_amount, final_amount, payment_method, created_at

4. **sale_items** - Individual items in sales
   - id, sale_id, product_id, quantity, unit_price, subtotal

5. **inventory_logs** - Inventory change history
   - id, product_id, change_type, quantity_change, previous_stock, new_stock, created_at

**Relationships**:
- sales.cashier_id → users.id
- sale_items.sale_id → sales.id
- sale_items.product_id → products.id
- inventory_logs.product_id → products.id

---

### Root Level

#### `module-info.java`
**Purpose**: Java Platform Module System (JPMS) module descriptor

**Module Name**: `CoffeeShopPOS`

**Requires**:
- `javafx.controls` - JavaFX UI controls
- `javafx.fxml` - FXML support
- `javafx.graphics` - JavaFX graphics (Scene, Stage)
- `java.sql` - JDBC API
- `mysql.connector.j` - MySQL JDBC driver
- `jdk.httpserver` - HTTP/HTTPS server

**Opens** (for reflection):
- `models` to `javafx.base`
- `controllers` to `javafx.fxml`
- `main` to `javafx.fxml`
- `utils` to `javafx.fxml`

**Exports**:
- `main`, `controllers`, `models`, `utils`

---

## 🔗 File Relationships & Data Flow

### Authentication Flow

```
Main.java
  ↓
LoginController.login()
  ↓
DBConnection.getConnection()
  ↓
Database Query (users table)
  ↓
User.hashPassword() [password verification]
  ↓
SessionManager.setCurrentUser()
  ↓
LoginController.navigateToDashboard()
  ↓
  ├─→ AdminController (if role = "admin")
  └─→ CashierController (if role = "cashier")
```

### Product Management Flow (Admin)

```
AdminController.addProduct()
  ↓
DBConnection.getConnection()
  ↓
INSERT INTO products (with generated ID)
  ↓
BarcodeGenerator.generateEAN13(productId)
  ↓
UPDATE products SET barcode = ?
  ↓
AdminController.loadProducts() [refresh table]
```

### Sales Transaction Flow (Cashier)

```
CashierController.addToCart()
  ↓
findProductInList() [in-memory search]
  ↓ (if not found)
findProductByBarcodeOrId() [database query]
  ↓
Stock validation
  ↓
Cart update (ObservableList)
  ↓
updateTotals()
```

```
CashierController.checkout()
  ↓
DBConnection.getConnection()
  ↓
conn.setAutoCommit(false) [start transaction]
  ↓
INSERT INTO sales
  ↓
For each cart item:
  ├─→ INSERT INTO sale_items
  ├─→ UPDATE products SET stock = stock - quantity
  └─→ INSERT INTO inventory_logs
  ↓
conn.commit() [commit transaction]
  ↓
printReceipt()
  ↓
clearCart()
```

### Barcode Scanning Flow

```
CashierController.startBarcodeReceiver()
  ↓
BarcodeReceiver.start()
  ↓
SimpleSSLUtil.createSSLContext() [HTTPS]
  ↓
HttpsServer.create() [or HttpServer if HTTPS fails]
  ↓
Server starts on port 8080 (or alternative)
  ↓
Phone browser → https://[IP]:8080/scanner
  ↓
ScannerPageHandler serves HTML/JavaScript
  ↓
User scans barcode with camera
  ↓
JavaScript: fetch("/barcode", {method: "POST", body: barcode})
  ↓
BarcodeHandler.handle() receives POST
  ↓
Platform.runLater() [JavaFX thread]
  ↓
CashierController.handleReceivedBarcode(barcode)
  ↓
findProductInList(barcode) [fast lookup]
  ↓ (if not found)
findProductByBarcodeOrId(barcode) [database query]
  ↓
addProductToCartDirectly(product, 1)
  ↓
Cart updated, totals refreshed
```

### Component Initialization Flow

```
Main.start()
  ↓
XMLLoader.loadScene("login.xml")
  ↓
XML parsed → JavaFX components created
  ↓
Components registered in nodeMap (by fx:id)
  ↓
LoginController(components)
  ↓
User logs in
  ↓
XMLLoader.loadScene("admin_dashboard.xml" or "cashier_dashboard.xml")
  ↓
Components registered
  ↓
AdminController(components) or CashierController(components)
```

---

## 🗄 Database Schema

### Table: `users`
```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('admin', 'cashier') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);
```

### Table: `products`
```sql
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock INT NOT NULL CHECK (stock >= 0),
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_available BOOLEAN DEFAULT TRUE,
    barcode VARCHAR(50)  -- EAN-13 format or null
);
```

### Table: `sales`
```sql
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
```

### Table: `sale_items`
```sql
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
```

### Table: `inventory_logs`
```sql
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

---

## ✨ Key Features

### 1. User Authentication
- Secure password hashing (SHA-256)
- Role-based access control (admin/cashier)
- Session management
- Demo login fallback

### 2. Product Management
- Full CRUD operations
- Automatic barcode generation (EAN-13)
- Stock management
- Category organization
- Availability toggle

### 3. Sales Processing
- Shopping cart management
- Quantity validation
- Stock checking
- Discount application
- Transaction processing with rollback support
- Receipt generation

### 4. Barcode Scanning
- Phone-based scanner (HTTPS/HTTP)
- Camera scanning support
- Manual entry fallback
- 5-second cooldown to prevent duplicates
- Success notifications
- Automatic product lookup and cart addition

### 5. Inventory Tracking
- Real-time stock updates
- Inventory change logging
- Stock validation on checkout
- Automatic stock reduction on sale

### 6. Sales Reporting
- Date range filtering
- Cashier filtering
- Product-level details
- Total calculations

### 7. UI Features
- Personalized welcome messages
- Responsive layouts
- Error handling and validation
- User-friendly alerts

---

## ⚙️ Setup & Configuration

### Prerequisites
1. Java 17 or higher
2. MySQL 8.0 or higher
3. JavaFX 20.0.2 (included in project)
4. MySQL JDBC Driver (mysql-connector-j)

### Database Setup
1. Run `src/docs/database_setup.sql` in MySQL
2. Database `pos_db` will be created
3. Tables will be initialized

### Database Configuration
Edit `src/utils/DBConnection.java` if needed:
```java
URL: "jdbc:mysql://localhost:3306/pos_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
USER: "root"
PASSWORD: "Azerty123@2003"  // Change to your MySQL password
```

### Running the Application
1. Ensure MySQL is running
2. Run `Main.java` as JavaFX Application
3. Login screen will appear
4. Use demo credentials or create users in database

### Demo Login Credentials
- **Admin**: username=`admin`, password=`admin123`
- **Cashier**: username=`cashier`, password=`cashier123`

### Phone Scanner Setup
1. Log in as cashier
2. Note the server URL displayed in console/popup
3. Open URL on phone (same Wi-Fi network)
4. Accept security warning (self-signed certificate)
5. Allow camera permissions
6. Scan barcodes or use manual entry

### HTTPS Certificate
- First run creates `barcode-server.keystore` automatically
- Uses keytool command (works on all Java versions)
- Self-signed certificate (browser warning is normal)
- Click "Advanced" → "Proceed" to accept

---

## 🔄 Data Flow Diagrams

### Login to Dashboard
```
User Input → LoginController → Database Query → SessionManager → Dashboard Controller
```

### Product Addition (Admin)
```
Admin Input → AdminController → Database INSERT → BarcodeGenerator → Database UPDATE → Table Refresh
```

### Sale Processing (Cashier)
```
Scan/Select Product → CashierController → Cart Update → Checkout → Transaction → Database Updates → Receipt
```

### Barcode Scanning
```
Phone Camera → JavaScript → POST /barcode → BarcodeReceiver → Callback → CashierController → Cart Update
```

---

## 🎯 Design Decisions

### Why JavaFX?
- Modern UI framework for Java
- Cross-platform support
- Rich component library
- Good performance

### Why Connection Pool?
- Reusing connections improves performance
- Reduces database load
- Better resource management

### Why HTTPS for Scanner?
- Modern browsers require HTTPS for camera access
- Secure data transmission
- Better user experience

### Why EAN-13 Barcode Format?
- Industry standard format
- 13 digits with check digit validation
- Embeddable product ID
- Widely supported by scanners

### Why XML for UI?
- Separation of UI and logic
- Easy to modify layouts
- No recompilation needed for UI changes
- Declarative approach

---

## 🐛 Troubleshooting

### Database Connection Issues
- Check MySQL is running
- Verify credentials in `DBConnection.java`
- Ensure database `pos_db` exists
- Check firewall settings

### HTTPS Certificate Issues
- First run creates keystore automatically
- If fails, check keytool is available
- Fallback to HTTP works (camera may not work)
- Browser warning is normal (self-signed cert)

### Port Already in Use
- Server automatically tries next port (8080, 8081, etc.)
- Check console for actual port number
- Update phone URL if port changed

### Phone Scanner Not Working
- Ensure phone and computer on same Wi-Fi
- Check firewall allows connections
- Try manual entry (always works)
- Check browser console for errors

### Barcode Not Found
- Verify product has barcode in database
- Check barcode format matches
- Try searching by product ID
- Ensure product is available and in stock

---

## 📝 Notes

- All JavaFX import errors in IDE are false positives (code compiles fine)
- Keystore file is auto-generated on first HTTPS attempt
- Welcome messages are personalized with capitalized username
- Cart uses Product objects with quantity in `stock` field (repurposed)
- All database operations use transactions for data integrity
- Connection pool automatically manages connection lifecycle

---

## 📚 Additional Resources

- **JavaFX Documentation**: https://openjfx.io/
- **MySQL Documentation**: https://dev.mysql.com/doc/
- **EAN-13 Barcode Standard**: ISO/IEC 15420
- **HTML5 QR Code Library**: https://github.com/mebjas/html5-qrcode

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Project**: Coffee Shop POS System

