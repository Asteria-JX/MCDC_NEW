package com.example.covdecisive.demos.web.controller;

import com.example.covdecisive.demos.web.generator.MCDCJavaGenerator;
import com.example.covdecisive.demos.web.generator.MCDCPythonGenerator;
import com.example.covdecisive.demos.web.model.TestCaseDTO; // Import the new DTO
import com.example.covdecisive.demos.web.service.ProgramService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RestController
@Api(tags = "API接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestCaseGenerationController {
    @Autowired
    private ProgramService programService;

    @GetMapping("/generateTestCase")
    // 修改返回类型为 ResponseEntity<List<TestCaseDTO>>
    public ResponseEntity<?> generateMCDCTestCases(@RequestParam int programId) {
        try {
            List<SourceCodeFile> sourceFiles = programService.getSourceFilesByProgramId(programId);

            if (sourceFiles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到源码文件");
            }

            // 根据第一个文件判断语言类型
            String language = sourceFiles.get(0).getFilePath().endsWith(".py") ? "python" : "java";

            List<String> allCode = sourceFiles.stream()
                    .map(SourceCodeFile::getCodeContent)
                    .collect(Collectors.toList());

            // 修改这里，现在直接接收 List<TestCaseDTO>
            List<TestCaseDTO> generatedTestCases;

            System.out.println(language);
            if ("java".equals(language)) {
                generatedTestCases = MCDCJavaGenerator.generate(allCode);
            } else {
                generatedTestCases = MCDCPythonGenerator.generate(allCode);
            }
            System.out.println(generatedTestCases); // 这会打印出对象列表的 toString()
            return ResponseEntity.ok(generatedTestCases); // Spring会自动将List<TestCaseDTO>序列化为JSON数组

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("生成测试用例失败：" + e.getMessage());
        }
    }
}