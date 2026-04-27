package com.cookbook.repository;

import com.cookbook.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findAllByOrderByCreatedAtDesc();

    Page<Recipe> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE " +
           "(:cuisine IS NULL OR :cuisine = '' OR r.cuisine = :cuisine) AND " +
           "(:difficulty IS NULL OR :difficulty = '' OR r.difficulty = :difficulty) AND " +
           "(:minTime IS NULL OR r.cookingTime >= :minTime) AND " +
           "(:maxTime IS NULL OR r.cookingTime <= :maxTime)")
    Page<Recipe> findByFilters(
            @Param("cuisine") String cuisine,
            @Param("difficulty") String difficulty,
            @Param("minTime") Integer minTime,
            @Param("maxTime") Integer maxTime,
            Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT i FROM Ingredient i WHERE i.recipe = r AND LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Recipe> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT i FROM Ingredient i WHERE i.recipe = r AND LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
           "(:cuisine IS NULL OR :cuisine = '' OR r.cuisine = :cuisine) AND " +
           "(:difficulty IS NULL OR :difficulty = '' OR r.difficulty = :difficulty) AND " +
           "(:minTime IS NULL OR r.cookingTime >= :minTime) AND " +
           "(:maxTime IS NULL OR r.cookingTime <= :maxTime)")
    Page<Recipe> searchWithFilters(
            @Param("keyword") String keyword,
            @Param("cuisine") String cuisine,
            @Param("difficulty") String difficulty,
            @Param("minTime") Integer minTime,
            @Param("maxTime") Integer maxTime,
            Pageable pageable);

    @Query("SELECT DISTINCT r.cuisine FROM Recipe r ORDER BY r.cuisine")
    List<String> findAllDistinctCuisines();

    @Query("SELECT DISTINCT r.difficulty FROM Recipe r ORDER BY r.difficulty")
    List<String> findAllDistinctDifficulties();

    @Query(value = "SELECT * FROM recipes ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Recipe findRandomRecipe();

    @Query("SELECT COUNT(r) FROM Recipe r")
    long countTotalRecipes();
}
