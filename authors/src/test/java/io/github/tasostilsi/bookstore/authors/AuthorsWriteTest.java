package io.github.tasostilsi.bookstore.authors;

import io.github.tasostilsi.bookstore.authors.data.AuthorsData;
import io.github.tasostilsi.bookstore.client.Api;
import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.github.tasostilsi.bookstore.model.Author;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static io.github.tasostilsi.bookstore.authors.data.AuthorsData.STABLE_AUTHOR_ID;

@Epic("Bookstore API")
@Feature("Authors write")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class AuthorsWriteTest {

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.3 and 15.3.2: creating a resource answers 201 with a Location")
    public void createReturns201WithLocation() {
        Api.authors().create(AuthorsData.author()).assertThat().hasStatus(201).hasHeader("Location");
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.3: the created author is readable at its Location with the sent representation")
    public void createMakesResourceReadableAtLocation() {
        Author author = AuthorsData.author();
        ApiResponse created = Api.authors().create(author);
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the author it creates and returns its Location")
                .hasHeader("Location");

        Api.authors().get(created.locationId()).assertThat().hasStatus(200)
                .hasField("id", created.locationId()).hasField("idBook", author.getIdBook())
                .hasField("firstName", author.getFirstName())
                .hasField("lastName", author.getLastName());
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("the schema declares no required fields, so an omitted idBook still creates a readable resource")
    public void createWithoutIdBookCreatesReadableResource() {
        Author author = AuthorsData.authorWithoutIdBook();
        ApiResponse created = Api.authors().create(author);
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the author it creates and returns its Location")
                .hasHeader("Location");

        Api.authors().get(created.locationId()).assertThat().hasStatus(200)
                .hasField("id", created.locationId()).hasField("firstName", author.getFirstName());
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("an idBook with no matching book violates referential integrity; 400, 409 and 422 are accepted")
    public void createWithNonExistentBookIdIsRejected() {
        // A service rule inferred from the Authors-to-Books relation, not an RFC requirement.
        Api.authors().create(AuthorsData.author().idBook(99999)).assertThat()
                .hasStatusOneOf(400, 409, 422);
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.4: replacing a stored representation answers 200 or 204 and is visible on the next GET")
    public void updateExistingAuthorReplacesRepresentation() {
        ApiResponse created = Api.authors().create(AuthorsData.author());
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the author this test replaces and returns its Location")
                .hasHeader("Location");
        int id = created.locationId();

        Author replacement = AuthorsData.author(id);
        Api.authors().update(id, replacement).assertThat().hasStatusOneOf(200, 204);
        Api.authors().get(id).assertThat().hasStatus(200)
                .hasField("id", id).hasField("firstName", replacement.getFirstName())
                .hasField("lastName", replacement.getLastName());
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.4: a PUT to an id with no current representation creates it and answers 201")
    public void updateAbsentAuthorCreates201WithLocation() {
        Author author = AuthorsData.author();
        ApiResponse created = Api.authors().update(author.getId(), author);
        created.assertThat().hasStatus(201).hasHeader("Location");
        Api.authors().get(created.locationId()).assertThat().hasStatus(200)
                .hasField("id", created.locationId()).hasField("firstName", author.getFirstName());
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.4: a body id that disagrees with the path is an inconsistent representation (409)")
    public void updateWithMismatchedBodyIdReturns409() {
        // A correct service rejects before mutating, so seed author 1 is safe to address here.
        Api.authors().update(STABLE_AUTHOR_ID, AuthorsData.author(7)).assertThat().hasStatus(409);
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 9.3.5: DELETE answers 204 and the author is gone")
    public void deleteExistingAuthorReturns204AndRemoves() {
        ApiResponse created = Api.authors().create(AuthorsData.author());
        created.assertThat()
                .hasStatus(201, "precondition: a correct service stores the author this test deletes and returns its Location")
                .hasHeader("Location");
        int id = created.locationId();

        Api.authors().get(id).assertThat().hasStatus(200);
        Api.authors().delete(id).assertThat().hasStatus(204).hasEmptyBody();
        Api.authors().get(id).assertThat().hasStatus(404);
    }

    @Test(groups = {"contract", "defect"})
    @Feature("Contract defects")
    @Description("RFC 9110 15.5.5: deleting an absent author answers 404")
    public void deleteAbsentAuthorReturns404() {
        Api.authors().delete(9000).assertThat().hasStatus(404);
    }
}
