package io.github.tasostilsi.bookstore.client;

import io.github.tasostilsi.bookstore.config.ApiPaths;
import io.github.tasostilsi.bookstore.http.ApiResponse;
import io.qameta.allure.Step;

public final class AuthorsApi {

    @Step("GET /Authors")
    public ApiResponse list() {
        return Api.request("GET", ApiPaths.AUTHORS);
    }

    @Step("GET /Authors/{id}")
    public ApiResponse get(int id) {
        return Api.request("GET", ApiPaths.author(id));
    }

    @Step("GET /Authors/authors/books/{bookId}")
    public ApiResponse byBook(int bookId) {
        return Api.request("GET", ApiPaths.authorsByBook(bookId));
    }

    @Step("POST /Authors")
    public ApiResponse create(String body) {
        return Api.request("POST", ApiPaths.AUTHORS, body, "application/json; charset=utf-8", null);
    }

    @Step("PUT /Authors/{id}")
    public ApiResponse update(int id, String body) {
        return Api.request("PUT", ApiPaths.author(id), body, "application/json; charset=utf-8", null);
    }

    @Step("DELETE /Authors/{id}")
    public ApiResponse delete(int id) {
        return Api.request("DELETE", ApiPaths.author(id));
    }
}
