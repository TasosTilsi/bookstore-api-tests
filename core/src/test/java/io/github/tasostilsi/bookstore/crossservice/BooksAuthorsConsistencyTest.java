package io.github.tasostilsi.bookstore.crossservice;

import io.github.tasostilsi.bookstore.client.Api;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.assertEquals;

@Epic("Bookstore API")
@Feature("Cross-service consistency")
@Severity(SeverityLevel.NORMAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class BooksAuthorsConsistencyTest {

    @Test(groups = "crossservice")
    public void everyBookHasAuthors() {
        // Both sides are read from the service, never from a literal: the demo's data set changing
        // must red this test on the Books side, which is the point. No size is asserted on either
        // side, because only the set of groups is invariant, not the number of authors.
        List<Integer> bookIds = Api.books().list().json().getList("id");
        List<Integer> grouped = Api.authors().list().json().getList("idBook");
        assertEquals(new HashSet<>(grouped), new HashSet<>(bookIds));
    }

    @Test(groups = {"crossservice", "deviation"})
    @Feature("Contract deviations")
    public void statelessnessSpansBothResources() {
        Api.books().create("{\"id\":9001,\"title\":\"cross\"}").assertThat()
                .hasStatus(200).hasField("id", 9001).hasField("title", "cross");
        Api.authors().create("{\"id\":9002,\"idBook\":9001,\"firstName\":\"cross\",\"lastName\":\"cross\"}")
                .assertThat().hasStatus(200).hasField("idBook", 9001);
        Api.books().get(9001).assertThat().hasStatus(404);
        Api.authors().get(9002).assertThat().hasStatus(404);
    }
}
