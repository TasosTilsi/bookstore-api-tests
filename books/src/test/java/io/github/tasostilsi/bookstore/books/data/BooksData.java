package io.github.tasostilsi.bookstore.books.data;

public final class BooksData {

    public static final String BOOK_1_TITLE = "Book 1";
    public static final int BOOK_1_PAGE_COUNT = 100;
    public static final int MAX_BOOK_ID = 200;
    public static final String ZERO_DATE_DEFAULT = "0001-01-01T00:00:00";

    public static final String FULL_BOOK =
            "{\"id\":5000,\"title\":\"Book 5000\",\"description\":\"d\",\"pageCount\":10,"
                    + "\"excerpt\":\"e\",\"publishDate\":\"2026-01-01T00:00:00Z\"}";
    public static final String NO_ID_BOOK =
            "{\"title\":\"no-id\",\"pageCount\":1,\"publishDate\":\"2026-01-01T00:00:00Z\"}";
    public static final String EMPTY_OBJECT = "{}";
    public static final String EMPTY_BODY = "";
    public static final String MALFORMED_BODY = "{\"title\": ";
    public static final String ARRAY_BODY = "[]";
    public static final String UNICODE_TITLE = "Ångström 日本語 😀";
    public static final String UNKNOWN_FIELD_BOOK =
            "{\"id\":5000,\"title\":\"Book 5000\",\"description\":\"d\",\"pageCount\":10,"
                    + "\"excerpt\":\"e\",\"publishDate\":\"2026-01-01T00:00:00Z\",\"nonsense\":\"drop-me\"}";

    private BooksData() {
    }

    public static String longTitle() {
        return "x".repeat(10_000);
    }
}
