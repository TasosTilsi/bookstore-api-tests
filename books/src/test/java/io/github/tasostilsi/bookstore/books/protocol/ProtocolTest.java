package io.github.tasostilsi.bookstore.books.protocol;

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

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Epic("Bookstore API")
@Feature("Protocol")
@Severity(SeverityLevel.NORMAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class ProtocolTest {

    @Test(groups = "contract")
    public void patchOnCollectionReturns405() {
        assertMethodNotAllowedOnCollection("PATCH");
    }

    @Test(groups = "contract")
    public void optionsOnCollectionReturns405() {
        // RFC 9110 section 9.1 makes OPTIONS optional, so a 405 here contradicts no convention.
        assertMethodNotAllowedOnCollection("OPTIONS");
    }

    @Test(groups = "contract")
    @Description("A path that matches no endpoint answers 404 with an empty body and no Content-Type")
    public void unknownRouteReturns404WithEmptyBody() {
        Api.request("GET", ApiPaths.BASE_PATH + "/Nope").assertThat()
                .hasStatus(404).hasNoContentType().hasEmptyBody();
    }

    @Test(groups = "contract")
    @Description("The collection is open, so an unrecognised bearer token changes nothing: the request "
            + "is served rather than rejected")
    public void requestWithUnrecognisedBearerTokenIsNotRejected() {
        Api.request("GET", ApiPaths.BOOKS, Map.of("Authorization", "Bearer not-a-real-token"))
                .assertThat().hasStatus(200).isVersionedJson();
    }

    private void assertMethodNotAllowedOnCollection(String method) {
        ApiResponse r = Api.request(method, ApiPaths.BOOKS);
        r.assertThat().hasStatus(405).hasHeader("Allow").hasNoContentType().hasEmptyBody();
        // RFC 9110 requires Allow on a 405. Measured 2026-10-27 as "GET, POST"; header order is not
        // asserted, but the rejected verb must not appear in its own Allow list.
        assertThat(r.header("Allow"), allOf(
                containsString("GET"), containsString("POST"), not(containsString(method))));
    }
}
