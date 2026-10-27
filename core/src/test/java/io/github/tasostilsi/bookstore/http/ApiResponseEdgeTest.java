package io.github.tasostilsi.bookstore.http;

import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.config.ApiPaths;
import io.github.tasostilsi.bookstore.model.Book;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.expectThrows;

@Epic("Bookstore API")
@Feature("HTTP layer")
@Severity(SeverityLevel.MINOR)
public class ApiResponseEdgeTest {

    @Test(description = "A 200 with zero bytes must say so, not fail inside a parser")
    public void emptyBodyAccessorsThrowIllegalStateException() {
        ApiResponse r = Api.books().delete(1);
        r.assertThat().hasStatus(200).hasEmptyBody();
        IllegalStateException onBodyAs = expectThrows(IllegalStateException.class, () -> r.bodyAs(Book.class));
        IllegalStateException onJson = expectThrows(IllegalStateException.class, () -> r.json());
        IllegalStateException onBodyAsList = expectThrows(IllegalStateException.class, () -> r.bodyAsList(Book.class));
        assertThat(onBodyAs.getMessage(), containsString("no body"));
        assertThat(onJson.getMessage(), containsString("no body"));
        assertThat(onBodyAsList.getMessage(), containsString("no body"));
    }

    @Test(description = "405 carries no Content-Type; asserting the status must not parse the body")
    public void absentContentTypeDoesNotParseBody() {
        Api.request("PATCH", ApiPaths.BOOKS)
                .assertThat().hasStatus(405).hasNoContentType().hasEmptyBody();
    }
}
