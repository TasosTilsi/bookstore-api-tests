package io.github.tasostilsi.bookstore.books;

import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.books.data.BooksData;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static io.github.tasostilsi.bookstore.books.data.BooksData.ARRAY_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.EMPTY_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.EMPTY_OBJECT;
import static io.github.tasostilsi.bookstore.books.data.BooksData.FULL_BOOK;
import static io.github.tasostilsi.bookstore.books.data.BooksData.MALFORMED_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.NO_ID_BOOK;
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
        Api.books().create(FULL_BOOK).assertThat().hasStatus(200).hasNoHeader("Location")
                .hasField("id", 5000).hasField("title", "Book 5000").hasField("pageCount", 10)
                .hasField("description", "d").hasField("excerpt", "e")
                .hasField("publishDate", "2026-01-01T00:00:00Z");
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    public void createDoesNotPersist() {
        Api.books().create(FULL_BOOK).assertThat().hasStatus(200).hasField("id", 5000);
        Api.books().get(5000).assertThat().hasStatus(404);
    }

    @Test(groups = {"deviation", "validationGap"})
    @Feature("Contract deviations")
    @Feature("Validation gaps")
    public void createWithEmptyObjectReturnsDefaults() {
        Api.books().create(EMPTY_OBJECT).assertThat().hasStatus(200)
                .hasField("id", 0).hasNullField("title").hasField("pageCount", 0)
                .hasNullField("description").hasNullField("excerpt")
                .hasField("publishDate", ZERO_DATE_DEFAULT);
    }

    @Test(groups = {"deviation", "validationGap"})
    @Feature("Contract deviations")
    @Feature("Validation gaps")
    public void createWithoutIdReturnsIdZero() {
        Api.books().create(NO_ID_BOOK).assertThat().hasStatus(200).hasField("id", 0)
                .hasField("title", "no-id");
    }

    @Test(groups = "contract")
    public void createWithZeroLengthBodyReturns400() {
        Api.books().create(EMPTY_BODY).assertThat().hasStatus(400).isProblemJson();
    }

    @Test(groups = "contract")
    public void createWithMalformedJsonReturns400() {
        Api.books().create(MALFORMED_BODY).assertThat().hasStatus(400).isProblemJson();
    }

    @Test(groups = "contract")
    public void createWithArrayBodyReturns400() {
        Api.books().create(ARRAY_BODY).assertThat().hasStatus(400).isProblemJson();
    }

    @Test(groups = "contract")
    public void createWithWrongFieldTypeReturns400() {
        Api.books().create("{\"title\":\"t\",\"pageCount\":\"many\"}").assertThat()
                .hasStatus(400).isProblemJson().hasNonNullField("errors");
    }

    @Test(groups = "contract")
    public void createWithUnparseableDateReturns400() {
        Api.books().create("{\"title\":\"t\",\"publishDate\":\"yesterday\"}").assertThat()
                .hasStatus(400).isProblemJson().hasNonNullField("errors");
    }

    @Test(groups = "contract")
    public void createWithPlainTextContentTypeReturns415() {
        Api.books().create(FULL_BOOK, "text/plain").assertThat().hasStatus(415).isProblemJson();
    }

    @Test(groups = {"deviation", "validationGap"})
    @Feature("Contract deviations")
    @Feature("Validation gaps")
    public void createSilentlyDropsUnknownFields() {
        Api.books().create(UNKNOWN_FIELD_BOOK).assertThat().hasStatus(200).hasNoField("nonsense")
                .hasField("id", 5000);
    }

    @Test(groups = "validationGap")
    @Feature("Validation gaps")
    public void createAcceptsNegativePageCount() {
        Api.books().create("{\"title\":\"t\",\"pageCount\":-5}").assertThat()
                .hasStatus(200).hasField("pageCount", -5);
    }

    @Test(groups = "validationGap")
    @Feature("Validation gaps")
    public void createAcceptsVeryLongTitle() {
        Api.books().create("{\"title\":\"" + BooksData.longTitle() + "\"}").assertThat()
                .hasStatus(200).hasField("title", BooksData.longTitle());
    }

    @Test(groups = "contract")
    public void createAcceptsUnicodeTitle() {
        Api.books().create("{\"title\":\"" + UNICODE_TITLE + "\"}").assertThat()
                .hasStatus(200).hasField("title", UNICODE_TITLE);
    }

    @Test(groups = "contract")
    public void createRejectsPageCountAboveInt32() {
        Api.books().create("{\"title\":\"t\",\"pageCount\":2147483648}").assertThat()
                .hasStatus(400).isProblemJson();
    }
}
