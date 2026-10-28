package io.github.tasostilsi.bookstore.books.data;

import io.github.tasostilsi.bookstore.data.RandomData;
import io.github.tasostilsi.bookstore.model.Book;
import io.qameta.allure.Step;
import net.datafaker.Faker;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public final class BooksData {

    public static final String BOOK_1_TITLE = "Book 1";
    public static final int BOOK_1_PAGE_COUNT = 100;
    public static final int MAX_BOOK_ID = 200;
    public static final String ZERO_DATE_DEFAULT = "0001-01-01T00:00:00";

    public static final String EMPTY_OBJECT = "{}";
    public static final String EMPTY_BODY = "";
    public static final String MALFORMED_BODY = "{\"title\": ";
    public static final String ARRAY_BODY = "[]";
    public static final String UNICODE_TITLE = "Ångström 日本語 😀";
    // The model has no field the service could drop, so this one stays a literal.
    public static final String UNKNOWN_FIELD_BOOK =
            "{\"id\":5000,\"title\":\"Book 5000\",\"description\":\"d\",\"pageCount\":10,"
                    + "\"excerpt\":\"e\",\"publishDate\":\"2026-01-01T00:00:00Z\",\"nonsense\":\"drop-me\"}";

    private BooksData() {
    }

    /** An id above the stored range, so a later GET of it cannot read a book the service already serves. */
    public static Book book() {
        return book(RandomData.faker().number().numberBetween(MAX_BOOK_ID + 1, 9000));
    }

    @Step("generate a random book")
    public static Book book(int id) {
        Faker faker = RandomData.faker();
        RandomData.attachSeed();
        return new Book()
                .id(id)
                .title(faker.book().title())
                .description(faker.lorem().sentence())
                .excerpt(faker.lorem().sentence())
                .pageCount(faker.number().numberBetween(1, 2000))
                // The service keeps 7 fractional digits, so a nanosecond timestamp would not round-trip.
                .publishDate(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS));
    }

    public static Book bookWithoutId() {
        return book().id(null);
    }

    public static String longTitle() {
        return "x".repeat(10_000);
    }
}
