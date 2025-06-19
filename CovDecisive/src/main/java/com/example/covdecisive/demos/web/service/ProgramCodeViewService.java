package com.example.covdecisive.demos.web.service;
import com.example.covdecisive.demos.web.mapper.ProgramCodeViewMapper;
import com.example.covdecisive.demos.web.model.ProgramCodeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProgramCodeViewService {
    @Autowired
    private ProgramCodeViewMapper programCodeViewMapper;

    public List<ProgramCodeView> getByProgramId(int programId) {
        return programCodeViewMapper.getByProgramId(programId);
    }
}
