package accounts;

/**
 * AdminAcc – the administrator account type.
 *
 * OOP Concept – INHERITANCE
 * --------------------------
 * AdminAcc EXTENDS Account, so it inherits the username and password fields
 * (and their getters) from Account. The constructor passes them up with
 * super(username, password).
 *
 * New in this version: AdminAcc knows how to check a login attempt itself.
 * That keeps the "does this username/password match?" rule inside the class
 * that owns the credentials (Encapsulation).
 */
public class AdminAcc extends Account {

    public AdminAcc(String username, String password) {
        super(username, password);
    }

    /**
     * Checks a login attempt against this admin's credentials.
     * The username is case-insensitive (like player usernames);
     * the password is case-sensitive.
     *
     * @return true if both match.
     */
    public boolean login(String username, String password) {
        return getUsername().equalsIgnoreCase(username)
                && getPassword().equals(password);
    }
}
