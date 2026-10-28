package io.github.tasostilsi.bookstore.books;

import io.github.tasostilsi.bookstore.books.data.BooksData;
import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.github.tasostilsi.bookstore.model.Book;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static io.github.tasostilsi.bookstore.books.data.BooksData.BOOK_1_TITLE;
import static io.github.tasostilsi.bookstore.books.data.BooksData.EMPTY_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.EMPTY_OBJECT;
import static io.github.tasostilsi.bookstore.books.data.BooksData.MALFORMED_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.ZERO_DATE_DEFAULT;

@Epic("Bookstore API")
@Feature("Books update")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class BooksUpdateTest {

    @Test(groups = "contract")
    public void updateExistingBookEchoesPayload() {
        Book book = BooksData.book(1);
        ApiResponse r = Api.books().update(1, book);
        r.assertThat().hasStatus(200).isVersionedJson().hasField("id", 1)
                .hasField("title", book.getTitle()).hasField("pageCount", book.getPageCount())
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
    }

    @Test(groups = "deviation",
            description = "the path id is ignored: the echoed id is the body id, defaulting to 0 when the body omits it")
    @Feature("Contract deviations")
    public void updateSucceedsForAbsentId() {
        Book book = BooksData.bookWithoutId();
        Api.books().update(99999, book).assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", 0)
                .hasField("title", book.getTitle()).hasField("pageCount", book.getPageCount())
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    @Description("the echoed 200 is the only effect: a later GET still returns the stored book")
    public void updateDoesNotPersist() {
        Api.books().update(1, BooksData.book(1)).assertThat().hasStatus(200);
        Api.books().get(1).assertThat().hasStatus(200).hasField("title", BOOK_1_TITLE);
    }

    @Test(groups = "contract")
    public void updateWithZeroLengthBodyReturns400() {
        Api.books().update(1, EMPTY_BODY).assertThat().hasStatus(400).isProblemJsonWithoutVersion()
                .hasNonNullField("errors['']");
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    @Description("the deviation: /Books/1 answers with id 0 instead of the resource's own id."
            + " publishDate is read through JsonPath because \"0001-01-01T00:00:00\" has no offset,"
            + " so the generated OffsetDateTime cannot parse it")
    public void updateWithEmptyObjectResetsFields() {
        Api.books().update(1, EMPTY_OBJECT).assertThat().hasStatus(200).isVersionedJson()
                .hasField("id", 0).hasNullField("title").hasField("pageCount", 0)
                .hasField("publishDate", ZERO_DATE_DEFAULT);
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    @Description("an omitted field is replaced by null, so this is a replace and not a merge")
    public void updateNullsOmittedFields() {
        // A merge with book 1 would keep its description, excerpt and publishDate; a replace resets them.
        Book book = BooksData.book(1).description(null).excerpt(null).publishDate(null);
        Api.books().update(1, book).assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", 1)
                .hasField("title", book.getTitle()).hasField("pageCount", book.getPageCount())
                .hasNullField("description").hasNullField("excerpt")
                .hasField("publishDate", ZERO_DATE_DEFAULT);
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    public void updateUsesBodyIdNotPathId() {
        Book book = BooksData.book(7);
        Api.books().update(1, book).assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", 7)
                .hasField("title", book.getTitle()).hasField("pageCount", book.getPageCount())
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
    }

    @Test(groups = "contract")
    public void updateWithMalformedBodyReturns400() {
        Api.books().update(1, MALFORMED_BODY).assertThat().hasStatus(400).isProblemJsonWithoutVersion()
                .hasNonNullField("errors['$.title']");
    }
}
