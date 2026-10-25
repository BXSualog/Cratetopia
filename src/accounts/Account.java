package accounts;

/**
 * Account – the base (parent) class for all account types in the game.
 *
 * OOP Concept – ABSTRACTION & INHERITANCE
 * ----------------------------------------
 * This class is declared "abstract", meaning you can never create an Account
 * object directly.  You must create a more specific subclass like PlayerAcc or
 * AdminAcc.  Think of Account as a blueprint that both players and admins share:
 * every account has a username and a password.
 *
 * The "private final" fields enforce ENCAPSULATION: only the Account class
 * itself can see the raw values; everyone else must use the public getters.
 */
public abstract class Account {

    // "private" = only this class can read/write these fields directly.
    // "final"   = once set in the constructor, they can never be changed.
    private final String username;
    private final String password;

    /**
     * Constructor – called by subclass constructors (PlayerAcc / AdminAcc)
     * via "super(username, password)".
     *
     * @param username  The unique display name chosen by the user.
     * @param password  The secret password for this account.
     */
    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // -------------------------------------------------------------------------
    // Getters – the only public way to read the private fields.
    // -------------------------------------------------------------------------

    /** Returns the account's username. */
    public String getUsername() {
        return username;
    }

    /** Returns the account's password. */
    public String getPassword() {
        return password;
    }
}
