package com.cookbook.service;

import com.cookbook.entity.Ingredient;
import com.cookbook.entity.Recipe;
import com.cookbook.entity.Step;
import com.cookbook.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    public List<Recipe> findAll() {
        return recipeRepository.findAllByOrderByCreatedAtDesc();
    }

    public Page<Recipe> findAll(Pageable pageable) {
        return recipeRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Optional<Recipe> findById(Long id) {
        return recipeRepository.findById(id);
    }

    @Transactional
    public Recipe create(Recipe recipe) {
        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.setRecipe(recipe);
            }
        }
        if (recipe.getSteps() != null) {
            for (int i = 0; i < recipe.getSteps().size(); i++) {
                Step step = recipe.getSteps().get(i);
                if (step.getStepNumber() == null) {
                    step.setStepNumber(i + 1);
                }
                step.setRecipe(recipe);
            }
        }
        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe update(Long id, Recipe recipeDetails) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜谱不存在: " + id));

        recipe.setName(recipeDetails.getName());
        recipe.setCoverImage(recipeDetails.getCoverImage());
        recipe.setCuisine(recipeDetails.getCuisine());
        recipe.setDifficulty(recipeDetails.getDifficulty());
        recipe.setCookingTime(recipeDetails.getCookingTime());
        recipe.setDescription(recipeDetails.getDescription());

        recipe.getIngredients().clear();
        if (recipeDetails.getIngredients() != null) {
            for (Ingredient ingredient : recipeDetails.getIngredients()) {
                ingredient.setRecipe(recipe);
                recipe.getIngredients().add(ingredient);
            }
        }

        recipe.getSteps().clear();
        if (recipeDetails.getSteps() != null) {
            for (int i = 0; i < recipeDetails.getSteps().size(); i++) {
                Step step = recipeDetails.getSteps().get(i);
                if (step.getStepNumber() == null) {
                    step.setStepNumber(i + 1);
                }
                step.setRecipe(recipe);
                recipe.getSteps().add(step);
            }
        }

        return recipeRepository.save(recipe);
    }

    @Transactional
    public void delete(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜谱不存在: " + id));
        recipeRepository.delete(recipe);
    }

    public Page<Recipe> searchWithFilters(
            String keyword,
            String cuisine,
            String difficulty,
            Integer minTime,
            Integer maxTime,
            Pageable pageable) {
        
        if ((keyword == null || keyword.isEmpty()) &&
            (cuisine == null || cuisine.isEmpty()) &&
            (difficulty == null || difficulty.isEmpty()) &&
            minTime == null && maxTime == null) {
            return findAll(pageable);
        }
        
        return recipeRepository.searchWithFilters(keyword, cuisine, difficulty, minTime, maxTime, pageable);
    }

    public List<String> getAllCuisines() {
        return recipeRepository.findAllDistinctCuisines();
    }

    public List<String> getAllDifficulties() {
        return recipeRepository.findAllDistinctDifficulties();
    }

    public Recipe getRandomRecipe() {
        return recipeRepository.findRandomRecipe();
    }

    public long countTotalRecipes() {
        return recipeRepository.countTotalRecipes();
    }
}
