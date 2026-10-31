package org.group12.feature.recipe.client;

import org.group12.common.enums.Unit;
import org.group12.feature.recipe.client.model.ExternalRecipeDetail;
import org.group12.feature.recipe.client.model.ExternalRecipePage;
import org.group12.feature.recipe.client.model.ExternalRecipeSummary;
import org.group12.feature.recipe.client.model.RecipeSearchCriteria;
import org.group12.feature.recipe.client.model.RecipeSort;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.exception.RecipeProviderException;
import org.group12.feature.recipe.exception.RecipeProviderQuotaExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.startsWith;
import org.springframework.http.HttpMethod;

class SpoonacularClientTest {

    private static final String BASE_URL = "https://api.test";

    private MockRestServiceServer server;
    private SpoonacularClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SpoonacularClient(builder.build(), "test-key");
    }

    @Test
    void search_sendsFiltersAndMapsResults() {
        server.expect(requestTo(startsWith(BASE_URL + "/recipes/complexSearch")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-api-key", "test-key"))
                .andExpect(queryParam("includeIngredients", "egg,tomato"))
                .andExpect(queryParam("excludeIngredients", "shrimp"))
                .andExpect(queryParam("fillIngredients", "true"))
                .andExpect(queryParam("sort", "min-missing-ingredients"))
                .andExpect(queryParam("maxReadyTime", "30"))
                .andExpect(queryParam("offset", "20"))
                .andExpect(queryParam("number", "10"))
                .andRespond(withSuccess("""
                        {"results":[{"id":716429,"title":"Pasta","image":"http://img/1.jpg","readyInMinutes":25,
                          "servings":2,"cuisines":["Italian"],"usedIngredientCount":2,"missedIngredientCount":1,
                          "missedIngredients":[{"name":"garlic","amount":1.0}],"unknownField":"ignored"}],
                         "offset":20,"number":10,"totalResults":86}
                        """, MediaType.APPLICATION_JSON));

        ExternalRecipePage page = client.search(new RecipeSearchCriteria(
                List.of("egg", "tomato"), List.of("shrimp"), null, null, 30, RecipeSort.MIN_MISSING_INGREDIENTS), 20, 10);

        server.verify();
        assertEquals(86, page.totalResults());
        ExternalRecipeSummary item = page.items().get(0);
        assertEquals("716429", item.externalId());
        assertEquals("Pasta", item.title());
        assertEquals("Italian", item.cuisineName());
        assertEquals(2, item.usedIngredientCount());
        assertEquals(1, item.missedIngredientCount());
        assertEquals(List.of("garlic"), item.missedIngredients());
    }

    @Test
    void getDetail_mapsUnitsStepsAndTimers() {
        server.expect(requestTo(startsWith(BASE_URL + "/recipes/123/information")))
                .andExpect(header("x-api-key", "test-key"))
                .andRespond(withSuccess("""
                        {"id":123,"title":"Fried rice","image":"http://img/123.jpg","servings":2,"readyInMinutes":25,
                         "sourceUrl":"http://src","cuisines":["Asian"],
                         "extendedIngredients":[
                           {"name":"rice","amount":2.0,"unit":"cups",
                            "measures":{"us":{"amount":2.0,"unitShort":"cups"},"metric":{"amount":370.0,"unitShort":"g"}}},
                           {"name":"garlic","amount":2.0,"unit":"cloves"},
                           {"name":"  ","original":"salt"}],
                         "analyzedInstructions":[{"name":"","steps":[
                           {"number":1,"step":"Boil rice.","length":{"number":20,"unit":"minutes"}},
                           {"number":2,"step":"Fry everything."}]}]}
                        """, MediaType.APPLICATION_JSON));

        ExternalRecipeDetail detail = client.getDetail("123");

        server.verify();
        assertEquals("123", detail.externalId());
        assertEquals("Asian", detail.cuisineName());
        assertEquals(3, detail.ingredients().size());
        assertEquals(370.0, detail.ingredients().get(0).amount());
        assertEquals(Unit.G, detail.ingredients().get(0).unit());
        assertEquals(Unit.PCS, detail.ingredients().get(1).unit());
        assertEquals("salt", detail.ingredients().get(2).name());
        assertEquals(2, detail.steps().size());
        assertEquals(1, detail.steps().get(0).order());
        assertEquals(1200, detail.steps().get(0).timerSeconds());
        assertNull(detail.steps().get(1).timerSeconds());
    }

    @Test
    void getSimilar_buildsImageUrlFromImageType() {
        server.expect(requestTo(startsWith(BASE_URL + "/recipes/123/similar")))
                .andExpect(queryParam("number", "3"))
                .andRespond(withSuccess("""
                        [{"id":5,"title":"Soup","imageType":"jpg","readyInMinutes":40,"servings":4}]
                        """, MediaType.APPLICATION_JSON));

        List<ExternalRecipeSummary> similar = client.getSimilar("123", 3);

        assertEquals("https://img.spoonacular.com/recipes/5-312x231.jpg", similar.get(0).imageUrl());
    }

    @Test
    void providerStatusCodes_areTranslatedToAppExceptions() {
        server.expect(requestTo(startsWith(BASE_URL + "/recipes/1/information")))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo(startsWith(BASE_URL + "/recipes/2/information")))
                .andRespond(withStatus(HttpStatus.PAYMENT_REQUIRED));
        server.expect(requestTo(startsWith(BASE_URL + "/recipes/3/information")))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        server.expect(requestTo(startsWith(BASE_URL + "/recipes/4/information")))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(RecipeNotFoundException.class, () -> client.getDetail("1"));
        assertThrows(RecipeProviderQuotaExceededException.class, () -> client.getDetail("2"));
        assertThrows(RecipeProviderQuotaExceededException.class, () -> client.getDetail("3"));
        assertThrows(RecipeProviderException.class, () -> client.getDetail("4"));
    }

    @Test
    void missingApiKey_failsWithoutCallingProvider() {
        SpoonacularClient unconfigured = new SpoonacularClient(RestClient.builder().baseUrl(BASE_URL).build(), " ");

        assertThrows(RecipeProviderException.class, () -> unconfigured.getDetail("1"));
    }

    @Test
    void toUnit_mapsKnownUnitsAndFallsBackToPieces() {
        assertEquals(Unit.TBSP, SpoonacularClient.toUnit("Tbsps"));
        assertEquals(Unit.ML, SpoonacularClient.toUnit("milliliters"));
        assertEquals(Unit.PINCH, SpoonacularClient.toUnit("pinch"));
        assertEquals(Unit.PCS, SpoonacularClient.toUnit("slice"));
        assertEquals(Unit.PCS, SpoonacularClient.toUnit(null));
    }

    @Test
    void supportsCuisine_isCaseInsensitiveAndRejectsUnknown() {
        assertTrue(client.supportsCuisine("Vietnamese"));
        assertTrue(client.supportsCuisine(" ITALIAN "));
        assertFalse(client.supportsCuisine("Western"));
        assertFalse(client.supportsCuisine(null));
    }
}
