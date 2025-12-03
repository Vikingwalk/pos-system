import java.net.*;
import java.util.*;

/**
 * Quick diagnostic tool to check network IP address detection
 * Run this to see what IP your computer is using on the network
 */
public class CheckNetworkIP {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("Network IP Diagnostic Tool");
        System.out.println("=========================================\n");
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            System.out.println("All Network Interfaces:\n");
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                System.out.println("Interface: " + networkInterface.getName());
                System.out.println("  Display Name: " + networkInterface.getDisplayName());
                System.out.println("  Is Up: " + networkInterface.isUp());
                System.out.println("  Is Loopback: " + networkInterface.isLoopback());
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    
                    if (address instanceof Inet4Address) {
                        String ip = address.getHostAddress();
                        System.out.println("  → IPv4: " + ip);
                        
                        if (!address.isLoopbackAddress()) {
                            if (!ip.startsWith("169.254.")) {
                                System.out.println("     ✓ This is a VALID network IP!");
                            } else {
                                System.out.println("     ⚠ APIPA address (no DHCP)");
                            }
                        }
                    }
                }
                System.out.println();
            }
            
            // Now show what the application would detect
            System.out.println("=========================================");
            System.out.println("What BarcodeReceiver Will Use:");
            System.out.println("=========================================");
            System.out.println("IP Address: " + getDetectedIP());
            System.out.println("\nUse this URL on your phone:");
            System.out.println("https://" + getDetectedIP() + ":8088/scanner");
            System.out.println("=========================================");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String getDetectedIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            String preferredIP = null;
            String fallbackIP = null;
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
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
                return fallbackIP + " (APIPA - Check DHCP)";
            }
            
            return "localhost (NO NETWORK DETECTED!)";
            
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
