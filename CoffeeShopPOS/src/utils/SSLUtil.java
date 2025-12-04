package utils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

/**
 * Utility class for creating SSL context with self-signed certificate
 * For local development use only
 */
public class SSLUtil {
    
    /**
     * Create SSL context with self-signed certificate
     */
    public static SSLContext createSSLContext() throws Exception {
        char[] password = "changeit".toCharArray();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, password);
        
        // Generate key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        
        // Create self-signed certificate
        X509Certificate cert = generateSelfSignedCertificate(keyPair);
        
        // Add to keystore
        Certificate[] chain = {cert};
        keyStore.setKeyEntry("barcode-server", keyPair.getPrivate(), password, chain);
        
        // Setup KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password);
        
        // Setup TrustManagerFactory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        
        // Create SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        
        return sslContext;
    }
    
    /**
     * Generate a self-signed X.509 certificate
     * Uses reflection to access internal APIs if available, otherwise throws exception
     */
    @SuppressWarnings("restriction")
    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
        try {
            // Try to use sun.security.x509 (internal API)
            return createCertificateUsingInternalAPI(keyPair);
        } catch (Exception e) {
            // If internal API not available, provide helpful error
            throw new Exception(
                "Cannot generate SSL certificate: Java internal APIs not accessible.\n" +
                "For HTTPS to work, you need to either:\n" +
                "1. Add BouncyCastle library to your project, OR\n" +
                "2. Use Java 8-11 (which has accessible internal APIs), OR\n" +
                "3. Generate a keystore file manually and load it\n\n" +
                "Error: " + e.getMessage());
        }
    }
    
    /**
     * Create certificate using sun.security.x509 (internal API)
     */
    @SuppressWarnings("restriction")
    private static X509Certificate createCertificateUsingInternalAPI(KeyPair keyPair) throws Exception {
        try {
            Class<?> x509CertInfoClass = Class.forName("sun.security.x509.X509CertInfo");
            Class<?> certValidityClass = Class.forName("sun.security.x509.CertificateValidity");
            Class<?> certSerialNumClass = Class.forName("sun.security.x509.CertificateSerialNumber");
            Class<?> certIssuerNameClass = Class.forName("sun.security.x509.CertificateIssuerName");
            Class<?> certSubjectNameClass = Class.forName("sun.security.x509.CertificateSubjectName");
            Class<?> certX509KeyClass = Class.forName("sun.security.x509.CertificateX509Key");
            Class<?> algorithmIdClass = Class.forName("sun.security.x509.AlgorithmId");
            Class<?> certVersionClass = Class.forName("sun.security.x509.CertificateVersion");
            
            String dn = "CN=Barcode Scanner, OU=POS System, O=Coffee Shop, C=US";
            
            Object certInfo = x509CertInfoClass.getConstructor().newInstance();
            java.lang.reflect.Method set = x509CertInfoClass.getMethod("set", 
                Class.forName("sun.security.x509.CertificateAttributeName"), Object.class);
            
            // Version
            Object version = certVersionClass.getConstructor(int.class).newInstance(2);
            set.invoke(certInfo, certVersionClass.getField("VERSION").get(null), version);
            
            // Serial number
            BigInteger serial = new BigInteger(64, new SecureRandom());
            Object serialNum = certSerialNumClass.getConstructor(BigInteger.class).newInstance(serial);
            set.invoke(certInfo, certSerialNumClass.getField("NAME").get(null), serialNum);
            
            // Algorithm
            Object algId = algorithmIdClass.getMethod("get", String.class).invoke(null, "SHA256withRSA");
            set.invoke(certInfo, algorithmIdClass.getField("NAME").get(null), algId);
            
            // Subject
            X500Principal subjectPrincipal = new X500Principal(dn);
            Object subjectName = certSubjectNameClass.getConstructor(X500Principal.class).newInstance(subjectPrincipal);
            set.invoke(certInfo, certSubjectNameClass.getField("NAME").get(null), subjectName);
            
            // Issuer (self-signed)
            Object issuerName = certIssuerNameClass.getConstructor(X500Principal.class).newInstance(subjectPrincipal);
            set.invoke(certInfo, certIssuerNameClass.getField("NAME").get(null), issuerName);
            
            // Validity
            Date from = new Date();
            Date to = new Date(from.getTime() + 365L * 24 * 60 * 60 * 1000);
            Object validity = certValidityClass.getConstructor(Date.class, Date.class).newInstance(from, to);
            set.invoke(certInfo, certValidityClass.getField("NAME").get(null), validity);
            
            // Key
            Object key = certX509KeyClass.getConstructor(PublicKey.class).newInstance(keyPair.getPublic());
            set.invoke(certInfo, certX509KeyClass.getField("NAME").get(null), key);
            
            // Create and sign
            byte[] encoded = (byte[]) x509CertInfoClass.getMethod("getEncodedInfo").invoke(certInfo);
            
            // Use reflection to create X509CertImpl
            Class<?> x509CertImplClass = Class.forName("sun.security.x509.X509CertImpl");
            Object certImpl = x509CertImplClass.getConstructor(byte[].class).newInstance(encoded);
            
            // Sign the certificate
            java.lang.reflect.Method signMethod = x509CertImplClass.getMethod("sign", 
                PrivateKey.class, String.class);
            signMethod.invoke(certImpl, keyPair.getPrivate(), "SHA256withRSA");
            
            return (X509Certificate) certImpl;
            
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            throw new Exception("Internal API not available in this Java version", e);
        }
    }
}

