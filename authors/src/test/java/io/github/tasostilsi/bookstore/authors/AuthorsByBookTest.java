package io.github.tasostilsi.bookstore.authors;

import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.config.ApiPaths;
import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.github.tasostilsi.bookstore.model.Author;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@Epic("Bookstore API")
@Feature("Authors by book")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class AuthorsByBookTest {

    @Test(groups = "contract", dataProvider = "bookIds")
    public void validBookIdReturnsOnlyMatchingAuthors(int bookId) {
        ApiResponse r = Api.authors().byBook(bookId);
        r.assertThat().hasStatus(200).isVersionedJson();
        List<Author> authors = r.bodyAsList(Author.class);
        // A non-empty list alone passes on bound-but-empty objects: the generated mapper ignores
        // unknown fields, so the bound idBook is what proves the filter and the binding.
        assertThat(authors, not(empty()));
        assertThat(authors.stream().map(Author::getIdBook).toList(), everyItem(is(bookId)));
    }

    @DataProvider
    public Object[][] bookIds() {
        // Id 2 is here rather than in the cross-service class: a by-book call exercises Authors alone,
        // so it belongs to this module even when the class that used to make it also knew about Books.
        return new Object[][]{{1}, {2}, {100}, {200}};
    }

    @Test(groups = "contract", dataProvider = "outOfRangeBookIds")
    public void bookIdOutsideRangeReturnsEmptyArray(int bookId) {
        Api.authors().byBook(bookId).assertThat().hasStatus(200).isVersionedJson().hasBodySize(0);
    }

    @DataProvider
    public Object[][] outOfRangeBookIds() {
        // 201 is not used: a corrected service assigns the next sequential book id, which can be 201.
        return new Object[][]{{0}, {-1}, {99999}};
    }

    @Test(groups = "contract")
    public void nonIntegerBookIdReturns400() {
        Api.request("GET", ApiPaths.authorsByBook("abc")).assertThat()
                .hasStatus(400).isProblemJsonWithoutVersion()
                .hasNonNullField("errors.idBook");
    }

    @Test(groups = "contract")
    public void bookIdAboveInt32Returns400() {
        Api.request("GET", ApiPaths.authorsByBook("2147483648")).assertThat()
                .hasStatus(400).isProblemJsonWithoutVersion()
                .hasNonNullField("errors.idBook");
    }
}
