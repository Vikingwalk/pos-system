module CoffeeShopPOS {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // <-- required for Scene
    requires java.sql;
    requires mysql.connector.j;
    requires jdk.httpserver;

    opens models to javafx.base;
    opens controllers to javafx.fxml;
    opens main to javafx.fxml;
    opens utils to javafx.fxml;

    exports main;
    exports controllers;
    exports models;
    exports utils;
}
