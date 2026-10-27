package io.github.tasostilsi.bookstore.client;

import io.github.tasostilsi.bookstore.config.ApiConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;

import java.time.Duration;

import static io.restassured.RestAssured.given;

final class ApiSpec {

    private ApiSpec() {
    }

    static RequestSpecification request() {
        RestAssuredConfig config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .connectTimeout(Duration.ofMillis(ApiConfig.connectTimeoutMs()))
                        .readTimeout(Duration.ofMillis(ApiConfig.socketTimeoutMs())));
        if (ApiConfig.logAll()) {
            config = config.logConfig(LogConfig.logConfig()
                    .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL));
        }
        return given()
                .config(config)
                .baseUri(ApiConfig.baseUri())
                .filter(new AllureRestAssured());
    }
}
