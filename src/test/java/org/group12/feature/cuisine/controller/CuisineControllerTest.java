package org.group12.feature.cuisine.controller;

import org.group12.common.exception.GlobalExceptionHandler;
import org.group12.common.utils.AppUtil;
import org.group12.feature.recipe.controller.CuisineController;
import org.group12.feature.recipe.dto.request.CuisineRequest;
import org.group12.feature.recipe.dto.response.CuisineResponse;
import org.group12.feature.recipe.exception.CuisineInUseException;
import org.group12.feature.recipe.exception.CuisineNotFoundException;
import org.group12.feature.recipe.service.CuisineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CuisineControllerTest {

    private static final UUID ID = UUID.randomUUID();

    private final CuisineService cuisineService = mock(CuisineService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CuisineController(cuisineService))
                .setControllerAdvice(new GlobalExceptionHandler(new AppUtil()))
                .build();
    }

    @Test
    void list_returnsCuisines() throws Exception {
        when(cuisineService.getAll()).thenReturn(List.of(
                CuisineResponse.builder().id(ID).code("VIETNAMESE").name("Vietnamese").build()));

        mockMvc.perform(get("/v1/cuisines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CUISINE_200"))
                .andExpect(jsonPath("$.data[0].code").value("VIETNAMESE"))
                .andExpect(jsonPath("$.data[0].name").value("Vietnamese"));
    }

    @Test
    void create_withValidBody_returnsCreatedCode() throws Exception {
        when(cuisineService.create(any(CuisineRequest.class))).thenReturn(
                CuisineResponse.builder().id(ID).code("JAPANESE").name("Japanese").build());

        mockMvc.perform(post("/v1/cuisines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"JAPANESE\",\"name\":\"Japanese\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CUISINE_201"))
                .andExpect(jsonPath("$.data.code").value("JAPANESE"));
    }

    @Test
    void create_withInvalidBody_returns400AndFieldErrors() throws Exception {
        mockMvc.perform(post("/v1/cuisines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"bad code\",\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").exists())
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(cuisineService);
    }

    @Test
    void update_unknownId_returns404() throws Exception {
        when(cuisineService.update(any(UUID.class), any(CuisineRequest.class))).thenThrow(new CuisineNotFoundException());

        mockMvc.perform(put("/v1/cuisines/" + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ASIAN\",\"name\":\"Asian\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_withMalformedId_returns400() throws Exception {
        mockMvc.perform(delete("/v1/cuisines/not-a-uuid")).andExpect(status().isBadRequest());

        verifyNoInteractions(cuisineService);
    }

    @Test
    void delete_cuisineInUse_returns409() throws Exception {
        doThrow(new CuisineInUseException()).when(cuisineService).delete(ID);

        mockMvc.perform(delete("/v1/cuisines/" + ID)).andExpect(status().isConflict());
    }

    @Test
    void delete_unusedCuisine_returnsDeletedCode() throws Exception {
        mockMvc.perform(delete("/v1/cuisines/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CUISINE_200"));
    }
}
