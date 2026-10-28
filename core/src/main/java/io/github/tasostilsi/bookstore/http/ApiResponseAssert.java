package io.github.tasostilsi.bookstore.http;

import io.restassured.path.json.JsonPath;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
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

    /** Names a precondition, so a fixture failure does not read as the assertion the test exists to make. */
    public ApiResponseAssert hasStatus(int status, String because) {
        assertThat(because, response.status(), is(status));
        return this;
    }

    /** Only for statuses RFC 9110 allows as alternatives on the same outcome, such as 200 or 204 on PUT. */
    public ApiResponseAssert hasStatusOneOf(int... allowed) {
        List<Matcher<? super Integer>> matchers = Arrays.stream(allowed).mapToObj(Matchers::is)
                .collect(Collectors.toList());
        assertThat(response.status(), anyOf(matchers));
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

    /**
     * Measured on every 200 that returns a representation: GET, POST and PUT all carry
     * {@code application/json; charset=utf-8; v=1.0}. Problem responses carry no version parameter.
     */
    public ApiResponseAssert isVersionedJson() {
        hasContentTypeContaining("application/json").hasContentTypeContaining("charset=utf-8")
                .hasContentTypeContaining("v=1.0");
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

    // The service keeps 7 fractional digits, so the echoed string is not equal to the sent one;
    // the instant is. Comparing the string would fail on a sub-second difference that is not real.
    public ApiResponseAssert hasInstantField(String jsonPath, OffsetDateTime expected) {
        assertThat(OffsetDateTime.parse(response.json().getString(jsonPath)).toInstant(),
                is(expected.toInstant()));
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
        // Both measured on every problem response. Absence of the `v=1.0` parameter is the callers'
        // assertion, because it is a property of the versioning convention, not of the envelope.
        hasContentTypeContaining("application/problem+json").hasContentTypeContaining("charset=utf-8");
        JsonPath json = response.json();
        assertThat(json.get("type"), notNullValue());
        assertThat(json.get("title"), notNullValue());
        assertThat(json.get("traceId"), notNullValue());
        assertThat(json.getInt("status"), is(response.status()));
        return this;
    }

    /**
     * Every 400 and 415 measured carries problem+json with no version parameter, where the 404 for a
     * missing resource carries {@code v=1.0}. Matched on {@code v=} rather than {@code v=1.0}, so a
     * future version cannot satisfy a caller that claims no version is sent.
     */
    public ApiResponseAssert isProblemJsonWithoutVersion() {
        return isProblemJson().hasContentTypeNotContaining("v=");
    }
}
