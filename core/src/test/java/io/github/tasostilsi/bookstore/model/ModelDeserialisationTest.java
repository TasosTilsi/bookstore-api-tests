package io.github.tasostilsi.bookstore.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

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
        Book book = new ObjectMapper().findAndRegisterModules().readValue(BOOK_JSON, Book.class);
        assertNotNull(book);
    }
}
