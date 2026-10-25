package accounts;

/**
 * AdminAcc – the administrator account type.
 *
 * OOP Concept – INHERITANCE
 * --------------------------
 * AdminAcc EXTENDS Account, which means it automatically inherits (gets for
 * free) the username and password fields and their getters from Account.
 *
 * The constructor calls super(username, password) to pass those values up
 * to the parent class (Account) so it can store them.
 *
 * Right now AdminAcc has no extra fields, but it could later gain admin-only
 * powers (e.g. managing game data) without changing Account at all.
 */
public class AdminAcc extends Account {

    /**
     * Creates an admin account with the given credentials.
     *
     * @param username  Admin username.
     * @param password  Admin password.
     */
    public AdminAcc(String username, String password) {
        // "super" calls the Account constructor with these values.
        super(username, password);
    }
}
