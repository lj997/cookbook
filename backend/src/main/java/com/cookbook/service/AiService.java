package com.cookbook.service;

import com.cookbook.dto.GeneratedRecipeDto;
import com.cookbook.entity.AiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AiService {

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String RECIPE_GENERATION_PROMPT = """
            你是一个专业的菜谱助手。请根据用户提供的菜谱名称，生成完整的菜谱信息。
            菜谱名称: {recipeName}

            请以严格的JSON格式返回结果，不要添加任何其他说明文字。JSON格式如下：
            {
                "name": "菜谱名称",
                "coverImage": "请根据菜谱内容生成一个合适的图片描述，用于后续生成图片，例如：一盘色香味俱全的红烧肉",
                "cuisine": "菜系分类，可选值：川菜、粤菜、湘菜、鲁菜、苏菜、浙菜、闽菜、徽菜、家常菜、甜品、烘焙、汤品、主食、凉菜、西餐、日料、韩餐、泰餐、其他",
                "difficulty": "难度等级，只能是：简单、中等、困难",
                "cookingTime": 烹饪时长（分钟，整数）,
                "description": "菜谱描述，简单描述这道菜的特点、口感等",
                "ingredients": [
                    {"name": "食材名称", "quantity": "用量"}
                ],
                "steps": [
                    {"stepNumber": 1, "description": "步骤详细描述", "image": "可以留空字符串"}
                ]
            }

            注意：
            1. 菜系分类必须从给定的选项中选择一个最合适的
            2. 难度等级只能是：简单、中等、困难
            3. cookingTime必须是整数，表示分钟数
            4. ingredients至少需要3种食材
            5. steps至少需要3个步骤
            6. 所有字段都不能为空（除了image可以是空字符串）
            7. 请确保JSON格式完全正确，没有语法错误
            """;

    public GeneratedRecipeDto generateRecipe(String recipeName) {
        Optional<AiConfig> activeConfigOpt = aiConfigService.findActiveConfig();
        if (activeConfigOpt.isEmpty()) {
            throw new RuntimeException("请先配置并激活一个AI模型");
        }

        AiConfig activeConfig = activeConfigOpt.get();
        
        if (activeConfig.getApiKey() == null || activeConfig.getApiKey().trim().isEmpty()) {
            throw new RuntimeException("当前激活的AI配置未设置API Key，请先配置API Key");
        }

        ChatClient chatClient = createChatClient(activeConfig);

        String prompt = RECIPE_GENERATION_PROMPT.replace("{recipeName}", recipeName);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return parseRecipeResponse(response);
    }

    private ChatClient createChatClient(AiConfig config) {
        String baseUrl = config.getBaseUrl();
        String apiKey = config.getApiKey();
        String model = config.getModel();

        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(model)
                .withTemperature(0.7)
                .build();

        ChatModel chatModel = new OpenAiChatModel(openAiApi, options);

        return ChatClient.builder(chatModel).build();
    }

    private GeneratedRecipeDto parseRecipeResponse(String response) {
        try {
            String jsonString = extractJsonFromResponse(response);
            JsonNode root = objectMapper.readTree(jsonString);

            GeneratedRecipeDto recipe = new GeneratedRecipeDto();
            recipe.setName(getTextValue(root, "name"));
            recipe.setCoverImage(getTextValue(root, "coverImage"));
            recipe.setCuisine(getTextValue(root, "cuisine"));
            recipe.setDifficulty(getTextValue(root, "difficulty"));
            recipe.setCookingTime(getIntValue(root, "cookingTime"));
            recipe.setDescription(getTextValue(root, "description"));

            List<GeneratedRecipeDto.IngredientDto> ingredients = new ArrayList<>();
            JsonNode ingredientsNode = root.path("ingredients");
            if (ingredientsNode.isArray()) {
                for (JsonNode ingNode : ingredientsNode) {
                    GeneratedRecipeDto.IngredientDto ing = new GeneratedRecipeDto.IngredientDto();
                    ing.setName(getTextValue(ingNode, "name"));
                    ing.setQuantity(getTextValue(ingNode, "quantity"));
                    ingredients.add(ing);
                }
            }
            recipe.setIngredients(ingredients);

            List<GeneratedRecipeDto.StepDto> steps = new ArrayList<>();
            JsonNode stepsNode = root.path("steps");
            if (stepsNode.isArray()) {
                int stepNum = 1;
                for (JsonNode stepNode : stepsNode) {
                    GeneratedRecipeDto.StepDto step = new GeneratedRecipeDto.StepDto();
                    Integer num = getIntValue(stepNode, "stepNumber");
                    step.setStepNumber(num != null ? num : stepNum);
                    step.setDescription(getTextValue(stepNode, "description"));
                    step.setImage(getTextValue(stepNode, "image"));
                    steps.add(step);
                    stepNum++;
                }
            }
            recipe.setSteps(steps);

            return recipe;
        } catch (Exception e) {
            throw new RuntimeException("解析AI响应失败: " + e.getMessage(), e);
        }
    }

    private String extractJsonFromResponse(String response) {
        response = response.trim();
        
        int jsonStart = response.indexOf('{');
        int jsonEnd = response.lastIndexOf('}');
        
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1);
        }
        
        return response;
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isNull() || value.isMissingNode()) {
            return "";
        }
        if (value.isTextual()) {
            return value.asText().trim();
        }
        return value.toString().trim();
    }

    private Integer getIntValue(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isNull() || value.isMissingNode()) {
            return null;
        }
        if (value.isInt()) {
            return value.asInt();
        }
        if (value.isTextual()) {
            try {
                return Integer.parseInt(value.asText().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public boolean testConnection(AiConfig config) {
        try {
            ChatClient chatClient = createChatClient(config);
            
            String response = chatClient.prompt()
                    .user("请回复'连接成功'四个字")
                    .call()
                    .content();
            
            return response != null && !response.trim().isEmpty();
        } catch (Exception e) {
            throw new RuntimeException("连接测试失败: " + e.getMessage(), e);
        }
    }
}
