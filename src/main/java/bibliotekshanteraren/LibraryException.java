package bibliotekshanteraren;

public class LibraryException extends Exception {

    public enum ErrorType {
        BOOK_NOT_FOUND,
        MEMBER_NOT_FOUND,
        BOOK_ALREADY_BORROWED,
        BOOK_NOT_BORROWED,
        LOAN_LIMIT_EXCEEDED,
        CAPACITY_EXCEEDED,
        INVALID_INPUT,
        DUPLICATE_ISBN
    }

    private final ErrorType errorType;

    public LibraryException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}