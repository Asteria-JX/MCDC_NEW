package com.example.covdecisive.demos.web.controller;

import com.example.covdecisive.demos.web.service.ProgramCodeViewService;
import com.example.covdecisive.demos.web.service.SourceCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.covdecisive.demos.web.model.ProgramCodeView;
import com.example.covdecisive.demos.web.model.SourceCode;

@Controller
@Api(tags = "API接口")
@RestController
//Spring 中的一个注解，用于支持跨域请求
@CrossOrigin(origins = "*", maxAge = 3600)
public class SourceController {
    @Autowired
    private SourceCodeService sourceCodeService;

    @Autowired
    private ProgramCodeViewService programCodeViewService;

    @GetMapping("/flat")
    public List<String> getFlatPaths(@RequestParam int programId) {
        //System.out.println("选中项目id："+programId);
        List<ProgramCodeView> list = programCodeViewService.getByProgramId(programId);
        return list.stream().map(ProgramCodeView::getFile_path).collect(Collectors.toList());
    }

    // 获取指定 file_path 的代码内容
    @ApiOperation("获取指定 file_path 的代码内容")
    @GetMapping("/getCodeContent")
    public String getCodeContent(
            @RequestParam("programId") int programId,
            @RequestParam("filePath") String filePath) {
        String result = programCodeViewService.getCodeContent(programId, filePath);
        System.out.println("代码文件："+result);
        return result;
    }

    // 编辑修改代码内容
    @ApiOperation("编辑修改代码内容")
    @PostMapping("/updateCodeContent")
    public String updateCodeContent(@RequestBody SourceCode request) {
        int result = sourceCodeService.updateCodeContent(request.getProgramId(), request.getFilePath(), request.getCodeContent());
        return result > 0 ? "success" : "fail";
    }

    @GetMapping("/getContentByCodeId")
    public String getContentByCodeId(@RequestParam int codeId) {
        String content = sourceCodeService.getContentByCodeId(codeId);
        String filePath=sourceCodeService.getFilePathByCodeId(codeId);
//        System.out.println(content);
//        System.out.println(filePath);
        String type=detectLanguage(filePath);
        System.out.println(type);
        return sourceCodeService.getContentByCodeId(codeId);
    }


    private static final Map<String, String> EXTENSION_TO_LANGUAGE = new HashMap<>();

    static {
        // 先添加一些常见的文件后缀和他们的对应语言
        EXTENSION_TO_LANGUAGE.put(".java", "Java");
        EXTENSION_TO_LANGUAGE.put(".py", "Python");
        EXTENSION_TO_LANGUAGE.put(".c", "C");
        EXTENSION_TO_LANGUAGE.put(".cpp", "C++");
        EXTENSION_TO_LANGUAGE.put(".js", "JavaScript");
        EXTENSION_TO_LANGUAGE.put(".html", "HTML");
        EXTENSION_TO_LANGUAGE.put(".xml", "XML");
    }

    public static String detectLanguage(String filePath) {
        // 先获取文件后缀
        String fileExtension = getFileExtension(filePath);

        // 根据文件后缀判断语言
        return EXTENSION_TO_LANGUAGE.getOrDefault(fileExtension, "Unknown");
    }

    private static String getFileExtension(String filePath) {
        // 如果文件没有后缀，那返回空字符串
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        // 判断是否有小数点分隔的后缀
        int lastDotIndex = filePath.lastIndexOf('.');
        // 只有当小数点不是在最开始或最后的时候，才认为是后缀
        if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex);
        }

        return "";
    }

    //获取指定 programId 下所有 src/main/java 中的 .java 源文件内容
    @ApiOperation("获取所有需要生成测试用例的java文件")
    @GetMapping("/getJavaFilesNeedingTests")
    public List<String> getJavaFilesNeedingTests(@RequestParam int programId) {
        List<SourceCode> sourceCodes = sourceCodeService.getJavaFilesNeedingTests(programId);
        return sourceCodes.stream()
                .map(SourceCode::getCodeContent)
                .collect(Collectors.toList());
    }
}
