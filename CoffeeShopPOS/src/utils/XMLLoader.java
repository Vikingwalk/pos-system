package utils;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class XMLLoader {
    
    private static Map<String, javafx.scene.Node> nodeMap = new HashMap<>();
    private static Stage currentStage;

    /**
     * Load XML layout and return a Scene with initialized controller
     */
    public static Scene loadScene(String xmlFilePath, Stage stage) {
        currentStage = stage;
        try {
            File xmlFile = new File(xmlFilePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            Element rootElement = doc.getDocumentElement();
            nodeMap.clear();

            // Recursively build UI
            Region rootNode = (Region) parseElement(rootElement);
            double width = rootNode.getPrefWidth();
            double height = rootNode.getPrefHeight();
            
            // Use screen dimensions for full screen if width/height not specified
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            double sceneWidth = width > 0 ? width : bounds.getWidth();
            double sceneHeight = height > 0 ? height : bounds.getHeight();
            
            Scene scene = new Scene(rootNode, sceneWidth, sceneHeight);
            
            // Store the scene in nodeMap for main components
            nodeMap.put("scene", rootNode);
            
            System.out.println("Loaded components: " + nodeMap.keySet());
            
            return scene;
            
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorScene("Failed to load: " + xmlFilePath + " - " + e.getMessage());
        }
    }
    
    /**
     * Overloaded method for backward compatibility
     */
    public static Scene loadScene(String xmlFilePath) {
        return loadScene(xmlFilePath, new Stage());
    }

    /**
     * Get a component by its fx:id
     */
    public static javafx.scene.Node getComponent(String fxId) {
        return nodeMap.get(fxId);
    }

    /**
     * Get all loaded components
     */
    public static Map<String, javafx.scene.Node> getComponents() {
        return new HashMap<>(nodeMap);
    }

    private static Scene createErrorScene(String message) {
        VBox root = new VBox(10);
        root.setPrefSize(400, 200);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14;");
        
        Button retryButton = new Button("Retry");
        retryButton.setOnAction(e -> {
            try {
                Scene loginScene = loadScene("src/xml/login.xml", currentStage);
                currentStage.setScene(loginScene);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        root.getChildren().addAll(errorLabel, retryButton);
        return new Scene(root);
    }

    private static javafx.scene.Node parseElement(Element element) {
        String tag = element.getTagName();
        javafx.scene.Node node = null;

        try {
            switch (tag) {
                case "Scene":
                    double width = 400;
                    double height = 300;
                    if (element.hasAttribute("width")) {
                        width = Double.parseDouble(element.getAttribute("width"));
                    }
                    if (element.hasAttribute("height")) {
                        height = Double.parseDouble(element.getAttribute("height"));
                    }
                    Pane pane = new VBox();
                    pane.setPrefSize(width, height);
                    pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    pane.setStyle("-fx-padding: 0; -fx-background-color: #000000;");
                    NodeList children = element.getChildNodes();
                    for (int i = 0; i < children.getLength(); i++) {
                        Node childNode = children.item(i);
                        if (childNode instanceof Element) {
                            javafx.scene.Node childElement = parseElement((Element) childNode);
                            if (childElement != null) {
                                pane.getChildren().add(childElement);
                            }
                        }
                        // Ignore text nodes (whitespace, newlines)
                    }
                    node = pane;
                    break;

                case "VBox":
                    VBox vbox = new VBox();
                    vbox.setPadding(new javafx.geometry.Insets(0));
                    if (element.hasAttribute("spacing"))
                        vbox.setSpacing(Double.parseDouble(element.getAttribute("spacing")));
                    if (element.hasAttribute("alignment")) {
                        String alignment = element.getAttribute("alignment");
                        String existingStyle = vbox.getStyle();
                        vbox.setStyle((existingStyle != null ? existingStyle : "") + "-fx-alignment: " + alignment + ";");
                    }
                    if (element.hasAttribute("style")) {
                        String existingStyle = vbox.getStyle();
                        vbox.setStyle((existingStyle != null ? existingStyle : "") + element.getAttribute("style"));
                    }
                    if (element.hasAttribute("prefWidth")) {
                        vbox.setPrefWidth(Double.parseDouble(element.getAttribute("prefWidth")));
                    }
                    if (element.hasAttribute("prefHeight")) {
                        vbox.setPrefHeight(Double.parseDouble(element.getAttribute("prefHeight")));
                    }
                    if (element.hasAttribute("maxWidth")) {
                        String maxWidth = element.getAttribute("maxWidth");
                        if ("Infinity".equals(maxWidth)) {
                            vbox.setMaxWidth(Double.MAX_VALUE);
                        } else if (maxWidth.endsWith("%")) {
                            // Handle percentage - calculate based on screen width
                            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
                            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
                            double percentage = Double.parseDouble(maxWidth.replace("%", "")) / 100.0;
                            vbox.setMaxWidth(bounds.getWidth() * percentage);
                            // Also set prefWidth to the same value for better layout
                            vbox.setPrefWidth(bounds.getWidth() * percentage);
                        } else {
                            vbox.setMaxWidth(Double.parseDouble(maxWidth));
                        }
                    }
                    // Handle VBox.vgrow to make VBox expand
                    if (element.hasAttribute("VBox.vgrow")) {
                        String vgrow = element.getAttribute("VBox.vgrow");
                        if ("ALWAYS".equals(vgrow)) {
                            vbox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        }
                    }
                    NodeList vChildren = element.getChildNodes();
                    for (int i = 0; i < vChildren.getLength(); i++) {
                        Node childNode = vChildren.item(i);
                        if (childNode instanceof Element) {
                            javafx.scene.Node childElement = parseElement((Element) childNode);
                            if (childElement != null) {
                                vbox.getChildren().add(childElement);
                                // Handle VBox.vgrow on child elements
                                if (childElement instanceof Region && ((Element) childNode).hasAttribute("VBox.vgrow")) {
                                    String vgrow = ((Element) childNode).getAttribute("VBox.vgrow");
                                    if ("ALWAYS".equals(vgrow)) {
                                        VBox.setVgrow((Region) childElement, javafx.scene.layout.Priority.ALWAYS);
                                    }
                                }
                            }
                        }
                    }
                    node = vbox;
                    break;

                case "HBox":
                    HBox hbox = new HBox();
                    hbox.setPadding(new javafx.geometry.Insets(0));
                    if (element.hasAttribute("spacing"))
                        hbox.setSpacing(Double.parseDouble(element.getAttribute("spacing")));
                    if (element.hasAttribute("style")) {
                        String existingStyle = hbox.getStyle();
                        hbox.setStyle((existingStyle != null ? existingStyle : "") + element.getAttribute("style"));
                    }
                    if (element.hasAttribute("prefWidth")) {
                        hbox.setPrefWidth(Double.parseDouble(element.getAttribute("prefWidth")));
                    }
                    // Handle HBox.hgrow to make HBox expand
                    if (element.hasAttribute("HBox.hgrow")) {
                        String hgrow = element.getAttribute("HBox.hgrow");
                        if ("ALWAYS".equals(hgrow)) {
                            hbox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        }
                    }
                    NodeList hChildren = element.getChildNodes();
                    for (int i = 0; i < hChildren.getLength(); i++) {
                        Node childNode = hChildren.item(i);
                        if (childNode instanceof Element) {
                            javafx.scene.Node childElement = parseElement((Element) childNode);
                            if (childElement != null) {
                                hbox.getChildren().add(childElement);
                                // Handle HBox.hgrow on child elements
                                if (childElement instanceof Region && ((Element) childNode).hasAttribute("HBox.hgrow")) {
                                    String hgrow = ((Element) childNode).getAttribute("HBox.hgrow");
                                    if ("ALWAYS".equals(hgrow)) {
                                        HBox.setHgrow((Region) childElement, javafx.scene.layout.Priority.ALWAYS);
                                    }
                                }
                            }
                        }
                    }
                    node = hbox;
                    break;

                case "Label":
                    Label label = new Label(element.getAttribute("text"));
                    if (element.hasAttribute("style"))
                        label.setStyle(element.getAttribute("style"));
                    if (element.hasAttribute("fx:id")) {
                        String fxId = element.getAttribute("fx:id");
                        nodeMap.put(fxId, label);
                        System.out.println("Registered Label: " + fxId);
                    }
                    node = label;
                    break;

                case "Button":
                    Button button = new Button(element.getAttribute("text"));
                    if (element.hasAttribute("style"))
                        button.setStyle(element.getAttribute("style"));
                    if (element.hasAttribute("prefWidth"))
                        button.setPrefWidth(Double.parseDouble(element.getAttribute("prefWidth")));
                    if (element.hasAttribute("fx:id")) {
                        String fxId = element.getAttribute("fx:id");
                        nodeMap.put(fxId, button);
                        System.out.println("Registered Button: " + fxId);
                    }
                    node = button;
                    break;

                case "TextField":
                    TextField tf = new TextField();
                    if (element.hasAttribute("promptText")) 
                        tf.setPromptText(element.getAttribute("promptText"));
                    if (element.hasAttribute("prefWidth"))
                        tf.setPrefWidth(Double.parseDouble(element.getAttribute("prefWidth")));
                    if (element.hasAttribute("maxWidth")) {
                        String maxWidth = element.getAttribute("maxWidth");
                        if ("Infinity".equals(maxWidth)) {
                            tf.setMaxWidth(Double.MAX_VALUE);
                        } else {
                            tf.setMaxWidth(Double.parseDouble(maxWidth));
                        }
                    }
                    if (element.hasAttribute("HBox.hgrow")) {
                        String hgrow = element.getAttribute("HBox.hgrow");
                        if ("ALWAYS".equals(hgrow)) {
                            HBox.setHgrow(tf, javafx.scene.layout.Priority.ALWAYS);
                            tf.setMaxWidth(Double.MAX_VALUE);
                        }
                    }
                    if (element.hasAttribute("fx:id")) {
                        String fxId = element.getAttribute("fx:id");
                        nodeMap.put(fxId, tf);
                        System.out.println("Registered TextField: " + fxId);
                    }
                    node = tf;
                    break;

                case "PasswordField":
                    PasswordField pf = new PasswordField();
                    if (element.hasAttribute("promptText")) 
                        pf.setPromptText(element.getAttribute("promptText"));
                    if (element.hasAttribute("prefWidth"))
                        pf.setPrefWidth(Double.parseDouble(element.getAttribute("prefWidth")));
                    if (element.hasAttribute("maxWidth")) {
                        String maxWidth = element.getAttribute("maxWidth");
                        if ("Infinity".equals(maxWidth)) {
                            pf.setMaxWidth(Double.MAX_VALUE);
                        } else {
                            pf.setMaxWidth(Double.parseDouble(maxWidth));
                        }
                    }
                    if (element.hasAttribute("HBox.hgrow")) {
                        String hgrow = element.getAttribute("HBox.hgrow");
                        if ("ALWAYS".equals(hgrow)) {
                            HBox.setHgrow(pf, javafx.scene.layout.Priority.ALWAYS);
                            pf.setMaxWidth(Double.MAX_VALUE);
                        }
                    }
                    if (element.hasAttribute("fx:id")) {
                        String fxId = element.getAttribute("fx:id");
                        nodeMap.put(fxId, pf);
                        System.out.println("Registered PasswordField: " + fxId);
                    }
                    node = pf;
                    break;

                case "TableView":
                    TableView<?> tableView = new TableView<>();
                    if (element.hasAttribute("prefHeight"))
                        tableView.setPrefHeight(Double.parseDouble(element.getAttribute("prefHeight")));
                    if (element.hasAttribute("fx:id")) {
                        String fxId = element.getAttribute("fx:id");
                        nodeMap.put(fxId, tableView);
                        System.out.println("Registered TableView: " + fxId);
                    }
                    node = tableView;
                    break;

                case "ComboBox":
                    ComboBox<String> comboBox = new ComboBox<>();
                    if (element.hasAttribute("promptText"))
                        comboBox.setPromptText(element.getAttribute("promptText"));
                    if (element.hasAttribute("prefWidth"))
                        comboBox.setPrefWidth(Double.parseDouble(element.getAttribute("prefWidth")));
                    if (element.hasAttribute("fx:id")) {
                        String fxId = element.getAttribute("fx:id");
                        nodeMap.put(fxId, comboBox);
                        System.out.println("Registered ComboBox: " + fxId);
                    }
                    node = comboBox;
                    break;

                case "BorderPane":
                    BorderPane borderPane = new BorderPane();
                    if (element.hasAttribute("prefWidth")) {
                        borderPane.setPrefWidth(Double.parseDouble(element.getAttribute("prefWidth")));
                    }
                    if (element.hasAttribute("prefHeight")) {
                        borderPane.setPrefHeight(Double.parseDouble(element.getAttribute("prefHeight")));
                    }
                    
                    NodeList bChildren = element.getChildNodes();
                    for (int i = 0; i < bChildren.getLength(); i++) {
                        Node childNode = bChildren.item(i);
                        if (childNode instanceof Element) {
                            Element childElement = (Element) childNode;
                            String regionTag = childElement.getTagName();
                            javafx.scene.Node regionNode = parseElement(childElement);
                            
                            if (regionNode != null) {
                                // Handle BorderPane regions
                                switch (regionTag) {
                                    case "top":
                                        borderPane.setTop(regionNode);
                                        break;
                                    case "center":
                                        borderPane.setCenter(regionNode);
                                        break;
                                    case "bottom":
                                        borderPane.setBottom(regionNode);
                                        break;
                                    case "left":
                                        borderPane.setLeft(regionNode);
                                        break;
                                    case "right":
                                        borderPane.setRight(regionNode);
                                        break;
                                    default:
                                        // If it's not a region tag, add to center as fallback
                                        borderPane.setCenter(regionNode);
                                }
                            }
                        }
                    }
                    node = borderPane;
                    break;

                case "TabPane":
                    TabPane tabPane = new TabPane();
                    if (element.hasAttribute("style")) {
                        tabPane.setStyle(element.getAttribute("style"));
                    }
                    // Handle VBox.vgrow to make TabPane expand
                    if (element.hasAttribute("VBox.vgrow")) {
                        String vgrow = element.getAttribute("VBox.vgrow");
                        if ("ALWAYS".equals(vgrow)) {
                            tabPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        }
                    }
                    NodeList tChildren = element.getChildNodes();
                    for (int i = 0; i < tChildren.getLength(); i++) {
                        Node childNode = tChildren.item(i);
                        if (childNode instanceof Element) {
                            Element childElement = (Element) childNode;
                            if ("Tab".equals(childElement.getTagName())) {
                                Tab tab = new Tab(childElement.getAttribute("text"));
                                NodeList tabChildren = childElement.getChildNodes();
                                for (int j = 0; j < tabChildren.getLength(); j++) {
                                    Node tabChildNode = tabChildren.item(j);
                                    if (tabChildNode instanceof Element) {
                                        Element tabChild = (Element) tabChildNode;
                                        javafx.scene.Node tabContent = parseElement(tabChild);
                                        if (tabContent != null) {
                                            tab.setContent(tabContent);
                                        }
                                    }
                                }
                                tabPane.getTabs().add(tab);
                            }
                        }
                    }
                    node = tabPane;
                    break;

                // Handle BorderPane region tags
                case "top":
                case "center":
                case "bottom":
                case "left":
                case "right":
                    // Process the first element child of the region
                    NodeList regionChildren = element.getChildNodes();
                    for (int i = 0; i < regionChildren.getLength(); i++) {
                        Node regionChild = regionChildren.item(i);
                        if (regionChild instanceof Element) {
                            node = parseElement((Element) regionChild);
                            break; // Only take the first element child
                        }
                    }
                    break;

                case "Region":
                    Region region = new Region();
                    if (element.hasAttribute("prefWidth")) {
                        region.setPrefWidth(Double.parseDouble(element.getAttribute("prefWidth")));
                    }
                    if (element.hasAttribute("prefHeight")) {
                        region.setPrefHeight(Double.parseDouble(element.getAttribute("prefHeight")));
                    }
                    if (element.hasAttribute("HBox.hgrow")) {
                        String hgrow = element.getAttribute("HBox.hgrow");
                        if ("ALWAYS".equals(hgrow)) {
                            HBox.setHgrow(region, javafx.scene.layout.Priority.ALWAYS);
                        }
                    }
                    if (element.hasAttribute("VBox.vgrow")) {
                        String vgrow = element.getAttribute("VBox.vgrow");
                        if ("ALWAYS".equals(vgrow)) {
                            VBox.setVgrow(region, javafx.scene.layout.Priority.ALWAYS);
                        } else if ("NEVER".equals(vgrow)) {
                            VBox.setVgrow(region, javafx.scene.layout.Priority.NEVER);
                        }
                    }
                    node = region;
                    break;

                case "DatePicker":
                    DatePicker datePicker = new DatePicker();
                    if (element.hasAttribute("promptText")) {
                        datePicker.setPromptText(element.getAttribute("promptText"));
                    }
                    if (element.hasAttribute("fx:id")) {
                        String fxId = element.getAttribute("fx:id");
                        nodeMap.put(fxId, datePicker);
                        System.out.println("Registered DatePicker: " + fxId);
                    }
                    node = datePicker;
                    break;

                default:
                    System.out.println("Unknown XML tag: " + tag);
                    // Return a simple label instead of null to avoid crashes
                    Label placeholder = new Label("[" + tag + "]");
                    placeholder.setStyle("-fx-text-fill: gray;");
                    return placeholder;
            }
        } catch (Exception e) {
            System.err.println("Error parsing element: " + tag + " - " + e.getMessage());
            // Return a placeholder instead of null to avoid crashes
            Label errorLabel = new Label("Error: " + tag);
            errorLabel.setStyle("-fx-text-fill: red;");
            return errorLabel;
        }

        return node;
    }
}