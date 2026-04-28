package com.cookbook.controller;

import com.cookbook.dto.GeneratedRecipeDto;
import com.cookbook.dto.RecipeGenerationRequest;
import com.cookbook.entity.AiConfig;
import com.cookbook.payload.ApiResponse;
import com.cookbook.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-recipe")
public class AiRecipeController {

    @Autowired
    private AiService aiService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GeneratedRecipeDto>> generateRecipe(
            @RequestBody RecipeGenerationRequest request) {
        
        if (request.getRecipeName() == null || request.getRecipeName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请输入菜谱名称"));
        }

        try {
            GeneratedRecipeDto recipe = aiService.generateRecipe(request.getRecipeName().trim());
            return ResponseEntity.ok(ApiResponse.success("菜谱生成成功", recipe));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/test-connection")
    public ResponseEntity<ApiResponse<Boolean>> testConnection(@RequestBody AiConfig config) {
        if (config.getBaseUrl() == null || config.getBaseUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请输入API地址"));
        }
        if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请输入API Key"));
        }
        if (config.getModel() == null || config.getModel().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请输入模型名称"));
        }

        try {
            boolean success = aiService.testConnection(config);
            return ResponseEntity.ok(ApiResponse.success(
                    success ? "连接测试成功" : "连接测试失败", 
                    success));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
