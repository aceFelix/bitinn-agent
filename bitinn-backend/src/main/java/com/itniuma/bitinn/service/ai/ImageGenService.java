package com.itniuma.bitinn.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * 图片生成服务
 * 对接阿里云 DashScope 多模态生成 API，支持纯文本生图和图片编辑（参考图+文字指令）。
 *
 * @author aceFelix
 */
@Slf4j
@Service
public class ImageGenService {

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ImageGenService(@Value("${spring.ai.dashscope.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        log.info("[ImageGen] 初始化, apiKey长度={}, 前4位={}", 
                apiKey != null ? apiKey.length() : 0,
                apiKey != null && apiKey.length() >= 4 ? apiKey.substring(0, 4) + "***" : "null");
    }

    /**
     * 纯文本生图
     */
    public String generateImage(String prompt) {
        try {
            return generate(prompt, null);
        } catch (Exception e) {
            log.error("[ImageGen] 生图失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 图片编辑：传入参考图 + 文字指令
     */
    public String generateImage(String prompt, List<String> imageBase64List) {
        try {
            return generate(prompt, imageBase64List);
        } catch (Exception e) {
            log.error("[ImageGen] 图片编辑失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private String generate(String prompt, List<String> imageBase64List) throws Exception {
        ArrayNode contentArray = objectMapper.createArrayNode();

        if (imageBase64List != null && !imageBase64List.isEmpty()) {
            for (String base64 : imageBase64List) {
                ObjectNode imageItem = objectMapper.createObjectNode();
                imageItem.put("image", base64);
                contentArray.add(imageItem);
            }
            log.info("[ImageGen] 图片编辑模式, 参考图数量={}, prompt={}",
                    imageBase64List.size(), prompt.length() > 50 ? prompt.substring(0, 50) + "..." : prompt);
        } else {
            log.info("[ImageGen] 纯文本生图, prompt={}", prompt.length() > 50 ? prompt.substring(0, 50) + "..." : prompt);
        }

        ObjectNode textItem = objectMapper.createObjectNode();
        textItem.put("text", prompt);
        contentArray.add(textItem);

        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.set("content", contentArray);

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(message);

        ObjectNode input = objectMapper.createObjectNode();
        input.set("messages", messages);

        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("n", 1);
        parameters.put("size", "1024*1024");
        parameters.put("watermark", false);
        parameters.put("negative_prompt", "");
        parameters.put("prompt_extend", true);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "qwen-image-2.0-pro");
        root.set("input", input);
        root.set("parameters", parameters);

        String body = root.toString();
        log.info("[ImageGen] 请求: model=qwen-image-2.0-pro, prompt={}", prompt.length() > 50 ? prompt.substring(0, 50) + "..." : prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(120))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("[ImageGen] 响应: status={}", response.statusCode());

        if (response.statusCode() != 200) {
            log.error("[ImageGen] 请求失败: status={}, body={}", response.statusCode(), response.body());
            return null;
        }

        JsonNode respRoot = objectMapper.readTree(response.body());
        JsonNode output = respRoot.path("output");
        if (output.isMissingNode() || output.isNull()) {
            log.error("[ImageGen] 响应无output: {}", response.body());
            return null;
        }

        JsonNode choices = output.path("choices");
        if (!choices.isArray() || choices.size() == 0) {
            log.error("[ImageGen] 响应无choices: {}", response.body());
            return null;
        }

        JsonNode messageContent = choices.get(0).path("message").path("content");
        if (!messageContent.isArray()) {
            log.error("[ImageGen] 响应content格式异常: {}", response.body());
            return null;
        }

        for (JsonNode item : messageContent) {
            String imageUrl = item.path("image").asText();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                log.info("[ImageGen] 生图成功: url={}", imageUrl);
                return imageUrl;
            }
        }

        log.error("[ImageGen] 响应中未找到图片URL: {}", response.body());
        return null;
    }
}
