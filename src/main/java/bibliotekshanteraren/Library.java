package bibliotekshanteraren;

public class Library {

    private static final int CAPACITY = 3;

    private Book[] books = new Book[CAPACITY];
    private int bookCount = 0;

    private Member[] members = new Member[CAPACITY];
    private int memberCount = 0;

    private Loan[] loans = new Loan[CAPACITY];
    private int loanCount = 0;

    private int nextMemberId = 1;

    /**
     * Lägger till en ny bok i biblioteket.
     * Kastar LibraryException vid tomma fält eller om ISBN redan finns.
     */
    public void addBook(Book book) throws LibraryException {
        if (book.title() == null || book.title().isBlank()
                || book.author() == null || book.author().isBlank()
                || book.isbn() == null || book.isbn().isBlank()) {
            throw new LibraryException(LibraryException.ErrorType.INVALID_INPUT,
                    "Titel, författare och ISBN måste vara ifyllda.");
        }

        if (findBookIndexByIsbn(book.isbn()) != -1) {
            throw new LibraryException(LibraryException.ErrorType.DUPLICATE_ISBN,
                    "En bok med ISBN " + book.isbn() + " finns redan.");
        }

        if (bookCount >= books.length) {
            books = growBookArray();
            System.out.println("Bokarrayen var full — kapaciteten utökades till " + books.length + ".");
        }

        books[bookCount] = book;
        bookCount++;
    }

    /**
     * Registrerar en ny medlem och tilldelar automatiskt ett unikt ID.
     * Returnerar det tilldelade ID:t.
     */
    public int registerMember(String name) throws LibraryException {
        if (name == null || name.isBlank()) {
            throw new LibraryException(LibraryException.ErrorType.INVALID_INPUT,
                    "Medlemsnamn måste vara ifyllt.");
        }

        if (memberCount >= members.length) {
            members = growMemberArray();
            System.out.println("Medlemsarrayen var full — kapaciteten utökades till " + members.length + ".");
        }

        int id = nextMemberId;
        Member member = new Member(name, id);
        members[memberCount] = member;
        memberCount++;
        nextMemberId++;
        return id;
    }

    /**
     * Lånar ut en bok till en medlem, givet boktitel och medlems-ID.
     * Kontrollerar att boken och medlemmen finns, att boken inte redan är
     * utlånad, och att medlemmen inte nått sin lånegräns.
     */
    public void borrowBook(String title, int memberId) throws LibraryException {
        int bookIndex = findBookIndexByTitle(title);
        if (bookIndex == -1) {
            throw new LibraryException(LibraryException.ErrorType.BOOK_NOT_FOUND,
                    "Ingen bok hittades med titeln: " + title);
        }

        int memberIndex = findMemberIndex(memberId);
        if (memberIndex == -1) {
            throw new LibraryException(LibraryException.ErrorType.MEMBER_NOT_FOUND,
                    "Ingen medlem hittades med ID: " + memberId);
        }

        Book book = books[bookIndex];
        Member member = members[memberIndex];

        if (findLoanIndexByBook(book) != -1) {
            throw new LibraryException(LibraryException.ErrorType.BOOK_ALREADY_BORROWED,
                    "Boken \"" + book.title() + "\" är redan utlånad.");
        }

        if (!member.canBorrowMore()) {
            throw new LibraryException(LibraryException.ErrorType.LOAN_LIMIT_EXCEEDED,
                    "Medlemmen " + member.getName() + " har redan nått max antal lån.");
        }

        if (loanCount >= loans.length) {
            loans = growLoanArray();
            System.out.println("Lånearrayen var full — kapaciteten utökades till " + loans.length + ".");
        }

        loans[loanCount] = new Loan(book, member);
        loanCount++;
        member.increaseLoanCount();
    }

    /**
     * Lämnar tillbaka en bok, givet boktitel.
     * Kontrollerar att boken finns och faktiskt är utlånad.
     */
    public void returnBook(String title) throws LibraryException {
        int bookIndex = findBookIndexByTitle(title);
        if (bookIndex == -1) {
            throw new LibraryException(LibraryException.ErrorType.BOOK_NOT_FOUND,
                    "Ingen bok hittades med titeln: " + title);
        }

        Book book = books[bookIndex];
        int loanIndex = findLoanIndexByBook(book);

        if (loanIndex == -1) {
            throw new LibraryException(LibraryException.ErrorType.BOOK_NOT_BORROWED,
                    "Boken \"" + book.title() + "\" är inte utlånad och kan därför inte lämnas tillbaka.");
        }

        Member member = loans[loanIndex].member();
        member.decreaseLoanCount();

        removeLoanAt(loanIndex);
    }

    /**
     * Söker efter böcker vars titel eller författare innehåller söktermen
     * (skiftlägesokänsligt). Skriver resultatet direkt till konsolen.
     */
    public void search(String query) {
        String lowerQuery = query.toLowerCase();
        boolean found = false;

        System.out.println("Sökresultat för \"" + query + "\":");
        for (int i = 0; i < bookCount; i++) {
            Book b = books[i];
            if (b.title().toLowerCase().contains(lowerQuery)
                    || b.author().toLowerCase().contains(lowerQuery)) {
                String status = (findLoanIndexByBook(b) != -1) ? "Utlånad" : "Tillgänglig";
                System.out.println("- " + capitalizeWords(b.title()) + " av " + capitalizeWords(b.author())
                        + " (ISBN: " + b.isbn() + ") [" + status + "]");
                found = true;
            }
        }

        if (!found) {
            System.out.println("Inga böcker matchade sökningen.");
        }
    }

    /**
     * Visar samtliga böcker, sorterade i bokstavsordning på titel,
     * tillsammans med status (utlånad/tillgänglig och till vem).
     */
    public void listAllBooks() {
        Book[] sortedBooks = getBooksSortedByTitle();

        System.out.println("Alla böcker (sorterade på titel):");
        for (int i = 0; i < sortedBooks.length; i++) {
            Book b = sortedBooks[i];
            int loanIndex = findLoanIndexByBook(b);
            String status = (loanIndex != -1)
                    ? "Utlånad till " + capitalizeWords(loans[loanIndex].member().getName())
                    : "Tillgänglig";
            System.out.println("- " + capitalizeWords(b.title()) + " av " + capitalizeWords(b.author())
                    + " (ISBN: " + b.isbn() + ") [" + status + "]");
        }
    }

    /**
     * Hittar och visar den medlem som just nu har flest aktiva lån.
     */
    public void showMemberWithMostLoans() {
        if (memberCount == 0) {
            System.out.println("Inga medlemmar är registrerade än.");
            return;
        }

        Member topMember = members[0];

        for (int i = 1; i < memberCount; i++) {
            if (members[i].getActiveLoans() > topMember.getActiveLoans()) {
                topMember = members[i];
            }
        }

        if (topMember.getActiveLoans() == 0) {
            System.out.println("Ingen medlem har några aktiva lån just nu.");
            return;
        }

        System.out.println("Medlem med flest aktiva lån: " + capitalizeWords(topMember.getName())
                + " (ID: " + topMember.getId() + ") med " + topMember.getActiveLoans() + " lån.");
    }

    // Skapar en ny bokarray med dubbel storlek och kopierar över befintliga element manuellt.
    private Book[] growBookArray() {
        Book[] newArray = new Book[books.length * 2];
        for (int i = 0; i < books.length; i++) {
            newArray[i] = books[i];
        }
        return newArray;
    }

    // Skapar en ny medlemsarray med dubbel storlek och kopierar över befintliga element manuellt.
    private Member[] growMemberArray() {
        Member[] newArray = new Member[members.length * 2];
        for (int i = 0; i < members.length; i++) {
            newArray[i] = members[i];
        }
        return newArray;
    }

    // Skapar en ny lånearray med dubbel storlek och kopierar över befintliga element manuellt.
    private Loan[] growLoanArray() {
        Loan[] newArray = new Loan[loans.length * 2];
        for (int i = 0; i < loans.length; i++) {
            newArray[i] = loans[i];
        }
        return newArray;
    }

    // Selection sort — hittar minsta (alfabetiskt tidigaste) titeln
    // i den osorterade delen och byter plats med den, ett steg i taget.
    private Book[] getBooksSortedByTitle() {
        Book[] sorted = new Book[bookCount];
        for (int i = 0; i < bookCount; i++) {
            sorted[i] = books[i];
        }

        for (int i = 0; i < sorted.length - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < sorted.length; j++) {
                if (sorted[j].title().compareToIgnoreCase(sorted[minIndex].title()) < 0) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                Book temp = sorted[i];
                sorted[i] = sorted[minIndex];
                sorted[minIndex] = temp;
            }
        }

        return sorted;
    }

    // Formaterar text för utskrift: första bokstaven i varje ord versal, resten gemener.
    // Ändrar inte det faktiskt lagrade värdet i Book/Member.
    private String capitalizeWords(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        String[] words = text.trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            result.append(Character.toUpperCase(word.charAt(0)));
            result.append(word.substring(1).toLowerCase());
            if (i < words.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }

    private int findBookIndexByTitle(String title) {
        for (int i = 0; i < bookCount; i++) {
            if (books[i].title().equalsIgnoreCase(title)) {
                return i;
            }
        }
        return -1;
    }

    private int findBookIndexByIsbn(String isbn) {
        for (int i = 0; i < bookCount; i++) {
            if (books[i].isbn().equals(isbn)) {
                return i;
            }
        }
        return -1;
    }

    private int findMemberIndex(int memberId) {
        for (int i = 0; i < memberCount; i++) {
            if (members[i].getId() == memberId) {
                return i;
            }
        }
        return -1;
    }

    private int findLoanIndexByBook(Book book) {
        for (int i = 0; i < loanCount; i++) {
            if (loans[i].book().equals(book)) {
                return i;
            }
        }
        return -1;
    }

    // Tar bort lånet på angivet index och skiftar efterföljande element
    // ett steg åt vänster, så att det inte blir ett "hål" i arrayen.
    private void removeLoanAt(int index) {
        for (int i = index; i < loanCount - 1; i++) {
            loans[i] = loans[i + 1];
        }
        loans[loanCount - 1] = null;
        loanCount--;
    }
}