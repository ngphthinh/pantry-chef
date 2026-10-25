package org.group12.feature.recipe.client.model;

import lombok.*;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalRecipeStep implements Serializable {

    private int order;
    private String text;
    private Integer timerSeconds;
}