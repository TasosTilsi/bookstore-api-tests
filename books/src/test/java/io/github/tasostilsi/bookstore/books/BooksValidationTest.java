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
                .hasStatus(400).isProblemJsonWithoutVersion()
                .hasNonNullField("errors.id");
    }

    @Test(groups = "contract")
    public void decimalPathIdReturns400() {
        Api.request("GET", ApiPaths.book("1.5")).assertThat()
                .hasStatus(400).isProblemJsonWithoutVersion()
                .hasNonNullField("errors.id");
    }

    @Test(groups = "regression")
    @Feature("Measured behaviour")
    @Description("path matching is case-insensitive: RFC 3986 treats path segments as case-sensitive,"
            + " so resolving a mixed-case spelling is this implementation's choice")
    public void mixedCasePathResolves() {
        // bOoKs differs from the declared Books spelling, so a 200 proves case-insensitive matching.
        Api.request("GET", ApiPaths.BASE_PATH + "/bOoKs/1").assertThat().hasStatus(200).hasField("id", 1);
    }

    @Test(groups = "regression")
    @Feature("Measured behaviour")
    @Description("a trailing slash answers 200 with the collection; redirects are not followed, "
            + "so a 3xx would surface here rather than be resolved")
    public void trailingSlashResolves() {
        Api.request("GET", ApiPaths.BOOKS + "/").assertThat().hasStatus(200).hasBodySize(200)
                .hasField("[0].id", 1);
    }

    @Test(groups = "regression")
    @Feature("Measured behaviour")
    @Description("page and size query parameters are ignored rather than honoured or rejected, so the full"
            + " 200-element list is returned; the committed schema declares no query parameters")
    public void queryParametersAreIgnored() {
        Api.request("GET", ApiPaths.BOOKS + "?page=2&size=1").assertThat()
                .hasStatus(200).hasBodySize(200).hasField("[0].id", 1);
    }
}
