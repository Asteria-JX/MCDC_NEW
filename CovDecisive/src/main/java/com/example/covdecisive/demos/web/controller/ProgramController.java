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

//    @Autowired
//    private CodeService codeService;

    @Autowired
    private TestResourceService testResourceService;

    @GetMapping("/getProgramsByUserID/{userID}")
    public List<Program> getProgramsByUserID(@PathVariable("userID") int userID) {
        //System.out.println(programService.getAll());
        return programService.getProgramsByUserID(userID);
    }

    @GetMapping("/all")
    public List<Program> getAllPrograms(@RequestParam Integer userId) {
        System.out.println("all:"+userId);
        return programService.getAll(userId);
    }

    @PostMapping("/uploadProject")
    public void uploadProject(@RequestParam("programName") String programName,
                              @RequestParam("user_id") Integer user_id,
                              @RequestParam("files") List<MultipartFile> files) throws IOException {

        System.out.println("uploadProject:"+programName+user_id);
        Program program = new Program();
        program.setProgramName(programName);
        program.setUserId(user_id);
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

}
