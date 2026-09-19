package bibliotekshanteraren;

public class Member {

    private static final int MAX_LOANS = 3;

    private final int id;
    private String name;
    private int activeLoans;

    public Member(String name, int id) {
        this.name = name;
        this.id = id;
        this.activeLoans = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getActiveLoans() {
        return activeLoans;
    }

    public boolean canBorrowMore() {
        return activeLoans < MAX_LOANS;
    }

    public void increaseLoanCount() {
        activeLoans++;
    }

    public void decreaseLoanCount() {
        if (activeLoans > 0) {
            activeLoans--;
        }
    }
}