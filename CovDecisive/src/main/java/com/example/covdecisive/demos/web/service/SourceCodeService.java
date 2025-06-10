package com.example.covdecisive.demos.web.service;
import com.example.covdecisive.demos.web.mapper.SourceCodeMapper;
import com.example.covdecisive.demos.web.model.SourceCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class SourceCodeService {
    @Autowired
    private SourceCodeMapper sourceCodeMapper;

    public void insert(SourceCode code) {
        sourceCodeMapper.insert(code);
    }

    public List<SourceCode> getByProgramId(int programId) {
        return sourceCodeMapper.getByProgramId(programId);
    }

    public String getCodeContent(int programId, String filePath) {
        return sourceCodeMapper.selectCodeContent(programId, filePath);
    }

    public int updateCodeContent(int programId, String filePath, String codeContent) {
        return sourceCodeMapper.updateCode(programId, filePath, codeContent);
    }

    public String getContentByCodeId(int codeId) {return sourceCodeMapper.getContentByCodeId(codeId);}

    public String getFilePathByCodeId(int codeId) {return sourceCodeMapper.getFilePathByCodeId(codeId);}







}
