package org.group12.common.config;

import lombok.Data;

import java.util.List;

@Data
public class SeedData {

    private List<String> roles;
    private List<CodeNameSeed> syncStatuses;
    private List<CodeNameSeed> cuisines;
    private List<CategorySeed> ingredientCategories;
    private List<IngredientSeed> ingredients;
    private List<UserSeed> users;
    private List<ExcludedIngredientSeed> excludedIngredients;
    private List<PantryItemSeed> pantryItems;
    private List<RecipeSeed> recipes;
    private List<MealPlanSeed> mealPlans;
    private List<SavedRecipeSeed> savedRecipes;
    private List<CollectionSeed> collections;
    private List<FeedbackSeed> feedbacks;

    @Data
    public static class CodeNameSeed {
        private String code;
        private String name;
    }

    @Data
    public static class CategorySeed {
        private String code;
        private String name;
        private int sortOrder;
    }

    @Data
    public static class IngredientSeed {
        private String name;
        private String defaultUnit;
        private String barcode;
        private String category;
    }

    @Data
    public static class UserSeed {
        private String email;
        private String password;          // null → dùng app.password-default
        private String displayName;
        private String measurementSystem;
        private String themeMode;
        private List<String> roles;
    }

    @Data
    public static class ExcludedIngredientSeed {
        private String userEmail;
        private String name;
    }

    @Data
    public static class PantryItemSeed {
        private String userEmail;
        private String ingredient;
        private double quantity;
        private String location;
        private int expiryInDays;
        private String syncStatus;
    }

    @Data
    public static class RecipeSeed {
        private String externalId;
        private String title;
        private String imageUrl;
        private String cuisine;
        private int servings;
        private int readyInMinutes;
        private String difficulty;
        private List<RecipeIngredientSeed> ingredients;
        private List<RecipeStepSeed> steps;
    }

    @Data
    public static class RecipeIngredientSeed {
        private String name;
        private double amount;
        private String unit;
    }

    @Data
    public static class RecipeStepSeed {
        private String text;
        private Integer timerSeconds;
    }

    @Data
    public static class MealPlanSeed {
        private String userEmail;
        private int weekOffset;
        private List<MealPlanEntrySeed> entries;
    }

    @Data
    public static class MealPlanEntrySeed {
        private int dayOffset;
        private String mealType;
        private String recipeExternalId;
        private int servings;
    }

    @Data
    public static class SavedRecipeSeed {
        private String userEmail;
        private String recipeExternalId;
        private int rating;
        private String note;
        private boolean favorite;
        private String syncStatus;
    }

    @Data
    public static class CollectionSeed {
        private String userEmail;
        private String name;
        private List<String> recipeExternalIds;
    }

    @Data
    public static class FeedbackSeed {
        private String userEmail;
        private String type;
        private String message;
        private String appVersion;
        private String deviceInfo;
    }
}