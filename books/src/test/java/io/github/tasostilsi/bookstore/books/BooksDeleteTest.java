package io.github.tasostilsi.bookstore.books;

import io.github.tasostilsi.bookstore.books.data.BooksData;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Epic("Bookstore API")
@Feature("Books delete")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class BooksDeleteTest {

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.5: a successful DELETE answers 204 with no body")
    public void deleteExistingReturns204WithNoBody() {
        ApiResponse created = Api.books().create(BooksData.book());
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the book this test deletes and returns its Location")
                .hasHeader("Location");
        Api.books().delete(created.locationId()).assertThat()
                .hasStatus(204).hasEmptyBody().hasNoContentType();
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.5: the resource is gone afterwards, so a later GET and a second DELETE are 404")
    public void deleteRemovesResourceAndLaterGetReturns404() {
        ApiResponse created = Api.books().create(BooksData.book());
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the book this test deletes and returns its Location")
                .hasHeader("Location");
        int id = created.locationId();

        Api.books().get(id).assertThat().hasStatus(200).hasField("id", id);
        Api.books().delete(id).assertThat().hasStatus(204);
        Api.books().get(id).assertThat().hasStatus(404);
        Api.books().delete(id).assertThat().hasStatus(404);
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 15.5.5: deleting an absent resource answers 404")
    public void deleteAbsentIdReturns404() {
        Api.books().delete(99999).assertThat().hasStatus(404);
    }

    @Test(groups = "contract")
    public void deleteCollectionReturns405() {
        ApiResponse r = Api.request("DELETE", ApiPaths.BOOKS);
        r.assertThat().hasStatus(405).hasHeader("Allow").hasNoContentType().hasEmptyBody();
        // RFC 9110 requires Allow on a 405. Measured 2026-10-27: "GET, POST"; order is not asserted,
        // but the rejected verb must not appear in its own Allow list.
        assertThat(r.header("Allow"), allOf(
                containsString("GET"), containsString("POST"), not(containsString("DELETE"))));
    }

    @Test(groups = "contract")
    public void deleteWithNonIntegerIdReturns400() {
        Api.request("DELETE", ApiPaths.book("abc")).assertThat().hasStatus(400).isProblemJsonWithoutVersion()
                .hasNonNullField("errors.id");
    }
}
