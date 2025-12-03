package main;

import controllers.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.XMLLoader;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        // Set application icon
        setApplicationIcon(stage);
        // Set stage style for better control (optional - can remove if issues)
        // primaryStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        // Set full screen mode
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        showLoginScreen();
    }
    
    /**
     * Set the application icon for the stage (appears in taskbar/window title)
     */
    private void setApplicationIcon(Stage stage) {
        try {
            // Try to load icon from resources
            java.io.InputStream iconStream = getClass().getResourceAsStream("/icon.png");
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                stage.getIcons().add(icon);
                System.out.println("Application icon loaded successfully");
            } else {
                // Try alternative locations
                try {
                    Image icon = new Image("file:src/main/resources/icon.png");
                    stage.getIcons().add(icon);
                    System.out.println("Application icon loaded from file path");
                } catch (Exception e2) {
                    System.out.println("Icon not found. Please add icon.png to src/main/resources/");
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load application icon: " + e.getMessage());
        }
    }
    
    /**
     * Static method to set application icon on any stage
     * Can be called from other controllers
     */
    public static void setIconOnStage(Stage stage) {
        try {
            // Try to load icon from resources
            java.io.InputStream iconStream = Main.class.getResourceAsStream("/icon.png");
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                stage.getIcons().add(icon);
            } else {
                // Try alternative locations
                try {
                    Image icon = new Image("file:src/main/resources/icon.png");
                    stage.getIcons().add(icon);
                } catch (Exception e2) {
                    // Icon not found, continue without it
                }
            }
        } catch (Exception e) {
            // Icon not found, continue without it
        }
    }

    /**
     * Show login screen - can be called from anywhere to return to login
     */
    public static void showLoginScreen() {
        try {
            System.out.println("Loading login screen...");
            Scene loginScene = XMLLoader.loadScene("src/xml/login.xml", primaryStage);
            // Apply dark mode CSS
            try {
                loginScene.getStylesheets().add(Main.class.getResource("/application.css").toExternalForm());
            } catch (Exception e) {
                // CSS not found, continue without it
            }
            primaryStage.setTitle("Mondalak Coffee - POS");
            primaryStage.setScene(loginScene);
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            primaryStage.show();

            // Get components from XMLLoader registry
            java.util.Map<String, javafx.scene.Node> components = XMLLoader.getComponents();
            
            TextField usernameField = (TextField) components.get("usernameField");
            PasswordField passwordField = (PasswordField) components.get("passwordField");
            Label messageLabel = (Label) components.get("messageLabel");
            Button loginButton = (Button) components.get("loginButton");

            System.out.println("Components found:");
            System.out.println("  usernameField: " + (usernameField != null ? "FOUND" : "NULL"));
            System.out.println("  passwordField: " + (passwordField != null ? "FOUND" : "NULL"));
            System.out.println("  messageLabel: " + (messageLabel != null ? "FOUND" : "NULL"));
            System.out.println("  loginButton: " + (loginButton != null ? "FOUND" : "NULL"));

            if (usernameField != null && passwordField != null && 
                messageLabel != null && loginButton != null) {
                new LoginController(primaryStage, usernameField, passwordField, messageLabel, loginButton);
                System.out.println("LoginController initialized successfully!");
            } else {
                System.err.println("Failed to initialize LoginController - missing components");
                // Fallback: try scene lookup
                initializeLoginWithSceneLookup(loginScene);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load login screen: " + e.getMessage());
            showErrorScreen("Failed to load application: " + e.getMessage());
        }
    }

    /**
     * Fallback method using scene lookup
     */
    private static void initializeLoginWithSceneLookup(Scene loginScene) {
        try {
            TextField usernameField = (TextField) loginScene.lookup("#usernameField");
            PasswordField passwordField = (PasswordField) loginScene.lookup("#passwordField");
            Label messageLabel = (Label) loginScene.lookup("#messageLabel");
            Button loginButton = (Button) loginScene.lookup("#loginButton");

            System.out.println("Fallback lookup results:");
            System.out.println("  usernameField: " + (usernameField != null ? "FOUND" : "NULL"));
            System.out.println("  passwordField: " + (passwordField != null ? "FOUND" : "NULL"));
            System.out.println("  messageLabel: " + (messageLabel != null ? "FOUND" : "NULL"));
            System.out.println("  loginButton: " + (loginButton != null ? "FOUND" : "NULL"));

            if (usernameField != null && passwordField != null && 
                messageLabel != null && loginButton != null) {
                new LoginController(primaryStage, usernameField, passwordField, messageLabel, loginButton);
                System.out.println("LoginController initialized via fallback!");
            }
        } catch (Exception e) {
            System.err.println("Fallback initialization failed: " + e.getMessage());
        }
    }

    /**
     * Show error screen as fallback
     */
    private static void showErrorScreen(String message) {
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 0; -fx-alignment: center; -fx-background-color: #000000;");
        
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14;");
        
        Button retryButton = new Button("Retry Login");
        retryButton.setOnAction(e -> showLoginScreen());
        
        root.getChildren().addAll(errorLabel, retryButton);
        Scene errorScene = new Scene(root, 400, 200);
        primaryStage.setScene(errorScene);
        primaryStage.show();
    }

    /**
     * Show login window - called from other controllers when logging out
     */
    public static void showLoginWindow() {
        showLoginScreen();
    }

    public static void main(String[] args) {
        System.out.println("Starting Coffee Shop POS Application...");
        launch(args);
    }
}