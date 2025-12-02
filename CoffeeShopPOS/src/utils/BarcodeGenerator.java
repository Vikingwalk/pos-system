package utils;

import java.util.Random;

public class BarcodeGenerator {
    
    /**
     * Generates a barcode in EAN-13 format (13 digits)
     * Format: Prefix(3) + Product ID(6) + Random(3) + Check Digit(1)
     * @param productId The product ID to encode in the barcode
     * @return A 13-digit barcode string
     */
    public static String generateEAN13(int productId) {
        String prefix = "200"; // Common prefix for internal use
        String productCode = String.format("%06d", productId); // 6 digits, zero-padded
        String randomPart = String.format("%03d", new Random().nextInt(1000)); // 3 random digits
        
        String barcodeWithoutChecksum = prefix + productCode + randomPart;
        int checkDigit = calculateEAN13CheckDigit(barcodeWithoutChecksum);
        
        return barcodeWithoutChecksum + checkDigit;
    }
    
    /**
     * Generates a simpler barcode using just the product ID with a prefix
     * Format: PREFIX + Product ID (e.g., PROD000001)
     * @param productId The product ID
     * @return A barcode string
     */
    public static String generateSimple(int productId) {
        return String.format("PROD%06d", productId);
    }
    
    /**
     * Calculates the EAN-13 check digit
     * @param barcode12digits First 12 digits of the barcode
     * @return The check digit
     */
    private static int calculateEAN13CheckDigit(String barcode12digits) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(barcode12digits.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit;
    }
    
    /**
     * Validates an EAN-13 barcode
     * @param barcode The barcode to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateEAN13(String barcode) {
        if (barcode == null || barcode.length() != 13) {
            return false;
        }
        
        try {
            String first12 = barcode.substring(0, 12);
            int providedCheckDigit = Character.getNumericValue(barcode.charAt(12));
            int calculatedCheckDigit = calculateEAN13CheckDigit(first12);
            
            return providedCheckDigit == calculatedCheckDigit;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extracts product ID from the barcode (assuming our format)
     * @param barcode The barcode string
     * @return The product ID, or -1 if extraction fails
     */
    public static int extractProductIdFromEAN13(String barcode) {
        try {
            if (barcode != null && barcode.length() == 13) {
                // Extract positions 3-9 (the product code part)
                String productCode = barcode.substring(3, 9);
                return Integer.parseInt(productCode);
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }
    
    /**
     * Extracts product ID from simple barcode format (PRODXXXXXX)
     * @param barcode The barcode string
     * @return The product ID, or -1 if extraction fails
     */
    public static int extractProductIdFromSimple(String barcode) {
        try {
            if (barcode != null && barcode.startsWith("PROD") && barcode.length() >= 10) {
                String productCode = barcode.substring(4);
                return Integer.parseInt(productCode);
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }
}
