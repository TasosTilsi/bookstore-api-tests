package io.github.tasostilsi.bookstore.http;

import io.github.tasostilsi.bookstore.model.Book;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.builder.ResponseBuilder;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.expectThrows;

@Epic("Bookstore API")
@Feature("HTTP layer")
@Severity(SeverityLevel.MINOR)
public class ApiResponseEdgeTest {

    // Built locally rather than fetched: what is under test is the wrapper's handling of a shape,
    // not the service's. The live shapes are asserted by BooksDeleteTest, for both the 200 with no
    // body and the 405 with no Content-Type.
    private static ApiResponse response(int status) {
        return ApiResponse.of(new ResponseBuilder().setStatusCode(status).build());
    }

    @Test(description = "A 200 with zero bytes must say so, not fail inside a parser")
    public void emptyBodyAccessorsThrowIllegalStateException() {
        ApiResponse r = response(200);
        r.assertThat().hasStatus(200).hasEmptyBody();
        IllegalStateException onBodyAs = expectThrows(IllegalStateException.class, () -> r.bodyAs(Book.class));
        IllegalStateException onJson = expectThrows(IllegalStateException.class, () -> r.json());
        IllegalStateException onBodyAsList = expectThrows(IllegalStateException.class, () -> r.bodyAsList(Book.class));
        // RestAssured also throws IllegalStateException for an absent Content-Type; the message is
        // what proves the guard fired rather than the parser.
        assertThat(onBodyAs.getMessage(), containsString("no body"));
        assertThat(onJson.getMessage(), containsString("no body"));
        assertThat(onBodyAsList.getMessage(), containsString("no body"));
    }

    @Test(description = "A 405-shaped response with no Content-Type and an empty body is asserted without parsing the body")
    public void absentContentTypeDoesNotParseBody() {
        response(405).assertThat().hasStatus(405).hasNoContentType().hasEmptyBody();
    }
}
