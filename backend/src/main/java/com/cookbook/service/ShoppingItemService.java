package com.cookbook.service;

import com.cookbook.entity.Ingredient;
import com.cookbook.entity.Recipe;
import com.cookbook.entity.ShoppingItem;
import com.cookbook.repository.RecipeRepository;
import com.cookbook.repository.ShoppingItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShoppingItemService {

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    public List<ShoppingItem> findAll() {
        return shoppingItemRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ShoppingItem> findUnpurchased() {
        return shoppingItemRepository.findByPurchasedFalseOrderByCreatedAtDesc();
    }

    public List<ShoppingItem> findPurchased() {
        return shoppingItemRepository.findByPurchasedTrueOrderByCreatedAtDesc();
    }

    public Optional<ShoppingItem> findById(Long id) {
        return shoppingItemRepository.findById(id);
    }

    @Transactional
    public ShoppingItem create(ShoppingItem shoppingItem) {
        if (!shoppingItemRepository.existsByNameIgnoreCaseAndPurchasedFalse(shoppingItem.getName())) {
            return shoppingItemRepository.save(shoppingItem);
        }
        return shoppingItem;
    }

    @Transactional
    public List<ShoppingItem> addIngredientsFromRecipe(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("菜谱不存在: " + recipeId));
        
        List<ShoppingItem> addedItems = new ArrayList<>();
        
        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (!shoppingItemRepository.existsByNameIgnoreCaseAndPurchasedFalse(ingredient.getName())) {
                    ShoppingItem item = new ShoppingItem();
                    item.setName(ingredient.getName());
                    item.setQuantity(ingredient.getQuantity());
                    item.setPurchased(false);
                    addedItems.add(shoppingItemRepository.save(item));
                }
            }
        }
        
        return addedItems;
    }

    @Transactional
    public ShoppingItem update(Long id, ShoppingItem itemDetails) {
        ShoppingItem item = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("购物项不存在: " + id));
        
        item.setName(itemDetails.getName());
        item.setQuantity(itemDetails.getQuantity());
        
        return shoppingItemRepository.save(item);
    }

    @Transactional
    public ShoppingItem togglePurchased(Long id) {
        ShoppingItem item = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("购物项不存在: " + id));
        
        item.setPurchased(!item.getPurchased());
        return shoppingItemRepository.save(item);
    }

    @Transactional
    public void delete(Long id) {
        ShoppingItem item = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("购物项不存在: " + id));
        shoppingItemRepository.delete(item);
    }

    @Transactional
    public void deletePurchased() {
        List<ShoppingItem> purchasedItems = shoppingItemRepository.findByPurchasedTrueOrderByCreatedAtDesc();
        shoppingItemRepository.deleteAll(purchasedItems);
    }

    @Transactional
    public void clearAll() {
        shoppingItemRepository.deleteAll();
    }

    public long countUnpurchased() {
        return shoppingItemRepository.countByPurchasedFalse();
    }

    public long countPurchased() {
        return shoppingItemRepository.countByPurchasedTrue();
    }
}
