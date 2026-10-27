package io.github.tasostilsi.bookstore.books.protocol;

import io.github.tasostilsi.bookstore.client.Api;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static io.github.tasostilsi.bookstore.books.data.BooksData.EMPTY_BODY;
import static io.github.tasostilsi.bookstore.books.data.BooksData.FULL_BOOK;

/**
 * Canonical home of the error envelope. Its requests repeat other classes on purpose: the other
 * classes assert behaviour, this one asserts the envelope's measured content, meaning the version
 * parameter, the exact {@code type} URI and the exact {@code title}, which the shared helpers
 * deliberately leave to callers.
 */
@Epic("Bookstore API")
@Feature("Error format")
@Severity(SeverityLevel.NORMAL)
@Link(name = "OpenAPI specification", url = "https://fakerestapi.azurewebsites.net/swagger/v1/swagger.json")
public class ErrorFormatTest {

    @Test(groups = "contract")
    public void missingResourceBodyIsProblemJsonWithVersion() {
        Api.books().get(99999).assertThat().hasStatus(404).isProblemJson()
                .hasContentTypeContaining("v=1.0")
                .hasField("type", "https://tools.ietf.org/html/rfc7231#section-6.5.4");
    }

    @Test(groups = "contract")
    public void validationErrorBodyIsProblemJsonWithoutVersion() {
        Api.books().update(1, EMPTY_BODY).assertThat().hasStatus(400).isProblemJsonWithoutVersion();
    }

    @Test(groups = "contract")
    public void unsupportedMediaTypeBodyIsProblemJsonWithoutVersion() {
        Api.books().create(FULL_BOOK, "text/plain").assertThat().hasStatus(415)
                .isProblemJsonWithoutVersion()
                .hasField("title", "Unsupported Media Type");
    }
}
