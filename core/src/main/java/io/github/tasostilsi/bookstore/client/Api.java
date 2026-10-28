package io.github.tasostilsi.bookstore.client;

import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public final class Api {

    private Api() {
    }

    public static ApiResponse request(String method, String path) {
        return request(method, path, null, null, null);
    }

    public static ApiResponse request(String method, String path, Map<String, String> headers) {
        return request(method, path, null, null, headers);
    }

    /** Sends a generated model, serialised through the non-null mapper in {@link ApiSpec}. */
    public static ApiResponse request(String method, String path, Object body, Map<String, String> headers) {
        return request(method, path, ApiSpec.body(body), ApiSpec.JSON, headers);
    }

    public static ApiResponse request(String method, String path, String body,
                                      String contentType, Map<String, String> headers) {
        RequestSpecification spec = ApiSpec.request();
        if (headers != null) {
            spec.headers(headers);
        }
        if (contentType != null) {
            spec.header("Content-Type", contentType);
        }
        if (body != null) {
            spec.body(body);
        }
        return ApiResponse.of(spec.request(method, path));
    }

    public static BooksApi books() {
        return new BooksApi();
    }

    public static AuthorsApi authors() {
        return new AuthorsApi();
    }
}
