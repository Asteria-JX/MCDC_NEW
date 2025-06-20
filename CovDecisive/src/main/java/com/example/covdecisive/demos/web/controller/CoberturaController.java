package com.example.covdecisive.demos.web.controller;

//import com.example.covdecisive.demos.web.service.CoberturaService;
import com.example.covdecisive.demos.web.service.CoverageService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Api(tags = "API接口")
@RestController
//Spring 中的一个注解，用于支持跨域请求
@CrossOrigin(origins = "*", maxAge = 3600)
public class CoberturaController {



    @Autowired
    private CoverageService coverageService;

    @GetMapping("/Analysis")
    public void analysis(@RequestParam Integer programId, @RequestParam Integer testResourceId,@RequestParam Integer way) {
        try {
//            coberturaService.generate(programId,testResourceId,way);

            // 1. 修正类名（去掉.java后缀，添加包名）
            String mainClassName = "com.example.Calculator";  // 改为完整类名
            String testClassName = "com.example.CalculatorTest";

// 2. 为类代码添加包声明
            String mainClassCode = "package com.example;\n\n" +  // 添加包声明
                    "public class Calculator {\n" +
                    "    public int add(int a, int b) {\n" +
                    "        return a + b;\n" +
                    "    }\n" +
                    // ... 其他方法保持不变
                    "}";

            String testClassCode = "package com.example;\n\n" +  // 添加包声明
                    "import org.junit.Test;\n" +
                    "import static org.junit.Assert.*;\n\n" +
                    "public class CalculatorTest {\n" +
                    // ... 测试代码保持不变
                    "}";

                // 3. 调用服务（注意参数顺序）
            coverageService.generateReport(
                    testClassName,    // 测试类名
                    testClassCode,    // 测试类代码
                    mainClassName,    // 主类名
                    mainClassCode     // 主类代码
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
