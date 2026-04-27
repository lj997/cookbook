package com.cookbook.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "菜谱名称不能为空")
    @Column(nullable = false)
    private String name;

    private String coverImage;

    @NotBlank(message = "菜系分类不能为空")
    @Column(nullable = false)
    private String cuisine;

    @NotBlank(message = "难度等级不能为空")
    @Column(nullable = false)
    private String difficulty;

    @NotNull(message = "烹饪时长不能为空")
    @Column(nullable = false)
    private Integer cookingTime;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<Step> steps = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
        ingredient.setRecipe(null);
    }

    public void addStep(Step step) {
        steps.add(step);
        step.setRecipe(this);
    }

    public void removeStep(Step step) {
        steps.remove(step);
        step.setRecipe(null);
    }
}
