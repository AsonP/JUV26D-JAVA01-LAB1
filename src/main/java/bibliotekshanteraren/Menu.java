package bibliotekshanteraren;

import java.util.Scanner;

public class Menu {

    private final Library library;
    private final Scanner scanner;

    public Menu(Library library) {
        this.library = library;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        boolean firstRun = true;

        while (running) {

            if (!firstRun) {
                clearScreen();
            }
            firstRun = false;

            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    addBook();
                    pause();
                }
                case "2" -> {
                    registerMember();
                    pause();
                }
                case "3" -> {
                    borrowBook();
                    pause();
                }
                case "4" -> {
                    returnBook();
                    pause();
                }
                case "5" -> {
                    searchBook();
                    pause();
                }
                case "6" -> {
                    listBooks();
                    pause();
                }
                case "e", "E" -> {
                    running = false;
                    System.out.println("Programmet avslutas...");
                }
                default -> {
                    System.out.println("Ogiltigt val, försök igen.");
                    pause();
                }
            }
        }
    }

    private void printMenu() {
        System.out.println("Bibliotekshanteraren");
        System.out.println("====================");
        System.out.println("1. Lägg till bok");
        System.out.println("2. Registrera medlem");
        System.out.println("3. Låna bok");
        System.out.println("4. Lämna tillbaka bok");
        System.out.println("5. Sök bok (titel eller författare)");
        System.out.println("6. Visa alla böcker och status");
        System.out.println("e. Avsluta");
        System.out.print("Välj ett alternativ: ");
    }

    private void clearScreen() {
        for (int i = 0; i < 100; i++) {
            System.out.println();
        }
    }

    private void pause() {
        System.out.println("\nTryck Enter för att fortsätta...");
        scanner.nextLine();
    }

    private void addBook() {
        System.out.print("Titel: ");
        String title = scanner.nextLine();
        System.out.print("Författare: ");
        String author = scanner.nextLine();
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine();

        try {
            library.addBook(new Book(title, author, isbn));
            System.out.println("Bok tillagd!");
        } catch (LibraryException e) {
            System.out.println("Fel: " + e.getMessage());
        }
    }

    private void registerMember() {
        System.out.print("Medlemsnamn: ");
        String name = scanner.nextLine();

        try {
            int id = library.registerMember(name);
            System.out.println("Medlem registrerad! Tilldelat ID: " + id);
        } catch (LibraryException e) {
            System.out.println("Fel: " + e.getMessage());
        }
    }

    private void borrowBook() {
        System.out.print("Boktitel att låna: ");
        String title = scanner.nextLine();
        System.out.print("Medlems-ID: ");
        String input = scanner.nextLine();

        int memberId;
        try {
            memberId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Fel: Medlems-ID måste vara ett heltal.");
            return;
        }

        try {
            library.borrowBook(title, memberId);
            System.out.println("Boken har lånats ut!");
        } catch (LibraryException e) {
            System.out.println("Fel: " + e.getMessage());
        }
    }

    private void returnBook() {
        System.out.print("Boktitel att lämna tillbaka: ");
        String title = scanner.nextLine();

        try {
            library.returnBook(title);
            System.out.println("Boken har lämnats tillbaka!");
        } catch (LibraryException e) {
            System.out.println("Fel: " + e.getMessage());
        }
    }

    private void searchBook() {
        System.out.print("Sökterm: ");
        String query = scanner.nextLine();

        library.search(query);
    }

    private void listBooks() {
        library.listAllBooks();
    }
}