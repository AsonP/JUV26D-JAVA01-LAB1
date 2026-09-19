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
        if (bookCount >= CAPACITY) {
            throw new LibraryException(LibraryException.ErrorType.CAPACITY_EXCEEDED,
                    "Biblioteket är fullt (max " + CAPACITY + " böcker). Kan inte lägga till fler.");
        }
        books[bookCount] = book;
        bookCount++;
    }

    public int registerMember(String name) throws LibraryException {
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
                System.out.println("- " + b.title() + " av " + b.author() + " (ISBN: " + b.isbn() + ") [" + status + "]");
                found = true;
            }
        }

        if (!found) {
            System.out.println("Inga böcker matchade sökningen.");
        }
    }

    public void listAllBooks() {
        System.out.println("Alla böcker:");
        for (int i = 0; i < bookCount; i++) {
            Book b = books[i];
            int loanIndex = findLoanIndexByBook(b);
            String status = (loanIndex != -1)
                    ? "Utlånad till " + loans[loanIndex].member().getName()
                    : "Tillgänglig";
            System.out.println("- " + b.title() + " av " + b.author() + " (ISBN: " + b.isbn() + ") [" + status + "]");
        }
    }

    private int findBookIndexByTitle(String title) {
        for (int i = 0; i < bookCount; i++) {
            if (books[i].title().equalsIgnoreCase(title)) {
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