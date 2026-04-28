package com.cookbook.dto;

import lombok.Data;
import java.util.List;

@Data
public class GeneratedRecipeDto {
    private String name;
    private String coverImage;
    private String cuisine;
    private String difficulty;
    private Integer cookingTime;
    private String description;
    private List<IngredientDto> ingredients;
    private List<StepDto> steps;

    @Data
    public static class IngredientDto {
        private String name;
        private String quantity;
    }

    @Data
    public static class StepDto {
        private Integer stepNumber;
        private String description;
        private String image;
    }
}
