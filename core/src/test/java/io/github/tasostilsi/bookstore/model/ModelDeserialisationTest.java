package io.github.tasostilsi.bookstore.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tasostilsi.bookstore.generated.JSON;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.testng.Assert.assertNotNull;

@Epic("Bookstore API")
@Feature("Models")
@Severity(SeverityLevel.MINOR)
public class ModelDeserialisationTest {

    private static final String BOOK_JSON = """
        {"id":1,"title":"Book 1","description":"Lorem.","pageCount":100,
         "excerpt":"Lorem.","publishDate":"2026-10-25T22:06:18.2094658+00:00"}
        """;

    @Test(description = "The measured wire format deserialises into the spec-generated model")
    public void publishedBookPayloadDeserialisesIntoGeneratedModel() throws Exception {
        Book book = strictGeneratedMapper().readValue(BOOK_JSON, Book.class);
        assertNotNull(book);
        assertThat(book.getPublishDate(), instanceOf(OffsetDateTime.class));
    }

    // The generated mapper disables FAIL_ON_UNKNOWN_PROPERTIES; this test wants a renamed field to fail.
    private static ObjectMapper strictGeneratedMapper() {
        return JSON.getDefault().getMapper().copy()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
