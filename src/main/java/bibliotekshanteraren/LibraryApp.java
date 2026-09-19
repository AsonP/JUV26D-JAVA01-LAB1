package bibliotekshanteraren;

public class LibraryApp {
    public static void main(String[] args) {
        Library library = new Library();
        Menu menu = new Menu(library);
        menu.start();
    }
}
