package io.github.tasostilsi.bookstore.authors;

import io.github.tasostilsi.bookstore.client.Api;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static io.github.tasostilsi.bookstore.authors.data.AuthorsData.AUTHOR_FOREIGN_KEY_GAP;
import static io.github.tasostilsi.bookstore.authors.data.AuthorsData.AUTHOR_NO_ID_BOOK;
import static io.github.tasostilsi.bookstore.authors.data.AuthorsData.AUTHOR_WITH_ID;
import static io.github.tasostilsi.bookstore.authors.data.AuthorsData.STABLE_AUTHOR_ID;

@Epic("Bookstore API")
@Feature("Authors write")
@Severity(SeverityLevel.CRITICAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class AuthorsWriteTest {

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    public void createEchoesPayloadWith200AndNoLocation() {
        Api.authors().create(AUTHOR_WITH_ID).assertThat().hasStatus(200).isVersionedJson()
                .hasField("id", 7000).hasField("idBook", 1).hasField("firstName", "Ada")
                .hasField("lastName", "Lovelace").hasNoHeader("Location");
    }

    @Test(groups = {"deviation", "validationGap"})
    @Feature("Contract deviations")
    @Feature("Validation gaps")
    public void createWithoutIdBookDefaultsToZero() {
        Api.authors().create(AUTHOR_NO_ID_BOOK).assertThat().hasStatus(200).isVersionedJson()
                .hasField("id", 0).hasField("idBook", 0).hasField("firstName", "nobook");
    }

    @Test(groups = "validationGap")
    @Feature("Validation gaps")
    public void createAcceptsForeignKeyOutsideBookRange() {
        Api.authors().create(AUTHOR_FOREIGN_KEY_GAP).assertThat().hasStatus(200).isVersionedJson()
                .hasField("idBook", 99999);
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    public void createDoesNotPersist() {
        Api.authors().create(AUTHOR_WITH_ID).assertThat().hasStatus(200).hasField("id", 7000);
        Api.authors().get(7000).assertThat().hasStatus(404);
    }

    @Test(groups = "contract")
    public void updateEchoesPayload() {
        Api.authors().update(STABLE_AUTHOR_ID,
                        "{\"id\":1,\"idBook\":7,\"firstName\":\"echo\",\"lastName\":\"check\"}")
                .assertThat().hasStatus(200).isVersionedJson().hasField("id", 1).hasField("idBook", 7);
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    @Description("the body id wins over the path id")
    public void updateUsesBodyIdNotPathId() {
        Api.authors().update(STABLE_AUTHOR_ID,
                        "{\"id\":7,\"idBook\":9,\"firstName\":\"x\",\"lastName\":\"y\"}")
                .assertThat().hasStatus(200).hasField("id", 7).hasField("idBook", 9);
    }

    @Test(groups = "deviation")
    @Feature("Contract deviations")
    @Description("200 with zero bytes and no Content-Type, and the author is still there afterwards")
    public void deleteReturns200AndRemovesNothing() {
        Api.authors().delete(STABLE_AUTHOR_ID).assertThat().hasStatus(200).hasEmptyBody().hasNoContentType();
        Api.authors().get(STABLE_AUTHOR_ID).assertThat().hasStatus(200).hasField("id", STABLE_AUTHOR_ID);
    }
}
