package io.github.tasostilsi.bookstore.client;

import io.github.tasostilsi.bookstore.config.ApiPaths;
import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.qameta.allure.Step;

public final class BooksApi {

    @Step("GET /Books")
    public ApiResponse list() {
        return Api.request("GET", ApiPaths.BOOKS);
    }

    @Step("GET /Books/{id}")
    public ApiResponse get(int id) {
        return Api.request("GET", ApiPaths.book(id));
    }

    public ApiResponse create(String body) {
        return create(body, ApiSpec.JSON);
    }

    @Step("POST /Books")
    public ApiResponse create(String body, String contentType) {
        return Api.request("POST", ApiPaths.BOOKS, body, contentType, null);
    }

    @Step("PUT /Books/{id}")
    public ApiResponse update(int id, String body) {
        return Api.request("PUT", ApiPaths.book(id), body, ApiSpec.JSON, null);
    }

    @Step("DELETE /Books/{id}")
    public ApiResponse delete(int id) {
        return Api.request("DELETE", ApiPaths.book(id));
    }
}
