package com.example.covdecisive.demos.web.service;
import com.example.covdecisive.demos.web.mapper.TestResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.covdecisive.demos.web.model.TestResource;
import java.util.List;

@Service
public class TestResourceService {
    @Autowired
    private TestResourceMapper testResourceMapper;

    public void batchInsert(List<TestResource> resources) {
        for (TestResource resource : resources) {
            testResourceMapper.insert(resource);
        }
    }

    public List<TestResource> getTestAll(Integer user_id,Integer program_id) {
        return testResourceMapper.getTestAll(user_id,program_id);
    }

    public String getCodeContentByName(String name) {
        return testResourceMapper.getCodeContentByName(name);
    }

    public List<TestResource> getTestResourceByProgramID(int programID) {
        return testResourceMapper.getTestResourceByProgramID(programID);
    };
}
