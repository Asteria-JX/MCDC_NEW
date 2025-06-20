package com.example.covdecisive.demos.web.controller;


import com.example.covdecisive.demos.web.dto.TestCaseResultDTO;
import com.example.covdecisive.demos.web.model.SourceCode;
import com.example.covdecisive.demos.web.service.LLMTestCaseService;
import com.example.covdecisive.demos.web.service.SourceCodeService;
import com.example.covdecisive.demos.web.service.TestProgramService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api(tags = "API接口")
@RestController
//Spring 中的一个注解，用于支持跨域请求
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestProgramsController {

    @Autowired
    private LLMTestCaseService llmTestCaseService;

    @Autowired
    private SourceCodeService sourceCodeService;

    @Autowired
    private TestProgramService testProgramService;


    @ApiOperation("LLM 生成测试用例并存入数据库")
    @PostMapping("/LLMgenerate")
    public ResponseEntity<Map<String, Object>> generateLLMTestCases(@RequestBody Map<String, Object> requestBody) {
        int programId = (int) requestBody.get("programId");
        int userId = (int) requestBody.get("userId");

        Map<String, Object> response = new HashMap<>();
        try {
            List<SourceCode> sourceCodes = sourceCodeService.getJavaFilesNeedingTests(programId);
            List<TestCaseResultDTO> testCases = new ArrayList<>();

            for (SourceCode src : sourceCodes) {
                String code = src.getCodeContent();
                if (code != null && !code.trim().isEmpty()) {
                    String raw = llmTestCaseService.generateTestCase(code);
                    // 控制台打印原始返回内容，便于调试
                    System.out.println("===== LLM 原始返回内容 START =====");
                    System.out.println(raw);
                    System.out.println("===== LLM 原始返回内容 END =====");
                    String testCode = extractTestCodeFromLLM(raw);
                    if (!testCode.isEmpty() && testCode.contains("@Test")) {
                        String fileName = Paths.get(src.getFilePath()).getFileName().toString().replace(".java", "Test.java");
                        testCases.add(new TestCaseResultDTO(fileName, testCode));
                    }
                }
            }

            int testProgramId = testProgramService.storeGeneratedTestCases(userId, programId, testCases);
            response.put("message", "测试用例已成功生成并保存，程序 ID: " + testProgramId+ "。共生成 " + testCases.size() + " 个测试文件。");
//            response.put("success", true);
//            "测试用例已成功生成并保存，程序 ID: " + programId + "。共生成 " + generatedTestFiles.size() + " 个测试文件。"
//            response.put("测试用例已成功生成并保存，程序 ID: " + testProgramId + "。共生成 " + testCases.size() + " 个测试文件。");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "生成失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }

    }


    /**
     * 提取 LLM 返回内容中的 Java 测试代码
     */
    public String extractTestCodeFromLLM(String raw) {
        String cleaned = raw.trim();

        // STEP 1：提取 ```java ... ```
        int start = cleaned.indexOf("```java");
        int end = cleaned.lastIndexOf("```");
        if (start != -1 && end != -1 && start < end) {
            cleaned = cleaned.substring(start + 7, end).trim();
        } else {
            int importIndex = cleaned.indexOf("import ");
            if (importIndex != -1) {
                cleaned = cleaned.substring(importIndex).trim();
            } else {
                int classIndex = cleaned.indexOf("public class");
                if (classIndex != -1) {
                    cleaned = cleaned.substring(classIndex).trim();
                }
            }
        }

        // STEP 2：提取所有 @Test 方法块（完整性判断）
        StringBuilder methodBody = new StringBuilder();
        Pattern methodPattern = Pattern.compile("@Test\\s*\\n?.*?\\{[\\s\\S]*?\\n\\s*\\}", Pattern.MULTILINE);
        Matcher matcher = methodPattern.matcher(cleaned);
        while (matcher.find()) {
            String methodBlock = matcher.group();
            if (methodBlock.contains("public void") && methodBlock.trim().endsWith("}")) {
                methodBody.append("\n\n").append(methodBlock);
            }
        }

        // STEP 3：提取 class 头
        String classHeader = "";
        Matcher classHeaderMatcher = Pattern.compile("public class .*?\\{", Pattern.DOTALL).matcher(cleaned);
        if (classHeaderMatcher.find()) {
            classHeader = classHeaderMatcher.group();
        } else {
            return ""; // 无 class 头，跳过
        }

        // STEP 4：组装代码
        String testCode = classHeader + "\n" + methodBody;

        // STEP 5：补全未闭合的大括号
        int openCount = countOccurrences(testCode, '{');
        int closeCount = countOccurrences(testCode, '}');
        int toAppend = openCount - closeCount;
        while (toAppend-- > 0) {
            testCode += "\n}";
        }

        // STEP 6：加上 import 段（如果存在）
        StringBuilder finalCode = new StringBuilder();
        Matcher importMatcher = Pattern.compile("(?:(package .*?;\\s*)?(import .*?;\\s*)+)", Pattern.DOTALL).matcher(raw);
        if (importMatcher.find()) {
            finalCode.append(importMatcher.group());
        }

        finalCode.append("\n").append(testCode.trim());
        return finalCode.toString().trim();
    }

    // 工具方法：统计字符出现次数
    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }



}
