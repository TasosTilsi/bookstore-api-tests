package io.github.tasostilsi.bookstore.books;

import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.config.ApiPaths;
import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.github.tasostilsi.bookstore.model.Book;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

import static io.github.tasostilsi.bookstore.books.data.BooksData.BOOK_1_PAGE_COUNT;
import static io.github.tasostilsi.bookstore.books.data.BooksData.BOOK_1_TITLE;
import static io.github.tasostilsi.bookstore.books.data.BooksData.MAX_BOOK_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Epic("Bookstore API")
@Feature("Books read")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class BooksReadTest {

    @Test(groups = "contract")
    public void listReturns200WithJsonContentType() {
        Api.books().list().assertThat().hasStatus(200)
                .hasContentTypeContaining("application/json").hasContentTypeContaining("charset=utf-8");
    }

    @Test(groups = "contract")
    public void listReturnsExactlyTwoHundredBooks() {
        Api.books().list().assertThat().hasBodySize(200);
    }

    @Test(groups = "contract")
    public void listIdsAreContiguousAscendingFromOne() {
        List<Integer> ids = Api.books().list().json().getList("id");
        assertEquals(ids, IntStream.rangeClosed(1, 200).boxed().toList());
    }

    @Test(groups = "contract")
    public void listDeserialisesIntoGeneratedModels() {
        assertThat(Api.books().list().bodyAsList(Book.class), hasSize(200));
    }

    @Test(groups = "contract")
    public void getExistingBookReturnsIt() {
        ApiResponse r = Api.books().get(1);
        r.assertThat().hasStatus(200)
                .hasField("id", 1).hasField("title", BOOK_1_TITLE)
                .hasField("pageCount", BOOK_1_PAGE_COUNT).hasNonNullField("description");
        assertThat(r.json().getMap("$").keySet(), containsInAnyOrder(
                "id", "title", "description", "pageCount", "excerpt", "publishDate"));
    }

    @Test(groups = "contract")
    public void getLastBookReturnsIt() {
        Api.books().get(MAX_BOOK_ID).assertThat().hasStatus(200).hasField("id", 200);
    }

    @Test(groups = "contract", dataProvider = "absentIds")
    public void absentIdsReturn404ProblemJson(int id) {
        Api.books().get(id).assertThat().hasStatus(404).isProblemJson();
    }

    @DataProvider
    public Object[][] absentIds() {
        return new Object[][]{{0}, {-1}, {201}, {99999}};
    }

    @Test(groups = "contract", dataProvider = "invalidIds", description = "path segment is not an int")
    public void invalidIdsReturn400WithErrors(String rawId) {
        Api.request("GET", ApiPaths.book(rawId)).assertThat()
                .hasStatus(400).isProblemJson().hasNonNullField("errors");
    }

    @DataProvider
    public Object[][] invalidIds() {
        return new Object[][]{{"abc"}, {"1.5"}, {"2147483648"}};
    }

    @Test(groups = "contract", description = "GET publishDate carries a UTC offset")
    public void publishDateIsParsableOffsetDateTime() {
        String value = Api.books().get(1).json().getString("publishDate");
        assertNotNull(OffsetDateTime.parse(value));
    }

    @Test(groups = "dynamicData", dataProvider = "sampleIds")
    @Feature("Dynamic data")
    public void publishDateIsNowMinusNDays(int id) {
        ApiResponse r = Api.books().get(id);
        OffsetDateTime published = OffsetDateTime.parse(r.json().getString("publishDate"));
        OffsetDateTime served = OffsetDateTime.parse(r.header("Date"), DateTimeFormatter.RFC_1123_DATE_TIME);
        assertThat(Math.abs(Duration.between(published, served.minusDays(id)).getSeconds()), lessThanOrEqualTo(5L));
    }

    @DataProvider
    public Object[][] sampleIds() {
        return new Object[][]{{1}, {5}, {200}};
    }
}
