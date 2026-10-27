package io.github.tasostilsi.bookstore.books;

import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.config.ApiPaths;
import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static io.github.tasostilsi.bookstore.books.data.BooksData.BOOK_1_TITLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

@Epic("Bookstore API")
@Feature("Books delete")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class BooksDeleteTest {

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    @Description("the deviation: a successful DELETE returns 200 with a zero-byte body and no Content-Type header,"
            + " where REST expects 204 when no representation is returned")
    public void deleteExistingReturns200WithEmptyBody() {
        Api.books().delete(1).assertThat().hasStatus(200).hasEmptyBody().hasNoContentType();
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    public void deleteDoesNotRemoveResource() {
        Api.books().delete(1).assertThat().hasStatus(200).hasEmptyBody();
        // DELETE is a no-op, so this assertion does not depend on the delete having changed state.
        Api.books().get(1).assertThat().hasStatus(200).hasField("id", 1).hasField("title", BOOK_1_TITLE);
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    public void deleteSucceedsForAbsentId() {
        Api.books().delete(99999).assertThat().hasStatus(200).hasEmptyBody().hasNoContentType();
    }

    @Test(groups = "contract")
    public void deleteCollectionReturns405() {
        ApiResponse r = Api.request("DELETE", ApiPaths.BOOKS);
        r.assertThat().hasStatus(405).hasHeader("Allow").hasNoContentType().hasEmptyBody();
        // RFC 9110 requires Allow on a 405. Measured 2026-10-27: "GET, POST"; order is not asserted.
        assertThat(r.header("Allow"), allOf(containsString("GET"), containsString("POST")));
    }

    @Test(groups = "contract")
    public void deleteWithNonIntegerIdReturns400() {
        Api.request("DELETE", ApiPaths.book("abc")).assertThat().hasStatus(400).isProblemJson()
                .hasContentTypeNotContaining("v=1.0").hasNonNullField("errors.id");
    }
}
