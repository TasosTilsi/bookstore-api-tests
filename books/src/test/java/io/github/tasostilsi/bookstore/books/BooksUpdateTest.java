package io.github.tasostilsi.bookstore.books;

import io.github.tasostilsi.bookstore.client.Api;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

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
        Api.books().update(1, "{\"id\":1,\"title\":\"changed\",\"pageCount\":42}").assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", 1).hasField("title", "changed").hasField("pageCount", 42)
                // A merge with book 1 would also echo those three fields; these three separate the two.
                .hasNullField("description").hasNullField("excerpt")
                .hasField("publishDate", ZERO_DATE_DEFAULT);
    }

    @Test(groups = "deviation",
            description = "the path id is ignored: the echoed id is the body id, defaulting to 0 when the body omits it")
    @Feature("Contract deviations")
    public void updateSucceedsForAbsentId() {
        Api.books().update(99999, "{\"title\":\"ghost\"}").assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", 0).hasField("title", "ghost");
    }

    @Test(groups = "contract")
    public void updateWithZeroLengthBodyReturns400() {
        Api.books().update(1, EMPTY_BODY).assertThat().hasStatus(400).isProblemJson()
                .hasContentTypeNotContaining("v=1.0").hasNonNullField("errors['']");
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
    public void updateNullsOmittedFields() {
        Api.books().update(1, "{\"id\":1,\"title\":\"partial\",\"pageCount\":9}").assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", 1).hasField("title", "partial").hasField("pageCount", 9)
                .hasNullField("description").hasNullField("excerpt")
                .hasField("publishDate", ZERO_DATE_DEFAULT);
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    public void updateUsesBodyIdNotPathId() {
        Api.books().update(1, "{\"id\":7,\"title\":\"override\"}").assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", 7);
    }

    @Test(groups = "contract")
    public void updateWithMalformedBodyReturns400() {
        Api.books().update(1, MALFORMED_BODY).assertThat().hasStatus(400).isProblemJson()
                .hasContentTypeNotContaining("v=1.0").hasNonNullField("errors['$.title']");
    }
}
