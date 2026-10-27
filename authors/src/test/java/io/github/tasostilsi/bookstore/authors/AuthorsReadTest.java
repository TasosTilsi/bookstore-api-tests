package io.github.tasostilsi.bookstore.authors;

import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.config.ApiPaths;
import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.github.tasostilsi.bookstore.model.Author;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import static io.github.tasostilsi.bookstore.authors.data.AuthorsData.STABLE_AUTHOR_ID;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;

@Epic("Bookstore API")
@Feature("Authors read")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class AuthorsReadTest {

    @Test(groups = "contract")
    public void listReturns200WithJsonContentType() {
        Api.authors().list().assertThat().hasStatus(200).isVersionedJson();
    }

    @Test(groups = "contract")
    public void listDeserialisesIntoGeneratedModels() {
        List<Author> authors = Api.authors().list().bodyAsList(Author.class);
        assertThat(authors, not(empty()));
        // A non-empty list alone passes on bound-but-empty objects: the generated mapper ignores
        // unknown fields, so the first element has to prove that the fields actually bound.
        assertThat(authors.get(0).getId(), is(1));
        assertThat(authors.get(0).getIdBook(), allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(200)));
    }

    @Test(groups = "dynamicData")
    @Feature("Dynamic data")
    @Description("the ids are contiguous from 1 but the total varies per call, so only the shape is asserted")
    public void listIdsAreContiguousAscendingFromOne() {
        List<Integer> ids = Api.authors().list().json().getList("id");
        assertEquals(ids, IntStream.rangeClosed(1, ids.size()).boxed().toList());
    }

    @Test(groups = "dynamicData")
    @Feature("Dynamic data")
    public void listCoversEveryBookGroup() {
        List<Integer> books = Api.authors().list().json().getList("idBook");
        assertThat(new HashSet<>(books), is(IntStream.rangeClosed(1, 200).boxed().collect(toSet())));
    }

    @Test(groups = "contract")
    public void getStableLowIdReturnsAuthor() {
        ApiResponse r = Api.authors().get(STABLE_AUTHOR_ID);
        r.assertThat().hasStatus(200).hasField("id", STABLE_AUTHOR_ID);
        assertThat(r.json().getMap("$").keySet(), containsInAnyOrder(
                "id", "idBook", "firstName", "lastName"));
        assertThat(r.json().getInt("idBook"), allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(200)));
    }

    @Test(groups = "contract", dataProvider = "absentIds")
    public void absentIdsReturn404ProblemJson(int id) {
        // Of the problem responses, the 404 carries v=1.0 and the 400 below does not.
        Api.authors().get(id).assertThat().hasStatus(404).isProblemJson().hasContentTypeContaining("v=1.0");
    }

    @DataProvider
    public Object[][] absentIds() {
        return new Object[][]{{0}, {100000}};
    }

    @Test(groups = "contract")
    public void nonIntegerIdReturns400() {
        Api.request("GET", ApiPaths.author("abc")).assertThat()
                .hasStatus(400).isProblemJson().hasContentTypeNotContaining("v=1.0")
                .hasNonNullField("errors.id");
    }
}
