package io.github.tasostilsi.bookstore.http;

import io.restassured.path.json.JsonPath;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public final class ApiResponseAssert {

    private final ApiResponse response;

    ApiResponseAssert(ApiResponse response) {
        this.response = response;
    }

    public ApiResponseAssert hasStatus(int status) {
        assertThat(response.status(), is(status));
        return this;
    }

    public ApiResponseAssert hasHeader(String name) {
        assertThat(response.header(name), notNullValue());
        return this;
    }

    public ApiResponseAssert hasNoHeader(String name) {
        assertThat(response.header(name), nullValue());
        return this;
    }

    public ApiResponseAssert hasContentTypeContaining(String fragment) {
        assertThat(response.contentType(), containsString(fragment));
        return this;
    }

    public ApiResponseAssert hasContentTypeNotContaining(String fragment) {
        assertThat(response.contentType(), not(containsString(fragment)));
        return this;
    }

    public ApiResponseAssert hasNoContentType() {
        assertThat(response.contentType(), nullValue());
        return this;
    }

    public ApiResponseAssert hasField(String jsonPath, Object expected) {
        assertThat(response.json().get(jsonPath), is(expected));
        return this;
    }

    // shortcut: top-level keys only; JsonPath.get cannot tell a present null from an absent key.
    public ApiResponseAssert hasNullField(String jsonPath) {
        assertThat(response.json().getMap("").containsKey(jsonPath), is(true));
        assertThat(response.json().get(jsonPath), nullValue());
        return this;
    }

    public ApiResponseAssert hasNonNullField(String jsonPath) {
        assertThat(response.json().get(jsonPath), notNullValue());
        return this;
    }

    public ApiResponseAssert hasNoField(String jsonPath) {
        assertThat(response.json().getMap("").containsKey(jsonPath), is(false));
        return this;
    }

    public ApiResponseAssert hasBodySize(int size) {
        assertThat(response.json().getList("").size(), is(size));
        return this;
    }

    public ApiResponseAssert hasEmptyBody() {
        assertThat(response.body(), is(""));
        return this;
    }

    public ApiResponseAssert isProblemJson() {
        hasContentTypeContaining("application/problem+json");
        JsonPath json = response.json();
        assertThat(json.get("type"), notNullValue());
        assertThat(json.get("title"), notNullValue());
        assertThat(json.get("traceId"), notNullValue());
        assertThat(json.getInt("status"), is(response.status()));
        return this;
    }
}
