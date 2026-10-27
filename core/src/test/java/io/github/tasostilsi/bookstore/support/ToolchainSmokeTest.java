package io.github.tasostilsi.bookstore.support;

import io.github.tasostilsi.bookstore.config.ApiConfig;
import io.github.tasostilsi.bookstore.config.ApiPaths;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Epic("Bookstore API")
@Feature("Infrastructure")
@Severity(SeverityLevel.MINOR)
public class ToolchainSmokeTest {

    @Test
    public void reactorResolvesAndServiceAnswers() {
        assertThat(collectionStatus(), is(200));
    }

    @Step("smoke: fetch the collection")
    private int collectionStatus() {
        return given()
                .baseUri(ApiConfig.baseUri())
                .filter(new AllureRestAssured())
                .get(ApiPaths.BOOKS)
                .statusCode();
    }
}
