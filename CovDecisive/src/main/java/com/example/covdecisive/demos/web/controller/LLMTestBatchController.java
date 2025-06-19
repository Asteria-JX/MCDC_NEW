package com.example.covdecisive.demos.web.controller;

import com.example.covdecisive.demos.web.dto.TestCaseResultDTO;
import com.example.covdecisive.demos.web.model.SourceCode;
import com.example.covdecisive.demos.web.service.LLMTestCaseService;
import com.example.covdecisive.demos.web.service.SourceCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@Api(tags = "API接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LLMTestBatchController {

    @Autowired
    private SourceCodeService sourceCodeService;

    @Autowired
    private LLMTestCaseService llmTestCaseService;

    @ApiOperation("批量生成指定项目下所有 java 文件的测试用例")
    @GetMapping("/generateAllTests")
    public ResponseEntity<List<TestCaseResultDTO>> generateAllTestCases(@RequestParam int programId) {
        try {
            List<SourceCode> sourceCodes = sourceCodeService.getJavaFilesNeedingTests(programId);
            List<TestCaseResultDTO> results = new ArrayList<>();

            for (SourceCode src : sourceCodes) {
                String code = src.getCodeContent();
                if (code != null && !code.trim().isEmpty()) {
                    try {
                        String testCode = llmTestCaseService.generateTestCase(code);

                        // 推测测试类名，如: AnnotationUtils.java -> AnnotationUtilsTest.java
                        String originalFileName = Paths.get(src.getFilePath()).getFileName().toString();
                        String className = originalFileName.replace(".java", "Test.java");

                        results.add(new TestCaseResultDTO(className, testCode));
                    } catch (Exception e) {
                        results.add(new TestCaseResultDTO("ERROR_" + src.getFilePath(), "// 生成失败: " + e.getMessage()));
                    }
                }
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    List.of(new TestCaseResultDTO("SYSTEM_ERROR", "系统内部错误：" + e.getMessage()))
            );
        }
    }
}