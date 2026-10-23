
import java.util.Scanner;
public class InputHandling {

        private final Scanner scanner = new Scanner(System.in);

        public String readLine(String prompt) {
            System.out.print(prompt);
            return scanner.nextLine().trim();
        }

        public void close() {
            scanner.close();
        }
    }

