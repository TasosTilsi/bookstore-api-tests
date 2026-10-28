package io.github.tasostilsi.bookstore.books;

import io.github.tasostilsi.bookstore.books.data.BooksData;
import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.github.tasostilsi.bookstore.model.Book;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;

import static io.github.tasostilsi.bookstore.books.data.BooksData.ARRAY_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.EMPTY_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.EMPTY_OBJECT;
import static io.github.tasostilsi.bookstore.books.data.BooksData.MALFORMED_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.UNICODE_TITLE;
import static io.github.tasostilsi.bookstore.books.data.BooksData.UNKNOWN_FIELD_BOOK;
import static io.github.tasostilsi.bookstore.books.data.BooksData.ZERO_DATE_DEFAULT;

@Epic("Bookstore API")
@Feature("Books create")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class BooksCreateTest {

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    public void createEchoesPayloadWith200AndNoLocation() {
        Book book = BooksData.book();
        ApiResponse r = Api.books().create(book);
        r.assertThat().hasStatus(200).isVersionedJson().hasNoHeader("Location")
                .hasField("id", book.getId()).hasField("title", book.getTitle())
                .hasField("pageCount", book.getPageCount())
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    public void createDoesNotPersist() {
        Book book = BooksData.book();
        Api.books().create(book).assertThat().hasStatus(200)
                .hasField("id", book.getId()).hasField("title", book.getTitle())
                .hasField("pageCount", book.getPageCount())
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
        Api.books().get(book.getId()).assertThat().hasStatus(404);
    }

    @Test(groups = {"deviation", "validationGap"})
    @Feature("Contract deviations")
    @Feature("Validation gaps")
    public void createWithEmptyObjectReturnsDefaults() {
        Api.books().create(EMPTY_OBJECT).assertThat().hasStatus(200).isVersionedJson()
                .hasField("id", 0).hasNullField("title").hasField("pageCount", 0)
                .hasNullField("description").hasNullField("excerpt")
                .hasField("publishDate", ZERO_DATE_DEFAULT);
    }

    @Test(groups = {"deviation", "validationGap"})
    @Feature("Contract deviations")
    @Feature("Validation gaps")
    public void createWithoutIdReturnsIdZero() {
        Book book = BooksData.bookWithoutId();
        Api.books().create(book).assertThat().hasStatus(200).isVersionedJson().hasField("id", 0)
                .hasField("title", book.getTitle()).hasField("pageCount", book.getPageCount())
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
    }

    @Test(groups = "contract")
    public void createWithZeroLengthBodyReturns400() {
        // An empty body fails before model binding, so the error map is keyed on the empty string.
        Api.books().create(EMPTY_BODY).assertThat()
                .hasStatus(400).isProblemJsonWithoutVersion().hasNonNullField("errors['']");
    }

    @Test(groups = "contract")
    public void createWithMalformedJsonReturns400() {
        Api.books().create(MALFORMED_BODY).assertThat()
                .hasStatus(400).isProblemJsonWithoutVersion().hasNonNullField("errors['$.title']");
    }

    @Test(groups = "contract")
    public void createWithArrayBodyReturns400() {
        Api.books().create(ARRAY_BODY).assertThat()
                .hasStatus(400).isProblemJsonWithoutVersion();
    }

    @Test(groups = "contract")
    public void createWithWrongFieldTypeReturns400() {
        Api.books().create("{\"title\":\"t\",\"pageCount\":\"many\"}").assertThat()
                .hasStatus(400).isProblemJsonWithoutVersion().hasNonNullField("errors['$.pageCount']");
    }

    @Test(groups = "contract")
    public void createWithUnparseableDateReturns400() {
        Api.books().create("{\"title\":\"t\",\"publishDate\":\"yesterday\"}").assertThat()
                .hasStatus(400).isProblemJsonWithoutVersion().hasNonNullField("errors['$.publishDate']");
    }

    @Test(groups = "contract")
    public void createWithPlainTextContentTypeReturns415() {
        // 415 is chosen before the body is bound, so the payload itself is irrelevant here.
        Api.books().create(EMPTY_OBJECT, "text/plain").assertThat().hasStatus(415).isProblemJsonWithoutVersion();
    }

    @Test(groups = {"deviation", "validationGap"})
    @Feature("Contract deviations")
    @Feature("Validation gaps")
    public void createSilentlyDropsUnknownFields() {
        Api.books().create(UNKNOWN_FIELD_BOOK).assertThat()
                .hasStatus(200).isVersionedJson().hasNoField("nonsense")
                .hasField("id", 5000).hasField("title", "Book 5000").hasField("pageCount", 10)
                .hasField("description", "d").hasField("excerpt", "e")
                .hasInstantField("publishDate", OffsetDateTime.parse("2026-01-01T00:00:00Z"));
    }

    @Test(groups = "validationGap")
    @Feature("Validation gaps")
    public void createAcceptsNegativePageCount() {
        Book book = BooksData.book().pageCount(-5);
        Api.books().create(book).assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", book.getId())
                .hasField("title", book.getTitle()).hasField("pageCount", -5)
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
    }

    @Test(groups = "validationGap")
    @Feature("Validation gaps")
    public void createAcceptsVeryLongTitle() {
        Book book = BooksData.book().title(BooksData.longTitle());
        Api.books().create(book).assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", book.getId())
                .hasField("title", BooksData.longTitle()).hasField("pageCount", book.getPageCount())
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
    }

    @Test(groups = "contract")
    public void createAcceptsUnicodeTitle() {
        Book book = BooksData.book().title(UNICODE_TITLE);
        Api.books().create(book).assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", book.getId())
                .hasField("title", UNICODE_TITLE).hasField("pageCount", book.getPageCount())
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
    }

    @Test(groups = "contract")
    public void createRejectsPageCountAboveInt32() {
        Api.books().create("{\"title\":\"t\",\"pageCount\":2147483648}").assertThat()
                .hasStatus(400).isProblemJsonWithoutVersion().hasNonNullField("errors['$.pageCount']");
    }
}
