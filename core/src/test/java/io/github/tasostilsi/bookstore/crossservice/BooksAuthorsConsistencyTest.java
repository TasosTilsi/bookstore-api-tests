package io.github.tasostilsi.bookstore.crossservice;

import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.data.RandomData;
import io.github.tasostilsi.bookstore.model.Author;
import io.github.tasostilsi.bookstore.model.Book;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.assertEquals;

@Epic("Bookstore API")
@Feature("Cross-service consistency")
@Severity(SeverityLevel.NORMAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class BooksAuthorsConsistencyTest {

    // Above every id the service serves, and distinct from each other, so both GETs must answer 404.
    private static final int UNSTORED_BOOK_ID = 9001;
    private static final int UNSTORED_AUTHOR_ID = 9002;

    @Test(groups = "crossservice")
    public void everyBookHasAuthors() {
        // Both sides are read from the service, never from a literal: the demo's data set changing
        // must red this test on the Books side, which is the point. No size is asserted on either
        // side, because only the set of groups is invariant, not the number of authors.
        List<Integer> bookIds = Api.books().list().json().getList("id");
        List<Integer> grouped = Api.authors().list().json().getList("idBook");
        assertEquals(new HashSet<>(grouped), new HashSet<>(bookIds));
    }

    @Test(groups = {"crossservice", "deviation"})
    @Feature("Contract deviations")
    public void statelessnessSpansBothResources() {
        Book book = new Book().id(UNSTORED_BOOK_ID).title(RandomData.faker().book().title());
        Author author = new Author().id(UNSTORED_AUTHOR_ID).idBook(UNSTORED_BOOK_ID)
                .firstName(RandomData.faker().name().firstName())
                .lastName(RandomData.faker().name().lastName());
        RandomData.attachSeed();

        Api.books().create(book).assertThat()
                .hasStatus(200).hasField("id", UNSTORED_BOOK_ID).hasField("title", book.getTitle());
        Api.authors().create(author).assertThat().hasStatus(200)
                .hasField("id", UNSTORED_AUTHOR_ID).hasField("idBook", UNSTORED_BOOK_ID)
                .hasField("firstName", author.getFirstName())
                .hasField("lastName", author.getLastName());

        Api.books().get(UNSTORED_BOOK_ID).assertThat().hasStatus(404);
        Api.authors().get(UNSTORED_AUTHOR_ID).assertThat().hasStatus(404);
    }
}
