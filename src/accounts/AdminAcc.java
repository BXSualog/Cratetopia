package accounts;

/**
 * AdminAcc – the administrator account type.
 *
 * OOP Concepts demonstrated here
 * --------------------------------
 * INHERITANCE  : AdminAcc EXTENDS Account, inheriting the username and
 *                password fields and their public getters.
 * ENCAPSULATION: Added role field and authentication / login validation logic
 *                are encapsulated within this class.
 */
public class AdminAcc extends Account {

    private String role; // e.g. "System Administrator", "Super Admin", "Game Master"

    /**
     * Creates an admin account with default role "Administrator".
     *
     * @param username  Admin username.
     * @param password  Admin password.
     */
    public AdminAcc(String username, String password) {
        this(username, password, "Administrator");
    }

    /**
     * Creates an admin account with the given credentials and role.
     *
     * @param username  Admin username.
     * @param password  Admin password.
     * @param role      Admin role or designation.
     */
    public AdminAcc(String username, String password, String role) {
        super(username, password);
        this.role = (role == null || role.trim().isEmpty()) ? "Administrator" : role.trim();
    }

    // -------------------------------------------------------------------------
    // Role Getter & Setter
    // -------------------------------------------------------------------------

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role != null && !role.trim().isEmpty()) {
            this.role = role.trim();
        }
    }

    // -------------------------------------------------------------------------
    // Login & Authentication Logic
    // -------------------------------------------------------------------------

    /**
     * Verifies whether the provided password matches this admin's password.
     *
     * @param enteredPassword  The plaintext password to test.
     * @return                 true if passwords match; false otherwise.
     */
    public boolean authenticate(String enteredPassword) {
        if (enteredPassword == null) {
            return false;
        }
        return getPassword().equals(enteredPassword);
    }

    /**
     * Validates both username and password against this admin's credentials.
     * Username comparison is case-insensitive, while password comparison is exact.
     *
     * @param enteredUsername  The username entered by the user.
     * @param enteredPassword  The password entered by the user.
     * @return                 true if credentials match; false otherwise.
     */
    public boolean validateCredentials(String enteredUsername, String enteredPassword) {
        if (enteredUsername == null || enteredPassword == null) {
            return false;
        }
        return getUsername().equalsIgnoreCase(enteredUsername.trim()) && authenticate(enteredPassword);
    }

    @Override
    public String toString() {
        return "Admin [" + getUsername() + " | Role: " + role + "]";
    }
}
