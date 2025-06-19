package com.example.covdecisive.demos.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LLMTestCaseService {

    private static final String API_KEY = "1ab7448c015a41b6bdb661a86807bb3c.PBzf1VoUZRH8hlac"; // 替换为你自己的
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    public String generateTestCase(String javaCode) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        String prompt = "你是一个 Java 单元测试专家，请为以下方法编写基于 JUnit 的完整测试类，确保覆盖其主要分支和条件（条件/分支覆盖）。\n"
                + "输出格式必须为标准 Java 代码，并使用如下包裹：\n"
                + "```java\n// 测试类代码\n```\n"
                + "注意不要添加任何解释说明、标题或注释文字。请务必直接返回完整的java代码。\n"
                + "以下是被测试的方法：\n"
                + javaCode;


        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "glm-4");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

        Map<String, Object> responseMap = mapper.readValue(response.getBody(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            return "LLM API 返回内容为空";
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}
