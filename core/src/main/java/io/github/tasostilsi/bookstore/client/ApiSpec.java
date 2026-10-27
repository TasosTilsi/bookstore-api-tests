package io.github.tasostilsi.bookstore.client;

import io.github.tasostilsi.bookstore.config.ApiConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
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
        RequestSpecification spec = given()
                .config(config)
                .baseUri(ApiConfig.baseUri())
                .filter(new AllureRestAssured());
        if (ApiConfig.logAll()) {
            // LogConfig's knob only fires on a RestAssured validation failure; this suite asserts outside it.
            return spec.filter(new RequestLoggingFilter(LogDetail.ALL))
                    .filter(new ResponseLoggingFilter(LogDetail.ALL));
        }
        return spec;
    }
}
