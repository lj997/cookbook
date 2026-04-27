package com.cookbook.controller;

import com.cookbook.entity.Recipe;
import com.cookbook.payload.ApiResponse;
import com.cookbook.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Recipe>>> getAllRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Integer minTime,
            @RequestParam(required = false) Integer maxTime) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Recipe> recipes = recipeService.searchWithFilters(keyword, cuisine, difficulty, minTime, maxTime, pageable);
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Recipe>>> getAllRecipesList() {
        List<Recipe> recipes = recipeService.findAll();
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Recipe>> getRecipeById(@PathVariable Long id) {
        return recipeService.findById(id)
                .map(recipe -> ResponseEntity.ok(ApiResponse.success(recipe)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("菜谱不存在")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Recipe>> createRecipe(@Valid @RequestBody Recipe recipe) {
        Recipe createdRecipe = recipeService.create(recipe);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("菜谱创建成功", createdRecipe));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Recipe>> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody Recipe recipeDetails) {
        try {
            Recipe updatedRecipe = recipeService.update(id, recipeDetails);
            return ResponseEntity.ok(ApiResponse.success("菜谱更新成功", updatedRecipe));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(@PathVariable Long id) {
        try {
            recipeService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("菜谱删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/cuisines")
    public ResponseEntity<ApiResponse<List<String>>> getAllCuisines() {
        List<String> cuisines = recipeService.getAllCuisines();
        return ResponseEntity.ok(ApiResponse.success(cuisines));
    }

    @GetMapping("/difficulties")
    public ResponseEntity<ApiResponse<List<String>>> getAllDifficulties() {
        List<String> difficulties = recipeService.getAllDifficulties();
        return ResponseEntity.ok(ApiResponse.success(difficulties));
    }

    @GetMapping("/random")
    public ResponseEntity<ApiResponse<Recipe>> getRandomRecipe() {
        Recipe randomRecipe = recipeService.getRandomRecipe();
        if (randomRecipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("暂无菜谱"));
        }
        return ResponseEntity.ok(ApiResponse.success(randomRecipe));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getRecipeCount() {
        long count = recipeService.countTotalRecipes();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
