import java.util.Scanner;

/**
 * InputHandling – responsible for reading input from the keyboard.
 *
 * Why a separate class?
 *   Instead of scattering "new Scanner(System.in)" everywhere, we wrap it
 *   in one place.  That is Encapsulation (OOP pillar #1): hiding the Scanner
 *   detail inside this class so the rest of the code only calls readLine().
 */
public class InputHandling {

    // Scanner reads text the user types in the terminal.
    // It is private so no other class can touch it directly (Encapsulation).
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Prints a prompt, waits for the user to press Enter, and returns the
     * text they typed (with leading/trailing spaces removed).
     *
     * @param prompt  The message shown to the user before they type.
     * @return        The text the user entered, trimmed.
     */
    public String readLine(String prompt) {
        System.out.print(prompt);        // show the question / label
        return scanner.nextLine().trim(); // read the answer, strip whitespace
    }

    /**
     * Closes the Scanner when we are done reading input.
     * Always call this before the program exits to free resources.
     */
    public void close() {
        scanner.close();
    }
}
