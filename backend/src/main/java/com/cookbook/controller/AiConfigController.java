package com.cookbook.controller;

import com.cookbook.entity.AiConfig;
import com.cookbook.payload.ApiResponse;
import com.cookbook.service.AiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-configs")
public class AiConfigController {

    @Autowired
    private AiConfigService aiConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiConfig>>> getAllConfigs() {
        List<AiConfig> configs = aiConfigService.findAll();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<AiConfig>> getActiveConfig() {
        return aiConfigService.findActiveConfig()
                .map(config -> ResponseEntity.ok(ApiResponse.success(config)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("未找到激活的AI配置")));
    }

    @GetMapping("/presets")
    public ResponseEntity<ApiResponse<List<AiConfig>>> getPresets() {
        List<AiConfig> presets = aiConfigService.findPresets();
        return ResponseEntity.ok(ApiResponse.success(presets));
    }

    @GetMapping("/custom")
    public ResponseEntity<ApiResponse<List<AiConfig>>> getCustomConfigs() {
        List<AiConfig> configs = aiConfigService.findCustomConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AiConfig>> getConfigById(@PathVariable Long id) {
        return aiConfigService.findById(id)
                .map(config -> ResponseEntity.ok(ApiResponse.success(config)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("AI配置不存在")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiConfig>> createConfig(@RequestBody AiConfig aiConfig) {
        try {
            AiConfig createdConfig = aiConfigService.create(aiConfig);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("AI配置创建成功", createdConfig));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("创建失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AiConfig>> updateConfig(
            @PathVariable Long id,
            @RequestBody AiConfig aiConfigDetails) {
        try {
            AiConfig updatedConfig = aiConfigService.update(id, aiConfigDetails);
            return ResponseEntity.ok(ApiResponse.success("AI配置更新成功", updatedConfig));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        try {
            aiConfigService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("AI配置删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<AiConfig>> activateConfig(@PathVariable Long id) {
        try {
            AiConfig activatedConfig = aiConfigService.activate(id);
            return ResponseEntity.ok(ApiResponse.success("AI配置已激活", activatedConfig));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/presets/{id}/duplicate")
    public ResponseEntity<ApiResponse<AiConfig>> duplicatePreset(@PathVariable Long id) {
        try {
            AiConfig duplicatedConfig = aiConfigService.duplicatePreset(id);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("预设配置已复制", duplicatedConfig));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
