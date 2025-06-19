package com.example.covdecisive.demos.web.controller;

import com.example.covdecisive.demos.web.service.GeneratePythonTestService;
import com.example.covdecisive.demos.web.service.GenerateTestByRDService; // 引用新的 Service 类名
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController // @RestController 是 @Controller 和 @ResponseBody 的组合
@Api(tags = "API接口") // Swagger API 标签
@CrossOrigin(origins = "*", maxAge = 3600) // 允许跨域请求
public class TestCaseGenerationController { // 类名已修改为 TestResourceController

    @Autowired
    private GenerateTestByRDService generateTestByRDService; // 注入新的 Service 类

    @GetMapping("/generateTestCase") // 前端请求路径
    @ApiOperation("根据programId生成Randoop测试用例并保存") // Swagger 操作描述
    public ResponseEntity<String> generateTestCases_ranDoop(@RequestParam int programId,@RequestParam int userId) {
        System.out.println("接收到生成程序ID为 " + programId + " 的测试用例请求。");
        try {
            String result = generateTestByRDService.generateTestCasesWithRandoop(programId,userId);
            return ResponseEntity.ok(result); // 返回成功信息
        } catch (IllegalArgumentException e) {
            System.err.println("错误：请求参数无效 - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (IllegalStateException e) {
            System.err.println("错误：业务逻辑异常 - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (IOException | InterruptedException e) {
            System.err.println("错误：Randoop 执行或文件操作期间出错 - " + e.getMessage());
            e.printStackTrace(); // 打印完整堆栈跟踪以便调试
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("由于内部服务器错误，生成测试用例失败: " + e.getMessage()); // 500 Internal Server Error
        } catch (RuntimeException e) {
            System.err.println("错误：测试用例生成期间发生意外错误 - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("发生意外错误: " + e.getMessage()); // 500 Internal Server Error
        } finally {
            System.out.println("--- 程序ID " + programId + " 的测试用例生成请求处理结束 ---");
        }
    }

    @Autowired
    private GeneratePythonTestService pynguinService;

    @GetMapping("/generateProjectTests") // 前端请求路径
    @ApiOperation("根据programId生成python测试用例并保存") // Swagger 操作描述
    public ResponseEntity<String> generateProjectTests(@RequestParam int programId,@RequestParam int userId) {
        try {
            String result = pynguinService.generatePynguinTestsForProject(programId,userId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("请求错误: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("生成 Pynguin 项目测试用例时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("测试用例生成失败: " + e.getMessage());
        }
    }
}