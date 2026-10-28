package io.github.tasostilsi.bookstore.crossservice;

import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.data.RandomData;
import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.github.tasostilsi.bookstore.model.Author;
import io.github.tasostilsi.bookstore.model.Book;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;

@Epic("Bookstore API")
@Feature("Cross-service consistency")
@Severity(SeverityLevel.NORMAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class BooksAuthorsConsistencyTest {

    // Sent ids, not asserted ids: a correct service assigns the identity and returns it in Location.
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

    @Test(groups = {"crossservice", "contract", "defect"})
    @Feature("Contract defects")
    @Description("a created book and the author that references it are both readable afterwards")
    public void createdResourcesAreVisibleAcrossBothResources() {
        Book book = new Book().id(UNSTORED_BOOK_ID).title(RandomData.faker().book().title());
        Author author = new Author().id(UNSTORED_AUTHOR_ID)
                .firstName(RandomData.faker().name().firstName())
                .lastName(RandomData.faker().name().lastName());
        RandomData.attachSeed();

        ApiResponse bookCreated = Api.books().create(book);
        bookCreated.assertThat()
                .hasStatus(201, "precondition: a correct service stores the book it creates and returns its Location")
                .hasHeader("Location");
        int bookId = bookCreated.locationId();

        // The foreign key comes from the Location, not from the id that was sent: the server owns identity.
        author.idBook(bookId);
        ApiResponse authorCreated = Api.authors().create(author);
        authorCreated.assertThat()
                .hasStatus(201, "precondition: a correct service stores the author it creates and returns its Location")
                .hasHeader("Location");
        int authorId = authorCreated.locationId();

        Api.books().get(bookId).assertThat().hasStatus(200).hasField("id", bookId);
        Api.authors().get(authorId).assertThat().hasStatus(200).hasField("id", authorId)
                .hasField("idBook", bookId);
        assertThat(Api.authors().byBook(bookId).json().getList("id"), hasItem(authorId));
    }
}
