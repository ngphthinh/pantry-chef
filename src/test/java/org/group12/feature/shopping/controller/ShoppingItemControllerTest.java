package org.group12.feature.shopping.controller;

import org.group12.common.exception.GlobalExceptionHandler;
import org.group12.common.utils.AppUtil;
import org.group12.feature.shopping.dto.request.ShoppingItemCreateRequest;
import org.group12.feature.shopping.dto.request.ShoppingItemUpdateRequest;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.group12.feature.shopping.enums.ShoppingItemStatus;
import org.group12.feature.shopping.exception.ShoppingItemNotFoundException;
import org.group12.feature.shopping.service.ShoppingItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShoppingItemControllerTest {

    private static final UUID LIST_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final String ITEMS_URL = "/v1/shopping-lists/" + LIST_ID + "/items";

    private final ShoppingItemService shoppingItemService = mock(ShoppingItemService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ShoppingItemController(shoppingItemService))
                .setControllerAdvice(new GlobalExceptionHandler(new AppUtil()))
                .build();
    }

    @Test
    void create_withValidBody_returnsCreatedCode() throws Exception {
        when(shoppingItemService.create(eq(LIST_ID), any(ShoppingItemCreateRequest.class)))
                .thenReturn(ShoppingItemResponse.builder().id(ITEM_ID).name("Milk")
                        .status(ShoppingItemStatus.PENDING).build());

        mockMvc.perform(post(ITEMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Milk\",\"quantity\":2,\"unit\":\"L\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SHOPPING_201"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void create_withMissingNameAndNegativeQuantity_returns400() throws Exception {
        mockMvc.perform(post(ITEMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.quantity").exists());

        verifyNoInteractions(shoppingItemService);
    }

    @Test
    void create_withUnknownUnit_returns400() throws Exception {
        mockMvc.perform(post(ITEMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Milk\",\"unit\":\"BUCKET\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(shoppingItemService);
    }

    @Test
    void patch_onlyStatus_passesPartialRequestToService() throws Exception {
        when(shoppingItemService.update(eq(LIST_ID), eq(ITEM_ID), any(ShoppingItemUpdateRequest.class)))
                .thenReturn(ShoppingItemResponse.builder().id(ITEM_ID).name("Milk")
                        .status(ShoppingItemStatus.BOUGHT).build());

        mockMvc.perform(patch(ITEMS_URL + "/" + ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BOUGHT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SHOPPING_200"))
                .andExpect(jsonPath("$.data.status").value("BOUGHT"));

        ArgumentCaptor<ShoppingItemUpdateRequest> captor = ArgumentCaptor.forClass(ShoppingItemUpdateRequest.class);
        verify(shoppingItemService).update(eq(LIST_ID), eq(ITEM_ID), captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ShoppingItemStatus.BOUGHT);
        assertThat(captor.getValue().getName()).isNull();
        assertThat(captor.getValue().getQuantity()).isNull();
    }

    @Test
    void patch_withBlankName_returns400() throws Exception {
        mockMvc.perform(patch(ITEMS_URL + "/" + ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(shoppingItemService);
    }

    @Test
    void patch_unknownItem_returns404() throws Exception {
        when(shoppingItemService.update(eq(LIST_ID), eq(ITEM_ID), any(ShoppingItemUpdateRequest.class)))
                .thenThrow(new ShoppingItemNotFoundException());

        mockMvc.perform(patch(ITEMS_URL + "/" + ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BOUGHT\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returnsDeletedCode() throws Exception {
        mockMvc.perform(delete(ITEMS_URL + "/" + ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SHOPPING_200"));

        verify(shoppingItemService).delete(LIST_ID, ITEM_ID);
    }
}
