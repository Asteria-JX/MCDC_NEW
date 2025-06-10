package com.example.covdecisive.demos.web.controller;
import com.example.covdecisive.demos.web.model.TestResource;
import com.example.covdecisive.demos.web.service.*;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.covdecisive.demos.web.model.Program;
import com.example.covdecisive.demos.web.model.SourceCode;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Api(tags = "API接口")
@RestController
//Spring 中的一个注解，用于支持跨域请求
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProgramController {
    @Autowired
    private ProgramService programService;

    @Autowired
    private SourceCodeService sourceCodeService;

    @Autowired
    private CodeInstrumentationService codeInstrumentationService;

    @Autowired
    private JavaExecutionService javaExecutionService;

    @Autowired
    private CodeService codeService;

    @Autowired
    private TestResourceService testResourceService;

    @GetMapping("/getProgramsByUserID/{userID}")
    public List<Program> getProgramsByUserID(@PathVariable("userID") int userID) {
        //System.out.println(programService.getAll());
        return programService.getProgramsByUserID(userID);
    }

    @GetMapping("/all")
    public List<Program> getAllPrograms(@RequestParam Integer userId) {
        return programService.getAll(userId);
    }

    @PostMapping("/uploadProject")
    public void uploadProject(@RequestParam("programName") String programName,
                              @RequestParam("user_id") Integer user_id,
                              @RequestParam("files") List<MultipartFile> files) throws IOException {

        Program program = new Program();
        program.setProgramName(programName);
        program.setUserId(user_id);
        program.setVersion("1.0");
        program.setDescription("上传生成");
        programService.insert(program); // 插入 programs 表

        int programId = program.getProgramId(); // 获取插入后的主键

        for (MultipartFile file : files) {
            String path = file.getOriginalFilename(); // 获取带路径的文件名
            String content = new String(file.getBytes());
            SourceCode code = new SourceCode();
            code.setProgramId(programId);
            code.setFilePath(path);
            code.setCodeContent(content);
            sourceCodeService.insert(code);
        }
    }

    @PostMapping("/instrumentFile")
    public ResponseEntity<byte[]> instrumentFile(@RequestParam("file") MultipartFile file, @RequestParam("className") String className) {
        try {
            // 获取文件内容
            byte[] fileContent = file.getBytes();

            // 调用插桩服务
            String base64EncodedBytecode = codeInstrumentationService.instrumentAndPrintBytecode(className, fileContent);

            // 将Base64编码的字节码解码为字节数组
            byte[] instrumentedBytecode = Base64.getDecoder().decode(base64EncodedBytecode);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "instrumented_" + className.replace(".", "_") + ".class");
            System.out.println("返回前端");
            return new ResponseEntity<>(instrumentedBytecode, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 定义请求体
    static class RunProjectRequest {
        private int programId;

        // Getters and Setters
        public int getProgramId() {
            return programId;
        }

        public void setProgramId(int programId) {
            this.programId = programId;
        }
    }

    /**
     * 运行指定ID的Java项目并返回执行结果。
     * 前端通过 POST 请求发送 { "programId": 123 }
     *
     * @param request 包含 programId 的请求体
     * @return 包含运行日志的 JSON 响应
     */
    @PostMapping("/runProject")
    public ResponseEntity<Map<String, String>> runProject(@RequestBody RunProjectRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            List<SourceCode> sourceCodes=sourceCodeService.getByProgramId(request.getProgramId());

            for (SourceCode code : sourceCodes) {
                System.out.println("  Code ID: " + code.getCodeId()); // Assuming 'id' is your code_id
            }
            TestResource testResource=new TestResource();
            List<TestResource> testResources = testResourceService.getTestAll(2,request.getProgramId());
            String logOutput = codeService.runTests(sourceCodes,testResources.get(0));
            response.put("log", logOutput);
            response.put("message", "项目运行请求已处理。");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            // 捕获服务层抛出的自定义异常
            response.put("log", e.getMessage());
            response.put("message", "项目运行失败。");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 捕获其他未知异常
            response.put("log", "服务器内部错误: " + e.getMessage());
            response.put("message", "项目运行失败。");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
