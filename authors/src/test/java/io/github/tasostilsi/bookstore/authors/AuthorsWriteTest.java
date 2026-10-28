package io.github.tasostilsi.bookstore.authors;

import io.github.tasostilsi.bookstore.authors.data.AuthorsData;
import io.github.tasostilsi.bookstore.client.Api;
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

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    public void createEchoesPayloadWith200AndNoLocation() {
        Author author = AuthorsData.author();
        Api.authors().create(author).assertThat().hasStatus(200).isVersionedJson()
                .hasField("id", author.getId()).hasField("idBook", author.getIdBook())
                .hasField("firstName", author.getFirstName())
                .hasField("lastName", author.getLastName()).hasNoHeader("Location");
    }

    @Test(groups = {"deviation", "validationGap"})
    @Feature("Contract deviations")
    @Feature("Validation gaps")
    public void createWithoutIdBookDefaultsToZero() {
        Author author = AuthorsData.authorWithoutIdBook();
        Api.authors().create(author).assertThat().hasStatus(200).isVersionedJson()
                .hasField("id", 0).hasField("idBook", 0).hasField("firstName", author.getFirstName())
                .hasField("lastName", author.getLastName());
    }

    @Test(groups = "validationGap")
    @Feature("Validation gaps")
    public void createAcceptsForeignKeyOutsideBookRange() {
        Author author = AuthorsData.author().idBook(99999);
        Api.authors().create(author).assertThat().hasStatus(200).isVersionedJson()
                .hasField("id", author.getId()).hasField("idBook", 99999)
                .hasField("firstName", author.getFirstName())
                .hasField("lastName", author.getLastName());
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    public void createDoesNotPersist() {
        Author author = AuthorsData.author();
        Api.authors().create(author).assertThat().hasStatus(200)
                .hasField("id", author.getId()).hasField("idBook", author.getIdBook())
                .hasField("firstName", author.getFirstName())
                .hasField("lastName", author.getLastName());
        Api.authors().get(author.getId()).assertThat().hasStatus(404);
    }

    @Test(groups = "contract")
    public void updateEchoesPayload() {
        Author author = AuthorsData.author(1);
        Api.authors().update(STABLE_AUTHOR_ID, author).assertThat()
                .hasStatus(200).isVersionedJson().hasField("id", 1)
                .hasField("idBook", author.getIdBook())
                .hasField("firstName", author.getFirstName())
                .hasField("lastName", author.getLastName());
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    @Description("the body id wins over the path id")
    public void updateUsesBodyIdNotPathId() {
        Author author = AuthorsData.author(7).idBook(9);
        Api.authors().update(STABLE_AUTHOR_ID, author).assertThat()
                .hasStatus(200).hasField("id", 7).hasField("idBook", 9)
                .hasField("firstName", author.getFirstName())
                .hasField("lastName", author.getLastName());
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    @Description("200 with zero bytes and no Content-Type, and the author is still there afterwards")
    public void deleteReturns200AndRemovesNothing() {
        Api.authors().delete(STABLE_AUTHOR_ID).assertThat().hasStatus(200).hasEmptyBody().hasNoContentType();
        Api.authors().get(STABLE_AUTHOR_ID).assertThat().hasStatus(200).hasField("id", STABLE_AUTHOR_ID);
    }
}
