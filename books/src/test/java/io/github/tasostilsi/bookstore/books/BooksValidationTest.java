package io.github.tasostilsi.bookstore.books;

import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.config.ApiPaths;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

@Epic("Bookstore API")
@Feature("Books validation and path handling")
@Severity(SeverityLevel.NORMAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class BooksValidationTest {

    @Test(groups = "contract")
    public void nonIntegerPathIdReturns400WithErrors() {
        Api.request("GET", ApiPaths.book("abc")).assertThat()
                .hasStatus(400).isProblemJson().hasContentTypeNotContaining("v=1.0")
                .hasNonNullField("errors.id");
    }

    @Test(groups = "contract")
    public void decimalPathIdReturns400() {
        Api.request("GET", ApiPaths.book("1.5")).assertThat()
                .hasStatus(400).isProblemJson().hasContentTypeNotContaining("v=1.0")
                .hasNonNullField("errors.id");
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    @Description("path matching is case-insensitive: RFC 3986 treats path segments as case-sensitive,"
            + " so a mixed-case spelling should not resolve")
    public void lowercasePathResolves() {
        // Mixed case cannot plausibly be a separately registered alias for a lowercase route.
        Api.request("GET", ApiPaths.BASE_PATH + "/bOoKs/1").assertThat().hasStatus(200).hasField("id", 1);
    }

    @Test(groups = "contract")
    @Description("a trailing slash answers 200 with the collection; the client follows redirects,"
            + " so a 3xx would not be distinguishable here and is recorded as a known limit")
    public void trailingSlashResolves() {
        Api.request("GET", ApiPaths.BOOKS + "/").assertThat().hasStatus(200).hasBodySize(200)
                .hasField("[0].id", 1);
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    @Description("page and size query parameters are ignored rather than honoured or rejected, so the full"
            + " 200-element list is returned")
    public void queryParametersAreIgnored() {
        Api.request("GET", ApiPaths.BOOKS + "?page=2&size=1").assertThat()
                .hasStatus(200).hasBodySize(200).hasField("[0].id", 1);
    }
}
