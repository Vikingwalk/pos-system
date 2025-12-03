package utils;

/**
 * Manages user session data across the application
 */
public class SessionManager {
    private static Integer currentUserId = null;
    private static String currentUsername = null;
    private static String currentUserRole = null;
    
    /**
     * Set the current logged-in user
     */
    public static void setCurrentUser(int userId, String username, String role) {
        currentUserId = userId;
        currentUsername = username;
        currentUserRole = role;
        System.out.println("Session started: User ID=" + userId + ", Username=" + username + ", Role=" + role);
    }
    
    /**
     * Clear the current session (on logout)
     */
    public static void clearSession() {
        System.out.println("Session cleared: User=" + currentUsername);
        currentUserId = null;
        currentUsername = null;
        currentUserRole = null;
    }
    
    /**
     * Get current user ID
     */
    public static Integer getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * Get current username
     */
    public static String getCurrentUsername() {
        return currentUsername;
    }
    
    /**
     * Get current user role
     */
    public static String getCurrentUserRole() {
        return currentUserRole;
    }
    
    /**
     * Check if a user is logged in
     */
    public static boolean isLoggedIn() {
        return currentUserId != null;
    }
}
