package io.github.tasostilsi.bookstore.authors.data;

public final class AuthorsData {

    public static final int STABLE_AUTHOR_ID = 1;

    public static final String AUTHOR_WITH_ID =
            "{\"id\":7000,\"idBook\":1,\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}";
    public static final String AUTHOR_NO_ID_BOOK = "{\"firstName\":\"nobook\",\"lastName\":\"default\"}";
    public static final String AUTHOR_FOREIGN_KEY_GAP =
            "{\"id\":7001,\"idBook\":99999,\"firstName\":\"gap\",\"lastName\":\"gap\"}";

    private AuthorsData() {
    }
}
