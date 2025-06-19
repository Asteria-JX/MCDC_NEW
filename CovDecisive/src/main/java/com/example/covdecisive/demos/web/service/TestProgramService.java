package com.example.covdecisive.demos.web.service;

import com.example.covdecisive.demos.web.dto.TestCaseResultDTO;
import com.example.covdecisive.demos.web.mapper.TestProgramMapper;
import com.example.covdecisive.demos.web.mapper.TestResourceMapper;
import com.example.covdecisive.demos.web.model.TestProgram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestProgramService {

    @Autowired
    private TestProgramMapper testProgramMapper;

    @Autowired
    private TestResourceMapper testResourceMapper;

    public int storeGeneratedTestCases(int userId, int programId, List<TestCaseResultDTO> testCases) {

//        String testProgramName = "LLM_generated_programId" + programId + "_userId" + userId;
//        testProgramMapper.insertTestProgram(programId,testProgramName, userId, 3);
//        int testProgramId = testProgramMapper.getLastInsertId();

        // 插入 test_programs
        TestProgram tp = new TestProgram();
        tp.setProgramId(programId);
        tp.setCreateWay(3);
        tp.setUserId(userId);
        tp.setTestProgramName("LLM_generated_programId" + programId + "_userId" + userId);
        testProgramMapper.insertTestProgram(tp);

        // 插入 test_resources
        for (TestCaseResultDTO dto : testCases) {
            String filePath = "LLM/" + dto.getFileName();
            Integer existingId = testResourceMapper.isExisted(filePath);
            if (existingId != null)
                testResourceMapper.updateTestResourceById(existingId, dto.getTestCode());
            else
                testResourceMapper.insertTestResource_xyd(tp.getTestProgramId(), userId, filePath, dto.getTestCode());

        }

        return tp.getTestProgramId();
    }
}
