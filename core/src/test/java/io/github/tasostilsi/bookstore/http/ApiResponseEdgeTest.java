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
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.expectThrows;

@Epic("Bookstore API")
@Feature("HTTP layer")
@Severity(SeverityLevel.MINOR)
public class ApiResponseEdgeTest {

    // Built locally rather than fetched: what is under test is the wrapper's handling of a shape,
    // not the service's. The live shapes are asserted by BooksDeleteTest, for both the 204 with no
    // body and the 405 with no Content-Type.
    private static ApiResponse response(int status) {
        return ApiResponse.of(new ResponseBuilder().setStatusCode(status).build());
    }

    private static ApiResponse response(int status, String contentType, String body) {
        return ApiResponse.of(new ResponseBuilder().setStatusCode(status)
                .setContentType(contentType).setBody(body).build());
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

    @Test(description = "hasStatus accepts the measured status and nothing else")
    public void hasStatusFailsWhenStatusDiffers() {
        expectThrows(AssertionError.class, () -> response(201).assertThat().hasStatus(200));
    }

    @Test(description = "hasStatusOneOf accepts the allowed set and rejects a status outside it")
    public void hasStatusOneOfAcceptsOnlyTheAllowedSet() {
        response(204).assertThat().hasStatusOneOf(200, 204);
        expectThrows(AssertionError.class, () -> response(200).assertThat().hasStatusOneOf(201, 204));
    }

    @Test(description = "locationId reads the last path segment of the Location header")
    public void locationIdReturnsLastPathSegment() {
        ApiResponse created = ApiResponse.of(new ResponseBuilder().setStatusCode(201)
                .setHeader("Location", "/api/v1/Books/42").build());
        assertThat(created.locationId(), is(42));
    }

    @Test(description = "locationId fails loudly when a 2xx carries no Location")
    public void locationIdRejectsAbsentHeader() {
        IllegalStateException thrown = expectThrows(IllegalStateException.class,
                () -> response(200).locationId());
        assertThat(thrown.getMessage(), containsString("Location"));
    }

    @Test(description = "hasNullField fails on an absent key rather than passing on a null one")
    public void hasNullFieldFailsWhenKeyIsAbsent() {
        expectThrows(AssertionError.class, () -> response(200, "application/json; charset=utf-8; v=1.0",
                "{\"id\":1}").assertThat().hasNullField("title"));
    }

    @Test(description = "isProblemJson compares the body status with the response status")
    public void problemJsonFailsWhenBodyStatusDiffers() {
        expectThrows(AssertionError.class, () -> response(400, "application/problem+json; charset=utf-8",
                "{\"type\":\"t\",\"title\":\"t\",\"traceId\":\"x\",\"status\":404}").assertThat().isProblemJson());
    }
}
