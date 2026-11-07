package org.group12.feature.ingredient.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientBatchCreateRequest {

    @NotEmpty
    @Size(max = 50)
    private List<@Valid IngredientCreateRequest> items;
}