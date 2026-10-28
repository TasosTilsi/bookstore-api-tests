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

import static io.github.tasostilsi.bookstore.books.data.BooksData.ARRAY_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.EMPTY_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.EMPTY_OBJECT;
import static io.github.tasostilsi.bookstore.books.data.BooksData.MALFORMED_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.UNICODE_TITLE;
import static io.github.tasostilsi.bookstore.books.data.BooksData.UNKNOWN_FIELD_BOOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@Epic("Bookstore API")
@Feature("Books create")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class BooksCreateTest {

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.3 and 15.3.2: creating a resource answers 201 with a Location")
    public void createReturns201WithLocation() {
        Api.books().create(BooksData.book()).assertThat().hasStatus(201).hasHeader("Location");
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.3: the created resource is readable at its Location with the sent representation")
    public void createMakesResourceReadableAtLocation() {
        Book book = BooksData.book();
        ApiResponse created = Api.books().create(book);
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the book it creates and returns its Location")
                .hasHeader("Location");

        Api.books().get(created.locationId()).assertThat().hasStatus(200)
                .hasField("id", created.locationId()).hasField("title", book.getTitle())
                .hasField("pageCount", book.getPageCount())
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("the schema declares no required fields, so an empty object creates a readable resource")
    public void createWithEmptyObjectCreatesReadableResource() {
        // This is not a validation test: the committed schema has no `required` list, so `{}` is valid.
        ApiResponse created = Api.books().create(EMPTY_OBJECT);
        created.assertThat().hasStatus(201).hasHeader("Location");
        assertThat(created.locationId(), greaterThan(0));
        Api.books().get(created.locationId()).assertThat().hasStatus(200)
                .hasField("id", created.locationId());
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.3: the server assigns the identity, so a later GET at the Location returns the same id")
    public void createWithoutIdAssignsServerId() {
        Book book = BooksData.bookWithoutId();
        ApiResponse created = Api.books().create(book);
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the book it creates and returns its Location")
                .hasHeader("Location");

        Api.books().get(created.locationId()).assertThat().hasStatus(200)
                .hasField("id", created.locationId()).hasField("title", book.getTitle())
                .hasField("pageCount", book.getPageCount());
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("the committed schema declares additionalProperties: false, so an unknown field is rejected")
    public void createWithUnknownFieldReturns400() {
        Api.books().create(UNKNOWN_FIELD_BOOK).assertThat()
                .hasStatus(400).isProblemJsonWithoutVersion();
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("a well-formed unicode title survives creation and is readable at the Location")
    public void createWithUnicodeTitleRoundTrips() {
        ApiResponse created = Api.books().create(BooksData.book().title(UNICODE_TITLE));
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the book it creates and returns its Location")
                .hasHeader("Location");
        Api.books().get(created.locationId()).assertThat().hasStatus(200)
                .hasField("id", created.locationId()).hasField("title", UNICODE_TITLE);
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

    @Test(groups = "contract")
    public void createRejectsPageCountAboveInt32() {
        Api.books().create("{\"title\":\"t\",\"pageCount\":2147483648}").assertThat()
                .hasStatus(400).isProblemJsonWithoutVersion().hasNonNullField("errors['$.pageCount']");
    }

    @Test(groups = "regression")
    @Feature("Measured behaviour")
    @Description("the committed schema declares no minimum on pageCount, so acceptance is this implementation's choice")
    public void createAcceptsNegativePageCount() {
        Book book = BooksData.book().pageCount(-5);
        Api.books().create(book).assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", book.getId())
                .hasField("title", book.getTitle()).hasField("pageCount", -5)
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
    }

    @Test(groups = "regression")
    @Feature("Measured behaviour")
    @Description("the committed schema declares no maxLength, so acceptance is this implementation's choice")
    public void createAcceptsVeryLongTitle() {
        Book book = BooksData.book().title(BooksData.longTitle());
        Api.books().create(book).assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", book.getId())
                .hasField("title", BooksData.longTitle()).hasField("pageCount", book.getPageCount())
                .hasField("description", book.getDescription()).hasField("excerpt", book.getExcerpt())
                .hasInstantField("publishDate", book.getPublishDate());
    }
}
