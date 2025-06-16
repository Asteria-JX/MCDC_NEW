package com.example.covdecisive.demos.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.covdecisive.demos.web.service.TestResourceService;
import com.example.covdecisive.demos.web.model.TestResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Api(tags = "API接口")
@RestController
//Spring 中的一个注解，用于支持跨域请求
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestResourceController {
    @Autowired
    private TestResourceService testResourceService;

    @GetMapping("/testall")
    public List<TestResource> getTestAll(@RequestParam Integer user_id, @RequestParam Integer program_id) {

        return testResourceService.getTestAll(user_id, program_id);
    }

    // 获取指定 test_source 的代码内容
    @ApiOperation("获取指定 test_source 的代码内容")
    @GetMapping("/getTestContent")
    public String getCodeContent(@RequestParam String name) {
        String result = testResourceService.getCodeContentByName(name);
        System.out.println("测试用例："+result);
        return result;
    }

    @GetMapping("/getTestResourceByProgramID/{programID}")
    public List<TestResource> getTestResourceByProgramID(@PathVariable("programID") int programID) {
        //System.out.println(programService.getAll());
        System.out.println(programID);
        return testResourceService.getTestResourceByProgramID(programID);
    }
}
