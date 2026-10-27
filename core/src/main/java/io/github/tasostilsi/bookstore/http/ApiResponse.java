package io.github.tasostilsi.bookstore.http;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.List;

public final class ApiResponse {

    private final Response response;

    public ApiResponse(Response response) {
        this.response = response;
    }

    public static ApiResponse of(Response response) {
        return new ApiResponse(response);
    }

    public int status() {
        return response.statusCode();
    }

    public String header(String name) {
        return response.getHeader(name);
    }

    public String contentType() {
        // getContentType() normalises an absent header to "", but the contract is null when absent.
        return response.getHeader("Content-Type");
    }

    public String body() {
        return response.getBody().asString();
    }

    public boolean hasBody() {
        return !body().isEmpty();
    }

    public JsonPath json() {
        requireBody();
        return response.jsonPath();
    }

    public <T> T bodyAs(Class<T> type) {
        requireBody();
        return response.as(type);
    }

    public <T> List<T> bodyAsList(Class<T> elementType) {
        return response.jsonPath().getList("", elementType);
    }

    public ApiResponseAssert assertThat() {
        return new ApiResponseAssert(this);
    }

    Response raw() {
        return response;
    }

    private void requireBody() {
        if (!hasBody()) {
            throw new IllegalStateException("response has no body: " + status() + " " + raw().statusLine());
        }
    }
}
