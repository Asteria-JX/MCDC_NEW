package com.example.covdecisive.demos.web.service;
import com.example.covdecisive.demos.web.mapper.SourceCodeMapper;
import com.example.covdecisive.demos.web.model.SourceCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;


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

    //获取指定 programId 下所有 src/main/java 中的 .java 源文件内容
    public List<SourceCode> getJavaFilesNeedingTests(int programId) {
        List<SourceCode> allFiles = sourceCodeMapper.getByProgramId(programId);

        return allFiles.stream()
                .filter(src -> {
                    String path = src.getFilePath();
                    if (path == null || !path.endsWith(".java")) return false;

                    // 排除测试类、测试路径、无效目录
                    String lowerPath = path.toLowerCase();
                    return !lowerPath.contains("/test/") &&
                            !lowerPath.contains("/site/") &&
                            !lowerPath.contains("/media/") &&
                            !path.contains("Test") &&  // 可选，排除以Test命名的类
                            !path.contains("/.") &&    // 排除隐藏文件夹
                            src.getCodeContent() != null && !src.getCodeContent().trim().isEmpty();
                })
                .collect(Collectors.toList());
    }





}
