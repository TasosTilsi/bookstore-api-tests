package io.github.tasostilsi.bookstore.authors.data;

import io.github.tasostilsi.bookstore.data.RandomData;
import io.github.tasostilsi.bookstore.model.Author;
import io.qameta.allure.Step;
import net.datafaker.Faker;

public final class AuthorsData {

    public static final int STABLE_AUTHOR_ID = 1;

    // The service serves ids up to a few hundred; a lower bound of 7000 keeps a GET 404.
    private static final int FIRST_UNSTORED_ID = 7000;
    private static final int MAX_BOOK_ID = 200;

    private AuthorsData() {
    }

    public static Author author() {
        return author(RandomData.faker().number().numberBetween(FIRST_UNSTORED_ID, 9000));
    }

    @Step("generate a random author")
    public static Author author(int id) {
        Faker faker = RandomData.faker();
        RandomData.attachSeed();
        return new Author()
                .id(id)
                .idBook(faker.number().numberBetween(1, MAX_BOOK_ID + 1))
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName());
    }

    public static Author authorWithoutIdBook() {
        return author().id(null).idBook(null);
    }
}
