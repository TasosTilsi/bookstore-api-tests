package io.github.tasostilsi.bookstore.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tasostilsi.bookstore.config.ApiConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RedirectConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

import java.time.Duration;

import static io.restassured.RestAssured.given;

final class ApiSpec {

    // The only media type the suite sends. text/plain measured 415; others are untested.
    static final String JSON = "application/json; charset=utf-8";

    // Nulls are omitted: the service answers 400 to an explicit null id, and an omitted field binds
    // to the same default the suite would otherwise have to spell out. Deprecated in Jackson 2.22,
    // as is the generated mapper's own equivalent; a builder would have to repeat that configuration.
    // Qualified because the generated JSON class shares its name with the media-type constant above.
    private static final ObjectMapper REQUESTS =
            io.github.tasostilsi.bookstore.generated.JSON.getDefault().getMapper().copy()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private ApiSpec() {
    }

    static String body(Object body) {
        try {
            return REQUESTS.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("cannot serialise " + body.getClass().getSimpleName(), e);
        }
    }

    static RequestSpecification request() {
        RestAssuredConfig config = RestAssuredConfig.config()
                // A followed 3xx hides the response the suite exists to report.
                .redirect(RedirectConfig.redirectConfig().followRedirects(false))
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
