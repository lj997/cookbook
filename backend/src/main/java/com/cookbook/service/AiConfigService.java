package com.cookbook.service;

import com.cookbook.entity.AiConfig;
import com.cookbook.repository.AiConfigRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AiConfigService {

    @Autowired
    private AiConfigRepository aiConfigRepository;

    @PostConstruct
    public void initPresets() {
        if (aiConfigRepository.count() == 0) {
            createDefaultPresets();
        }
    }

    private void createDefaultPresets() {
        AiConfig zhipuConfig = new AiConfig();
        zhipuConfig.setName("智谱 AI (Zhipu)");
        zhipuConfig.setBaseUrl("https://open.bigmodel.cn/api/paas/v4/");
        zhipuConfig.setApiKey("");
        zhipuConfig.setModel("glm-4-flash");
        zhipuConfig.setDescription("智谱AI大模型，支持glm-4、glm-4-flash等模型");
        zhipuConfig.setIsActive(false);
        zhipuConfig.setIsPreset(true);
        zhipuConfig.setProvider("zhipu");
        aiConfigRepository.save(zhipuConfig);

        AiConfig deepseekConfig = new AiConfig();
        deepseekConfig.setName("DeepSeek AI");
        deepseekConfig.setBaseUrl("https://api.deepseek.com/v1/");
        deepseekConfig.setApiKey("");
        deepseekConfig.setModel("deepseek-chat");
        deepseekConfig.setDescription("DeepSeek大模型，支持deepseek-chat、deepseek-coder等模型");
        deepseekConfig.setIsActive(false);
        deepseekConfig.setIsPreset(true);
        deepseekConfig.setProvider("deepseek");
        aiConfigRepository.save(deepseekConfig);

        AiConfig openaiConfig = new AiConfig();
        openaiConfig.setName("OpenAI");
        openaiConfig.setBaseUrl("https://api.openai.com/v1/");
        openaiConfig.setApiKey("");
        openaiConfig.setModel("gpt-3.5-turbo");
        openaiConfig.setDescription("OpenAI官方API，支持gpt-3.5-turbo、gpt-4等模型");
        openaiConfig.setIsActive(false);
        openaiConfig.setIsPreset(true);
        openaiConfig.setProvider("openai");
        aiConfigRepository.save(openaiConfig);
    }

    public List<AiConfig> findAll() {
        return aiConfigRepository.findAll();
    }

    public Optional<AiConfig> findById(Long id) {
        return aiConfigRepository.findById(id);
    }

    public Optional<AiConfig> findActiveConfig() {
        return aiConfigRepository.findByIsActiveTrue();
    }

    public List<AiConfig> findPresets() {
        return aiConfigRepository.findByIsPresetTrue();
    }

    public List<AiConfig> findCustomConfigs() {
        return aiConfigRepository.findByIsPresetFalse();
    }

    @Transactional
    public AiConfig create(AiConfig aiConfig) {
        if (aiConfig.getIsActive() == null) {
            aiConfig.setIsActive(false);
        }
        if (aiConfig.getIsPreset() == null) {
            aiConfig.setIsPreset(false);
        }
        
        if (Boolean.TRUE.equals(aiConfig.getIsActive())) {
            deactivateAllConfigs();
        }
        
        return aiConfigRepository.save(aiConfig);
    }

    @Transactional
    public AiConfig update(Long id, AiConfig aiConfigDetails) {
        AiConfig aiConfig = aiConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI配置不存在: " + id));

        aiConfig.setName(aiConfigDetails.getName());
        aiConfig.setBaseUrl(aiConfigDetails.getBaseUrl());
        aiConfig.setApiKey(aiConfigDetails.getApiKey());
        aiConfig.setModel(aiConfigDetails.getModel());
        aiConfig.setDescription(aiConfigDetails.getDescription());
        aiConfig.setProvider(aiConfigDetails.getProvider());

        if (Boolean.TRUE.equals(aiConfigDetails.getIsActive()) && !Boolean.TRUE.equals(aiConfig.getIsActive())) {
            deactivateAllConfigs();
            aiConfig.setIsActive(true);
        } else if (Boolean.FALSE.equals(aiConfigDetails.getIsActive()) && Boolean.TRUE.equals(aiConfig.getIsActive())) {
            aiConfig.setIsActive(false);
        }

        return aiConfigRepository.save(aiConfig);
    }

    @Transactional
    public void delete(Long id) {
        AiConfig aiConfig = aiConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI配置不存在: " + id));
        
        if (Boolean.TRUE.equals(aiConfig.getIsPreset())) {
            throw new RuntimeException("不能删除预设配置");
        }
        
        aiConfigRepository.delete(aiConfig);
    }

    @Transactional
    public AiConfig activate(Long id) {
        AiConfig aiConfig = aiConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI配置不存在: " + id));
        
        deactivateAllConfigs();
        aiConfig.setIsActive(true);
        
        return aiConfigRepository.save(aiConfig);
    }

    private void deactivateAllConfigs() {
        List<AiConfig> allConfigs = aiConfigRepository.findAll();
        for (AiConfig config : allConfigs) {
            if (Boolean.TRUE.equals(config.getIsActive())) {
                config.setIsActive(false);
                aiConfigRepository.save(config);
            }
        }
    }

    @Transactional
    public AiConfig duplicatePreset(Long presetId) {
        AiConfig preset = aiConfigRepository.findById(presetId)
                .orElseThrow(() -> new RuntimeException("预设配置不存在: " + presetId));
        
        if (!Boolean.TRUE.equals(preset.getIsPreset())) {
            throw new RuntimeException("该配置不是预设配置");
        }

        AiConfig newConfig = new AiConfig();
        newConfig.setName(preset.getName() + " (副本)");
        newConfig.setBaseUrl(preset.getBaseUrl());
        newConfig.setApiKey("");
        newConfig.setModel(preset.getModel());
        newConfig.setDescription(preset.getDescription());
        newConfig.setIsActive(false);
        newConfig.setIsPreset(false);
        newConfig.setProvider(preset.getProvider());

        return aiConfigRepository.save(newConfig);
    }
}
