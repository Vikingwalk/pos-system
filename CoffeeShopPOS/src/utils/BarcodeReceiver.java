package utils;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * HTTP server that receives barcode data from mobile devices
 * Allows phone barcode scanner to send scanned barcodes to the desktop application
 */
public class BarcodeReceiver {
    private Object server; // Can be HttpServer or HttpsServer
    private Consumer<String> barcodeCallback;
    private int port;
    private boolean isRunning = false;
    private boolean useHTTPS = true; // Try HTTPS first, fallback to HTTP if needed

    /**
     * Create a barcode receiver server
     * @param port Port number to listen on (default: 8088)
     * @param barcodeCallback Callback function called when barcode is received
     */
    public BarcodeReceiver(int port, Consumer<String> barcodeCallback) {
        this.port = port; // ✅ use the provided port
        this.barcodeCallback = barcodeCallback;
    }

    /**
     * Start the HTTP server
     * Tries the specified port first, then tries alternative ports if needed
     */
    public void start() {
        if (isRunning && server != null) {
            System.out.println("Stopping existing server on port " + port);
            stop();
        }

        int startPort = port;
        int maxAttempts = 10;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int currentPort = startPort + attempt;

            try {
                if (!isPortAvailable(currentPort)) {
                    System.out.println("Port " + currentPort + " is in use, trying next port...");
                    continue;
                }

                // Try HTTPS first
                if (useHTTPS) {
                    try {
                        // Create HTTPS server
                        HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(currentPort), 0);
                        
                        // Configure SSL context
                        SSLContext sslContext = createSSLContext();
                        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                            @Override
                            public void configure(HttpsParameters params) {
                                try {
                                    javax.net.ssl.SSLContext context = getSSLContext();
                                    javax.net.ssl.SSLParameters sslParams = context.getDefaultSSLParameters();
                                    params.setSSLParameters(sslParams);
                                } catch (Exception e) {
                                    System.err.println("Error configuring HTTPS: " + e.getMessage());
                                }
                            }
                        });
                        
                        httpsServer.createContext("/barcode", new BarcodeHandler());
                        httpsServer.createContext("/scanner", new ScannerPageHandler());
                        httpsServer.createContext("/", new RootHandler());
                        
                        httpsServer.setExecutor(null);
                        httpsServer.start();
                        server = httpsServer;
                        isRunning = true;
                        port = currentPort;

                        System.out.println("========================================");
                        System.out.println("Barcode Receiver Server Started (HTTPS)!");
                        System.out.println("========================================");
                        if (port != startPort) {
                            System.out.println("Note: Port " + startPort + " was in use, using port " + port + " instead");
                        } else {
                            System.out.println("Port: " + port);
                        }
                        System.out.println("Open on your phone:");
                        System.out.println("https://" + getLocalIP() + ":" + port + "/scanner");
                        System.out.println("Note: You may see a security warning - click 'Advanced' then 'Proceed'");
                        System.out.println("Camera should now work!");
                        System.out.println("========================================");

                        return;
                    } catch (Exception httpsError) {
                        // HTTPS failed - will try HTTP below
                        System.err.println("HTTPS setup failed: " + httpsError.getMessage());
                        System.err.println("Falling back to HTTP (camera may not work in some browsers)");
                        useHTTPS = false;
                        // Continue to HTTP fallback below - don't throw, let it try HTTP
                    }
                }
                
                // Fallback to HTTP if HTTPS failed or disabled
                if (!useHTTPS) {
                    try {
                        HttpServer httpServer = HttpServer.create(new InetSocketAddress(currentPort), 0);
                        
                        setupHttpServerContexts(httpServer);
                        httpServer.setExecutor(null);
                        httpServer.start();
                        
                        server = httpServer;
                        isRunning = true;
                        port = currentPort;

                        System.out.println("========================================");
                        System.out.println("Barcode Receiver Server Started (HTTP)!");
                        System.out.println("========================================");
                        System.out.println("WARNING: Using HTTP instead of HTTPS");
                        System.out.println("Camera may not work in some browsers.");
                        System.out.println("Manual entry will always work.");
                        System.out.println("========================================");
                        if (port != startPort) {
                            System.out.println("Note: Port " + startPort + " was in use, using port " + port + " instead");
                        } else {
                            System.out.println("Port: " + port);
                        }
                        System.out.println("Open on your phone:");
                        System.out.println("http://" + getLocalIP() + ":" + port + "/scanner");
                        System.out.println("========================================");

                        return;
                    } catch (java.net.BindException httpBindError) {
                        // Port in use - continue to next port
                        if (attempt < maxAttempts - 1) {
                            System.out.println("Port " + currentPort + " is in use for HTTP, trying next port...");
                            continue;
                        } else {
                            throw new RuntimeException("Could not start HTTP server. All ports are in use.", httpBindError);
                        }
                    } catch (Exception httpError) {
                        // Other HTTP errors - continue to next port attempt
                        if (attempt < maxAttempts - 1) {
                            System.out.println("HTTP setup failed on port " + currentPort + ", trying next port...");
                            continue;
                        } else {
                            throw new RuntimeException("Could not start HTTP server after trying all ports.", httpError);
                        }
                    }
                }

            } catch (Exception e) {
                // Check if it's a port binding error
                if (e instanceof java.net.BindException || 
                    (e.getCause() != null && e.getCause() instanceof java.net.BindException)) {
                    if (attempt < maxAttempts - 1) {
                        System.out.println("Port " + currentPort + " is already in use, trying port " + (currentPort + 1) + "...");
                        continue;
                    } else {
                        System.err.println("Failed to start barcode receiver server: All ports from " +
                                startPort + " to " + (startPort + maxAttempts - 1) + " are in use.");
                        throw new RuntimeException("Could not start barcode receiver server. All ports are in use.", e);
                    }
                }
                
                // Check if it's an HTTPS setup error (SSL/certificate)
                String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                if (errorMsg.contains("SSL") || errorMsg.contains("certificate") || 
                    errorMsg.contains("HTTPS") || errorMsg.contains("X509")) {
                    System.err.println("HTTPS setup failed: " + errorMsg);
                    System.err.println("Falling back to HTTP (camera may not work in some browsers)");
                    useHTTPS = false;
                    continue; // Try again with HTTP
                }
                
                // Other errors
                if (attempt < maxAttempts - 1) {
                    System.out.println("Failed to start on port " + currentPort + ", trying next port...");
                    continue;
                } else {
                    System.err.println("Failed to start barcode receiver server after trying ports");
                    throw new RuntimeException("Could not start barcode receiver server.", e);
                }
            }
        }
    }
    
    /**
     * Setup server contexts (for HTTP fallback)
     */
    private void setupHttpServerContexts(HttpServer httpServer) {
        httpServer.createContext("/barcode", new BarcodeHandler());
        httpServer.createContext("/scanner", new ScannerPageHandler());
        httpServer.createContext("/", new RootHandler());
    }

    private boolean isPortAvailable(int port) {
        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(port)) {
            serverSocket.setReuseAddress(false);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void stop() {
        if (server != null && isRunning) {
            try {
                // Use reflection to call stop() since server can be HttpServer or HttpsServer
                java.lang.reflect.Method stopMethod = server.getClass().getMethod("stop", int.class);
                stopMethod.invoke(server, 0);
                isRunning = false;
                System.out.println("Barcode receiver server stopped on port " + port);
            } catch (Exception e) {
                System.err.println("Error stopping server: " + e.getMessage());
            } finally {
                server = null;
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getServerURL() {
        String protocol = useHTTPS ? "https" : "http";
        return protocol + "://" + getLocalIP() + ":" + port;
    }
    
    /**
     * Create SSL context with self-signed certificate
     * Tries SimpleSSLUtil first (works on all Java versions), falls back to SSLUtil
     */
    private SSLContext createSSLContext() {
        try {
            // Try SimpleSSLUtil first (uses keytool, works on all Java versions)
            return SimpleSSLUtil.createSSLContext();
        } catch (Exception e1) {
            System.err.println("SimpleSSLUtil failed, trying SSLUtil: " + e1.getMessage());
            try {
                // Fallback to SSLUtil (uses internal APIs, works on Java 8-11)
                return SSLUtil.createSSLContext();
            } catch (Exception e2) {
                System.err.println("========================================");
                System.err.println("ERROR: Could not create HTTPS server!");
                System.err.println("========================================");
                System.err.println("Camera requires HTTPS, but SSL certificate generation failed.");
                System.err.println("SimpleSSLUtil error: " + e1.getMessage());
                System.err.println("SSLUtil error: " + e2.getMessage());
                System.err.println("");
                System.err.println("SOLUTIONS:");
                System.err.println("1. Use manual entry (always works)");
                System.err.println("2. Ensure keytool is available in your Java installation");
                System.err.println("3. Check file permissions in the project directory");
                System.err.println("========================================");
                e2.printStackTrace();
                throw new RuntimeException("HTTPS setup failed. Camera will not work. Use manual entry instead.", e2);
            }
        }
    }

    private String getLocalIP() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces =
                    java.net.NetworkInterface.getNetworkInterfaces();

            String preferredIP = null;
            String fallbackIP = null;

            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                java.util.Enumeration<java.net.InetAddress> addresses =
                        networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    java.net.InetAddress address = addresses.nextElement();

                    if (address instanceof java.net.Inet4Address &&
                            !address.isLoopbackAddress()) {

                        String ip = address.getHostAddress();

                        if (!ip.startsWith("169.254.")) {
                            preferredIP = ip;
                            break;
                        } else {
                            if (fallbackIP == null) fallbackIP = ip;
                        }
                    }
                }

                if (preferredIP != null) {
                    return preferredIP;
                }
            }

            if (fallbackIP != null) {
                System.out.println("Warning: Using APIPA address " + fallbackIP);
                return fallbackIP;
            }

            System.out.println("Warning: Using localhost");
            return "localhost";

        } catch (Exception e) {
            System.err.println("Error getting IP: " + e.getMessage());
            return "localhost";
        }
    }

    private class BarcodeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));

                StringBuilder requestBodyContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBodyContent.append(line);
                }

                String barcode = requestBodyContent.toString().trim();

                // Process barcode callback (optimized - minimal logging)
                boolean processed = false;
                if (barcodeCallback != null && !barcode.isEmpty()) {
                    try {
                        Class<?> platformClass = Class.forName("javafx.application.Platform");
                        java.lang.reflect.Method runLaterMethod =
                                platformClass.getMethod("runLater", Runnable.class);
                        runLaterMethod.invoke(null, (Runnable) () -> barcodeCallback.accept(barcode));
                        processed = true;
                    } catch (Exception e) {
                        // Fallback to direct call
                        barcodeCallback.accept(barcode);
                        processed = true;
                    }
                }

                // Response with processing status
                String response = "{\"status\":\"success\",\"barcode\":\"" + barcode + "\",\"processed\":" + processed + ",\"message\":\"Barcode received and processed successfully\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.length());

                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();

            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(200, -1);
                exchange.close();

            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    private class ScannerPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {

                String html = getScannerHTML();
                byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

                // ✅ Ensure Chrome on mobile allows camera without flags
                String origin = "http://" + getLocalIP() + ":" + port;

                exchange.getResponseHeaders().add(
                        "Permissions-Policy",
                        "camera=(self \"" + origin + "\")"
                );

                exchange.getResponseHeaders().add(
                        "Feature-Policy",
                        "camera 'self'"
                );

                exchange.sendResponseHeaders(200, htmlBytes.length);

                OutputStream os = exchange.getResponseBody();
                os.write(htmlBytes);
                os.close();

                System.out.println("Scanner page served to: " + exchange.getRemoteAddress());

            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    private class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String redirect = "<!DOCTYPE html><html><head><meta http-equiv='refresh' content='0;url=/scanner'></head><body>Redirecting...</body></html>";

                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, redirect.length());

                OutputStream os = exchange.getResponseBody();
                os.write(redirect.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    private String getScannerHTML() {
        return """
<!DOCTYPE html>
<html lang='en'>
<head>
    <meta charset='UTF-8'>
    <meta name='viewport' content='width=device-width, initial-scale=1.0'>
    <title>Barcode Scanner</title>

    <script>
        // Load scanner library - scannerLoaded will be set in main script
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/html5-qrcode@2.3.8/html5-qrcode.min.js';
        script.onload = () => {
            if (typeof window.setScannerLoaded === 'function') {
                window.setScannerLoaded(true);
            }
        };
        script.onerror = () => {
            document.getElementById('reader').innerHTML =
                '<p style="padding:20px;text-align:center;">Camera scanning unavailable (no internet). Use manual mode below.</p>';
        };
        document.head.appendChild(script);
    </script>

    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f4f4f4; min-height: 100vh; display: flex; justify-content: center; padding: 20px; }
        .container { background: white; width: 100%; max-width: 500px; padding: 25px; border-radius: 15px; box-shadow: 0 4px 20px rgba(0,0,0,0.15); }
        #reader { margin: 20px 0; width: 100%; border-radius: 10px; overflow: hidden; min-height: 200px; background: #eee; display: flex; align-items: center; justify-content: center; }
        h1 { text-align: center; margin-bottom: 20px; }
        button { width: 100%; padding: 15px; margin-top: 10px; border: none; border-radius: 8px; background: #667eea; color: white; font-size: 16px; font-weight: bold; cursor: pointer; }
        button:disabled { background: #ccc; cursor: not-allowed; }
        .manual-input { margin-top: 20px; }
        input[type="text"] { width: 100%; padding: 12px; margin: 10px 0; border-radius: 8px; border: 2px solid #ccc; font-size: 16px; }
        .status { padding: 12px; margin-top: 15px; border-radius: 8px; text-align: center; }
        .status.info  { background: #e0f3ff; color: #055160; }
        .status.error { background: #ffe0e0; color: #7a1f1f; }
        .status.success { background: #dcf7e0; color: #165a21; }
    </style>
</head>
<body>

<div class="container">
    <h1>📱 Barcode Scanner</h1>

    <div id="reader"><p>Camera preview will appear here</p></div>
    <div id="status" class="status info">Ready. Use camera or manual mode.</div>

    <button id="startBtn" onclick="startScanning()">Start Camera</button>
    <button id="stopBtn" onclick="stopScanning()" disabled>Stop Camera</button>
    <button onclick="activateManualMode()" style="background:#444;">Manual Mode Only</button>

    <div class="manual-input">
        <h3>Manual Entry (Always Works)</h3>
        <input id="manualBarcode" type="text" placeholder="Enter barcode manually">
        <button style="background:#4CAF50;" onclick="sendManualBarcode()">Send Barcode</button>
    </div>
</div>

<script>
    let scannerLoaded = false;
    let html5QrcodeScanner = null;
    const serverURL = window.location.origin;
    
    // Cooldown tracking: prevent duplicate scans
    let lastScannedBarcode = null;
    let lastScanTime = 0;
    const SCAN_COOLDOWN_MS = 5000; // 5 seconds
    let isProcessing = false;
    
    // Function to set scanner loaded status (called by library loader script)
    window.setScannerLoaded = function(loaded) {
        scannerLoaded = loaded;
    };

    window.addEventListener("DOMContentLoaded", () => {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            document.getElementById("startBtn").disabled = true;
            updateStatus("Camera not supported on this browser. Use manual entry below.", "error");
        }
    });

    function updateStatus(msg, type="info") {
        const s = document.getElementById("status");
        s.textContent = msg;
        s.className = "status " + type;
    }
    
    function showNotification(message, type="success") {
        // Create notification element
        const notification = document.createElement("div");
        notification.className = "notification " + type;
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: ${type === "success" ? "#4CAF50" : "#f44336"};
            color: white;
            padding: 15px 25px;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.3);
            z-index: 10000;
            font-size: 16px;
            font-weight: bold;
            animation: slideDown 0.3s ease-out;
        `;
        
        // Add animation
        const style = document.createElement("style");
        style.textContent = `
            @keyframes slideDown {
                from { transform: translateX(-50%) translateY(-100px); opacity: 0; }
                to { transform: translateX(-50%) translateY(0); opacity: 1; }
            }
        `;
        if (!document.getElementById("notificationStyle")) {
            style.id = "notificationStyle";
            document.head.appendChild(style);
        }
        
        document.body.appendChild(notification);
        
        // Remove after 3 seconds
        setTimeout(() => {
            notification.style.animation = "slideDown 0.3s ease-out reverse";
            setTimeout(() => notification.remove(), 300);
        }, 3000);
        
        // Also try browser notification API if available
        if ("Notification" in window && Notification.permission === "granted") {
            new Notification("Barcode Scanned", {
                body: message,
                icon: "/favicon.ico"
            });
        }
    }

    function activateManualMode() {
        stopScanning();
        updateStatus("Manual mode enabled. Camera disabled.", "info");
        document.getElementById("startBtn").disabled = true;
    }

    function sendBarcode(code) {
        if (!code) return;
        
        // Check cooldown - prevent duplicate scans
        const now = Date.now();
        if (code === lastScannedBarcode && (now - lastScanTime) < SCAN_COOLDOWN_MS) {
            const remainingSeconds = Math.ceil((SCAN_COOLDOWN_MS - (now - lastScanTime)) / 1000);
            updateStatus("⏳ Please wait " + remainingSeconds + "s before scanning again", "info");
            showNotification("Already scanned! Wait " + remainingSeconds + " seconds", "error");
            return;
        }
        
        // Prevent concurrent requests
        if (isProcessing) {
            updateStatus("Processing previous scan...", "info");
            return;
        }
        
        isProcessing = true;
        updateStatus("Sending barcode: " + code, "info");

        fetch(serverURL + "/barcode", {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: code
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("Server returned: " + response.status + " " + response.statusText);
            }
            return response.text().then(text => {
                try {
                    return JSON.parse(text);
                } catch (e) {
                    return { status: "success", message: text };
                }
            });
        })
        .then(data => {
            // Update cooldown tracking
            lastScannedBarcode = code;
            lastScanTime = Date.now();
            isProcessing = false;
            
            // Show success notification
            showNotification("✓ Scanned successfully: " + code, "success");
            updateStatus("✔ Scanned: " + code + " - Success! (5s cooldown)", "success");
            console.log("Barcode sent successfully:", code, data);
        })
        .catch(e => {
            isProcessing = false;
            updateStatus("✗ Error: " + e.message, "error");
            showNotification("✗ Error: " + e.message, "error");
            console.error("Error sending barcode:", e);
        });
    }

    function sendManualBarcode() {
        const code = document.getElementById("manualBarcode").value.trim();
        if (code.length === 0) return;
        sendBarcode(code);
        document.getElementById("manualBarcode").value = "";
    }

    function startScanning() {
        if (!scannerLoaded) { updateStatus("Scanner library not loaded. Use manual entry.", "error"); return; }
        
        // Request notification permission
        if ("Notification" in window && Notification.permission === "default") {
            Notification.requestPermission();
        }

        html5QrcodeScanner = new Html5Qrcode("reader");
        html5QrcodeScanner.start(
            { facingMode: "environment" },
            { fps: 10, qrbox: 250 },
            (decodedText) => {
                // Only process if not in cooldown and not already processing
                const now = Date.now();
                if (decodedText !== lastScannedBarcode || (now - lastScanTime) >= SCAN_COOLDOWN_MS) {
                    if (!isProcessing) {
                        sendBarcode(decodedText);
                    }
                } else {
                    const remainingSeconds = Math.ceil((SCAN_COOLDOWN_MS - (now - lastScanTime)) / 1000);
                    updateStatus("⏳ Cooldown: " + remainingSeconds + "s remaining", "info");
                }
            },
            () => {}
        )
        .then(() => {
            updateStatus("Camera running. Aim at barcode.", "success");
            document.getElementById("startBtn").disabled = true;
            document.getElementById("stopBtn").disabled = false;
        })
        .catch(err => { updateStatus("Camera error: " + err + ". Manual mode available.", "error"); });
    }

    function stopScanning() {
        if (html5QrcodeScanner) {
            html5QrcodeScanner.stop().then(() => {
                html5QrcodeScanner.clear();
                html5QrcodeScanner = null;
                updateStatus("Camera stopped.", "info");
                document.getElementById("startBtn").disabled = false;
                document.getElementById("stopBtn").disabled = true;
            });
        }
    }

    document.getElementById("manualBarcode").addEventListener("keypress", e => {
        if (e.key === "Enter") sendManualBarcode();
    });
</script>

</body>
</html>
""";
    }
}
