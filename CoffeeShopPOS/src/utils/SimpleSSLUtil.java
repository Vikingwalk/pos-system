package utils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;

/**
 * Simplified SSL utility that works on all Java versions
 * Creates a keystore file if it doesn't exist, or loads existing one
 */
public class SimpleSSLUtil {
    
    private static final String KEYSTORE_FILE = "barcode-server.keystore";
    private static final String KEYSTORE_PASSWORD = "changeit";
    private static final String KEY_ALIAS = "barcode-server";
    
    /**
     * Create SSL context with self-signed certificate
     * Uses a keystore file approach that works on all Java versions
     */
    public static SSLContext createSSLContext() throws Exception {
        KeyStore keyStore = loadOrCreateKeystore();
        
        // Setup KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());
        
        // Setup TrustManagerFactory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        
        // Create SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        
        return sslContext;
    }
    
    /**
     * Load existing keystore or create a new one using keytool
     */
    private static KeyStore loadOrCreateKeystore() throws Exception {
        Path keystorePath = Paths.get(KEYSTORE_FILE);
        KeyStore keyStore = KeyStore.getInstance("JKS");
        
        // Try to load existing keystore
        if (Files.exists(keystorePath)) {
            try (FileInputStream fis = new FileInputStream(KEYSTORE_FILE)) {
                keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
                System.out.println("Loaded existing keystore from " + KEYSTORE_FILE);
                return keyStore;
            } catch (Exception e) {
                System.err.println("Failed to load existing keystore, will create new one: " + e.getMessage());
            }
        }
        
        // Create new keystore using keytool command
        System.out.println("Creating new keystore...");
        createKeystoreWithKeytool();
        
        // Load the newly created keystore
        try (FileInputStream fis = new FileInputStream(KEYSTORE_FILE)) {
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
            System.out.println("Successfully created and loaded keystore");
            return keyStore;
        } catch (Exception e) {
            throw new Exception("Failed to load keystore after creation: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create keystore using keytool command (works on all Java versions)
     */
    private static void createKeystoreWithKeytool() throws Exception {
        try {
            // Get Java home
            String javaHome = System.getProperty("java.home");
            String keytoolPath = javaHome + File.separator + "bin" + File.separator + "keytool";
            
            // On Windows, use keytool.exe
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                keytoolPath += ".exe";
            }
            
            // Check if keytool exists
            File keytoolFile = new File(keytoolPath);
            if (!keytoolFile.exists()) {
                throw new Exception("keytool not found at: " + keytoolPath);
            }
            
            // Get local network IP for SAN extension
            String localIP = getLocalNetworkIP();
            String sanExtension = "SAN=DNS:localhost,IP:127.0.0.1";
            
            // Add local IP to SAN if we have a valid one (not localhost)
            if (localIP != null && !localIP.equals("localhost") && !localIP.equals("127.0.0.1")) {
                sanExtension += ",IP:" + localIP;
                System.out.println("Including local network IP in certificate: " + localIP);
            }
            
            // Build keytool command
            ProcessBuilder pb = new ProcessBuilder(
                keytoolPath,
                "-genkeypair",
                "-alias", KEY_ALIAS,
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-validity", "365",
                "-keystore", KEYSTORE_FILE,
                "-storepass", KEYSTORE_PASSWORD,
                "-keypass", KEYSTORE_PASSWORD,
                "-dname", "CN=Barcode Scanner, OU=POS System, O=Coffee Shop, C=US",
                "-ext", sanExtension
            );
            
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Wait for process to complete
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                // Read error output
                StringBuilder error = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line).append("\n");
                    }
                }
                throw new Exception("keytool failed with exit code " + exitCode + ": " + error.toString());
            }
            
            // Verify keystore was created
            if (!Files.exists(Paths.get(KEYSTORE_FILE))) {
                throw new Exception("Keystore file was not created");
            }
            
            System.out.println("Keystore created successfully using keytool");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Keytool process was interrupted", e);
        } catch (Exception e) {
            throw new Exception("Failed to create keystore with keytool: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get the local network IP address (same logic as BarcodeReceiver)
     * @return IP address as string, or "localhost" if none found
     */
    private static String getLocalNetworkIP() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces =
                    java.net.NetworkInterface.getNetworkInterfaces();

            String preferredIP = null;
            String fallbackIP = null;

            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();

                // Skip loopback and down interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                // Skip VMware virtual adapters
                String interfaceName = networkInterface.getDisplayName().toLowerCase();
                if (interfaceName.contains("vmware") || interfaceName.contains("vmnet")) {
                    continue;
                }

                java.util.Enumeration<java.net.InetAddress> addresses =
                        networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    java.net.InetAddress address = addresses.nextElement();

                    if (address instanceof java.net.Inet4Address &&
                            !address.isLoopbackAddress()) {

                        String ip = address.getHostAddress();

                        // Skip APIPA addresses (169.254.x.x)
                        if (ip.startsWith("169.254.")) {
                            if (fallbackIP == null) fallbackIP = ip;
                            continue;
                        }

                        // Skip VMware network ranges
                        if (ip.startsWith("192.168.100.") || ip.startsWith("192.168.134.")) {
                            continue;
                        }

                        // This is a valid IP - use it
                        preferredIP = ip;
                        break;
                    }
                }

                if (preferredIP != null) {
                    return preferredIP;
                }
            }

            if (fallbackIP != null) {
                return fallbackIP;
            }

            return "localhost";

        } catch (Exception e) {
            return "localhost";
        }
    }
}

