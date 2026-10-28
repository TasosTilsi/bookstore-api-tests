package io.github.tasostilsi.bookstore.books;

import io.github.tasostilsi.bookstore.books.data.BooksData;
import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.github.tasostilsi.bookstore.model.Book;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static io.github.tasostilsi.bookstore.books.data.BooksData.EMPTY_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.EMPTY_OBJECT;
import static io.github.tasostilsi.bookstore.books.data.BooksData.MALFORMED_BODY;

@Epic("Bookstore API")
@Feature("Books update")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class BooksUpdateTest {

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.4: a PUT to an existing resource answers 200 or 204")
    public void updateExistingBookReturns200Or204() {
        ApiResponse created = Api.books().create(BooksData.book());
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the book this test replaces and returns its Location")
                .hasHeader("Location");
        Api.books().update(created.locationId(), BooksData.book(created.locationId()))
                .assertThat().hasStatusOneOf(200, 204);
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.4: a PUT to an id with no current representation creates it and answers 201")
    public void updateAbsentIdCreates201WithLocation() {
        Book book = BooksData.book();
        ApiResponse created = Api.books().update(book.getId(), book);
        created.assertThat().hasStatus(201).hasHeader("Location");
        Api.books().get(created.locationId()).assertThat().hasStatus(200)
                .hasField("id", created.locationId()).hasField("title", book.getTitle());
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.4: the replacement is what a later GET returns")
    public void updateIsVisibleOnNextRead() {
        Book original = BooksData.book();
        ApiResponse created = Api.books().create(original);
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the book this test replaces and returns its Location")
                .hasHeader("Location");
        int id = created.locationId();

        Book replacement = BooksData.book(id).title("replaced-" + original.getTitle());
        Api.books().update(id, replacement).assertThat().hasStatusOneOf(200, 204);
        Api.books().get(id).assertThat().hasStatus(200)
                .hasField("id", id).hasField("title", replacement.getTitle());
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("the resource is identified by its URI, so replacing it with {} keeps the path id")
    public void updateWithEmptyObjectKeepsPathId() {
        ApiResponse created = Api.books().create(BooksData.book());
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the book this test replaces and returns its Location")
                .hasHeader("Location");
        int id = created.locationId();

        Api.books().update(id, EMPTY_OBJECT).assertThat().hasStatusOneOf(200, 204);
        Api.books().get(id).assertThat().hasStatus(200).hasField("id", id);
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.4: PUT replaces, so a field omitted from the request is cleared")
    public void updateReplacesOmittedFieldsWithNull() {
        ApiResponse created = Api.books().create(BooksData.book());
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the book this test replaces and returns its Location")
                .hasHeader("Location");
        int id = created.locationId();

        Book replacement = BooksData.book(id).description(null).excerpt(null);
        Api.books().update(id, replacement).assertThat().hasStatusOneOf(200, 204);
        Api.books().get(id).assertThat().hasStatus(200)
                .hasField("id", id).hasNullField("description").hasNullField("excerpt");
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.4: a body id that disagrees with the path is an inconsistent representation (409)")
    public void updateWithMismatchedBodyIdReturns409() {
        // A correct service rejects before mutating, so seed book 1 is safe to address here.
        Api.books().update(1, BooksData.book(7)).assertThat().hasStatus(409);
    }

    @Test(groups = "contract")
    public void updateWithZeroLengthBodyReturns400() {
        Api.books().update(1, EMPTY_BODY).assertThat().hasStatus(400).isProblemJsonWithoutVersion()
                .hasNonNullField("errors['']");
    }

    @Test(groups = "contract")
    public void updateWithMalformedBodyReturns400() {
        Api.books().update(1, MALFORMED_BODY).assertThat().hasStatus(400).isProblemJsonWithoutVersion()
                .hasNonNullField("errors['$.title']");
    }
}
