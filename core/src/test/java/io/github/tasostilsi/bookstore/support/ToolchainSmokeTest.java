package io.github.tasostilsi.bookstore.support;

import io.github.tasostilsi.bookstore.config.ApiConfig;
import io.github.tasostilsi.bookstore.config.ApiPaths;
import io.qameta.allure.Step;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ToolchainSmokeTest {

    @Test
    public void reactorResolvesAndServiceAnswers() {
        assertThat(collectionStatus(), is(200));
    }

    @Step("smoke: fetch the collection")
    private int collectionStatus() {
        return given().baseUri(ApiConfig.baseUri()).get(ApiPaths.BOOKS).statusCode();
    }
}
