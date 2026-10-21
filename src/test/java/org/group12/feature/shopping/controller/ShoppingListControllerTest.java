package org.group12.feature.shopping.controller;

import org.group12.common.dto.response.CursorResponse;
import org.group12.common.exception.GlobalExceptionHandler;
import org.group12.common.utils.AppUtil;
import org.group12.feature.shopping.dto.request.ShoppingListCreateRequest;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.group12.feature.shopping.dto.response.ShoppingListDetailResponse;
import org.group12.feature.shopping.dto.response.ShoppingListResponse;
import org.group12.feature.shopping.enums.ShoppingItemStatus;
import org.group12.feature.shopping.exception.ShoppingListNotFoundException;
import org.group12.feature.shopping.service.ShoppingListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShoppingListControllerTest {

    private static final UUID LIST_ID = UUID.randomUUID();

    private final ShoppingListService shoppingListService = mock(ShoppingListService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ShoppingListController(shoppingListService))
                .setControllerAdvice(new GlobalExceptionHandler(new AppUtil()))
                .build();
    }

    @Test
    void create_withValidBody_returnsCreatedCode() throws Exception {
        when(shoppingListService.create(any(ShoppingListCreateRequest.class)))
                .thenReturn(ShoppingListResponse.builder().id(LIST_ID).name("Weekend").build());

        mockMvc.perform(post("/v1/shopping-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Weekend\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SHOPPING_201"))
                .andExpect(jsonPath("$.data.name").value("Weekend"));
    }

    @Test
    void create_withBlankName_returns400() throws Exception {
        mockMvc.perform(post("/v1/shopping-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(shoppingListService);
    }

    @Test
    void list_passesCursorAndSizeToService() throws Exception {
        UUID cursor = UUID.randomUUID();
        when(shoppingListService.getMyLists(cursor, 10)).thenReturn(CursorResponse.<ShoppingListResponse>builder()
                .content(List.of(ShoppingListResponse.builder().id(LIST_ID).name("Weekend").build()))
                .hasNext(false)
                .build());

        mockMvc.perform(get("/v1/shopping-lists").param("cursor", cursor.toString()).param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SHOPPING_200"))
                .andExpect(jsonPath("$.data.content[0].name").value("Weekend"))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    void list_withoutParams_usesDefaultSize20() throws Exception {
        when(shoppingListService.getMyLists(null, 20)).thenReturn(
                CursorResponse.<ShoppingListResponse>builder().content(List.of()).hasNext(false).build());

        mockMvc.perform(get("/v1/shopping-lists")).andExpect(status().isOk());

        verify(shoppingListService).getMyLists(null, 20);
    }

    @Test
    void detail_returnsItems() throws Exception {
        when(shoppingListService.getDetail(LIST_ID)).thenReturn(ShoppingListDetailResponse.builder()
                .id(LIST_ID)
                .name("Weekend")
                .items(List.of(ShoppingItemResponse.builder().name("Egg").status(ShoppingItemStatus.PENDING).build()))
                .build());

        mockMvc.perform(get("/v1/shopping-lists/" + LIST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].name").value("Egg"))
                .andExpect(jsonPath("$.data.items[0].status").value("PENDING"));
    }

    @Test
    void detail_unknownList_returns404() throws Exception {
        when(shoppingListService.getDetail(LIST_ID)).thenThrow(new ShoppingListNotFoundException());

        mockMvc.perform(get("/v1/shopping-lists/" + LIST_ID)).andExpect(status().isNotFound());
    }

    @Test
    void detail_withMalformedId_returns400() throws Exception {
        mockMvc.perform(get("/v1/shopping-lists/not-a-uuid")).andExpect(status().isBadRequest());

        verifyNoInteractions(shoppingListService);
    }
}
