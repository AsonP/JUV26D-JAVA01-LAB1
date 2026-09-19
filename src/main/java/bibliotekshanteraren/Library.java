package bibliotekshanteraren;

public class Library {

    private static final int CAPACITY = 25;

    private final Book[] books = new Book[CAPACITY];
    private int bookCount = 0;

    private final Member[] members = new Member[CAPACITY];
    private int memberCount = 0;

    private final Loan[] loans = new Loan[CAPACITY];
    private int loanCount = 0;

    private int nextMemberId = 1;

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

        if (bookCount >= CAPACITY) {
            throw new LibraryException(LibraryException.ErrorType.CAPACITY_EXCEEDED,
                    "Biblioteket är fullt (max " + CAPACITY + " böcker). Kan inte lägga till fler.");
        }

        books[bookCount] = book;
        bookCount++;
    }

    public int registerMember(String name) throws LibraryException {
        if (name == null || name.isBlank()) {
            throw new LibraryException(LibraryException.ErrorType.INVALID_INPUT,
                    "Medlemsnamn måste vara ifyllt.");
        }

        if (memberCount >= CAPACITY) {
            throw new LibraryException(LibraryException.ErrorType.CAPACITY_EXCEEDED,
                    "Max antal medlemmar (" + CAPACITY + ") är uppnått. Kan inte registrera fler.");
        }

        int id = nextMemberId;
        Member member = new Member(name, id);
        members[memberCount] = member;
        memberCount++;
        nextMemberId++;
        return id;
    }

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

        if (loanCount >= CAPACITY) {
            throw new LibraryException(LibraryException.ErrorType.CAPACITY_EXCEEDED,
                    "Max antal samtidiga lån (" + CAPACITY + ") är uppnått.");
        }

        loans[loanCount] = new Loan(book, member);
        loanCount++;
        member.increaseLoanCount();
    }

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

    private Book[] getBooksSortedByTitle() {
        Book[] sorted = new Book[bookCount];
        for (int i = 0; i < bookCount; i++) {
            sorted[i] = books[i];
        }

        // Selection sort — hittar minsta (alfabetiskt tidigaste) titeln
        // i den osorterade delen och byter plats med den, ett steg i taget.
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

    private void removeLoanAt(int index) {
        for (int i = index; i < loanCount - 1; i++) {
            loans[i] = loans[i + 1];
        }
        loans[loanCount - 1] = null;
        loanCount--;
    }
}

