package email;

import custom_exception.InvalidInputException;
import java.util.Scanner;

public class InputHandler {

    Scanner scanner = new Scanner(System.in);

    public String readString(String text) {
        System.out.print("\u001B[33m" + "\n" + text + "\u001B[0m");
        return scanner.nextLine().trim();
    }

    public Byte readByte(String text) throws InvalidInputException {
        System.out.print("\u001B[33m" + "\n" + text + "\u001B[0m");
        if (scanner.hasNextByte()) {
            Byte inputValue = scanner.nextByte();
            scanner.nextLine();
            return inputValue;
        } else {
            scanner.nextLine(); 
            throw new InvalidInputException("Invalid input.");
        }
    }

    public Long readLong(String text) throws InvalidInputException {
        System.out.print("\u001B[33m" + "\n" + text + "\u001B[0m");
        if (scanner.hasNextLong()) {
            Long inputValue = scanner.nextLong();
            scanner.nextLine();
            return inputValue;
        } else {
            scanner.nextLine();
            throw new InvalidInputException("Invalid input.");
        }
    }

    public void close() {
        scanner.close();
    }
}
