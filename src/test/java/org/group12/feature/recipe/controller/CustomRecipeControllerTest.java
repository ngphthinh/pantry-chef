package org.group12.feature.recipe.controller;

import org.group12.common.exception.GlobalExceptionHandler;
import org.group12.common.utils.AppUtil;
import org.group12.feature.recipe.dto.request.RecipeCreateRequest;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.service.CustomRecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomRecipeControllerTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String VALID_BODY = """
            {"title":"Pho","servings":2,
             "ingredients":[{"name":"rice noodle","amount":200,"unit":"G"}],
             "steps":[{"text":"Boil the broth"}]}
            """;

    private final CustomRecipeService customRecipeService = mock(CustomRecipeService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CustomRecipeController(customRecipeService))
                .setControllerAdvice(new GlobalExceptionHandler(new AppUtil()))
                .build();
    }

    @Test
    void create_withValidBody_returnsCreated() throws Exception {
        when(customRecipeService.create(any(RecipeCreateRequest.class)))
                .thenReturn(RecipeDetailResponse.builder().id(ID).title("Pho").custom(true).build());

        mockMvc.perform(post("/v1/recipes/custom").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("RECIPE_201"))
                .andExpect(jsonPath("$.data.title").value("Pho"))
                .andExpect(jsonPath("$.data.custom").value(true));
    }

    @Test
    void create_withoutTitleAndSteps_returnsBadRequestAndSkipsService() throws Exception {
        mockMvc.perform(post("/v1/recipes/custom").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servings\":2,\"ingredients\":[{\"name\":\"rice\"}],\"steps\":[]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customRecipeService);
    }

    @Test
    void create_withNonPositiveAmount_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/v1/recipes/custom").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"servings\":2,\"ingredients\":[{\"name\":\"rice\",\"amount\":0}],"
                                + "\"steps\":[{\"text\":\"go\"}]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customRecipeService);
    }

    @Test
    void getById_malformedUuid_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/v1/recipes/custom/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void getById_otherUsersRecipe_returnsNotFound() throws Exception {
        when(customRecipeService.getById(ID)).thenThrow(new RecipeNotFoundException());

        mockMvc.perform(get("/v1/recipes/custom/" + ID)).andExpect(status().isNotFound());
    }

    @Test
    void delete_returnsSuccessCode() throws Exception {
        mockMvc.perform(delete("/v1/recipes/custom/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("RECIPE_200"));
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new RecipeNotFoundException()).when(customRecipeService).delete(ID);

        mockMvc.perform(delete("/v1/recipes/custom/" + ID)).andExpect(status().isNotFound());
    }
}
