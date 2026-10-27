package io.github.tasostilsi.bookstore.config;

public final class ApiPaths {

    public static final String BASE_PATH = "/api/v1";
    public static final String BOOKS = BASE_PATH + "/Books";
    public static final String AUTHORS = BASE_PATH + "/Authors";
    public static final String AUTHORS_BY_BOOK = BASE_PATH + "/Authors/authors/books";

    private ApiPaths() {
    }

    public static String book(int id) {
        return book(Integer.toString(id));
    }

    public static String book(String rawId) {
        return BOOKS + "/" + rawId;
    }

    public static String author(int id) {
        return author(Integer.toString(id));
    }

    public static String author(String rawId) {
        return AUTHORS + "/" + rawId;
    }

    public static String authorsByBook(int bookId) {
        return authorsByBook(Integer.toString(bookId));
    }

    public static String authorsByBook(String rawBookId) {
        return AUTHORS_BY_BOOK + "/" + rawBookId;
    }
}
