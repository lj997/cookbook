package com.cookbook.controller;

import com.cookbook.entity.ShoppingItem;
import com.cookbook.payload.ApiResponse;
import com.cookbook.service.ShoppingItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shopping")
public class ShoppingItemController {

    @Autowired
    private ShoppingItemService shoppingItemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShoppingItem>>> getAllShoppingItems() {
        List<ShoppingItem> items = shoppingItemService.findAll();
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/unpurchased")
    public ResponseEntity<ApiResponse<List<ShoppingItem>>> getUnpurchasedItems() {
        List<ShoppingItem> items = shoppingItemService.findUnpurchased();
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/purchased")
    public ResponseEntity<ApiResponse<List<ShoppingItem>>> getPurchasedItems() {
        List<ShoppingItem> items = shoppingItemService.findPurchased();
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShoppingItem>> getShoppingItemById(@PathVariable Long id) {
        return shoppingItemService.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(item)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("购物项不存在")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShoppingItem>> createShoppingItem(@Valid @RequestBody ShoppingItem shoppingItem) {
        ShoppingItem createdItem = shoppingItemService.create(shoppingItem);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("购物项添加成功", createdItem));
    }

    @PostMapping("/from-recipe/{recipeId}")
    public ResponseEntity<ApiResponse<List<ShoppingItem>>> addFromRecipe(@PathVariable Long recipeId) {
        try {
            List<ShoppingItem> addedItems = shoppingItemService.addIngredientsFromRecipe(recipeId);
            return ResponseEntity.ok(ApiResponse.success("已从菜谱添加食材到购物清单", addedItems));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShoppingItem>> updateShoppingItem(
            @PathVariable Long id,
            @Valid @RequestBody ShoppingItem itemDetails) {
        try {
            ShoppingItem updatedItem = shoppingItemService.update(id, itemDetails);
            return ResponseEntity.ok(ApiResponse.success("购物项更新成功", updatedItem));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ShoppingItem>> togglePurchased(@PathVariable Long id) {
        try {
            ShoppingItem updatedItem = shoppingItemService.togglePurchased(id);
            return ResponseEntity.ok(ApiResponse.success(updatedItem.getPurchased() ? "已标记为已购买" : "已标记为未购买", updatedItem));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShoppingItem(@PathVariable Long id) {
        try {
            shoppingItemService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("购物项删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/purchased")
    public ResponseEntity<ApiResponse<Void>> deletePurchasedItems() {
        shoppingItemService.deletePurchased();
        return ResponseEntity.ok(ApiResponse.success("已清空已购买的商品", null));
    }

    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Void>> clearAllItems() {
        shoppingItemService.clearAll();
        return ResponseEntity.ok(ApiResponse.success("已清空所有购物项", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getShoppingStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("unpurchased", shoppingItemService.countUnpurchased());
        stats.put("purchased", shoppingItemService.countPurchased());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
