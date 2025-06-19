package com.example.covdecisive.demos.web.mapper;

import java.util.List;
import com.example.covdecisive.demos.web.model.ProgramCodeView;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ProgramCodeViewMapper {
    @Select("SELECT * FROM program_code_view WHERE program_id = #{programId}")
    List<ProgramCodeView> getByProgramId(int programId);

    @Select("SELECT code_content FROM program_code_view WHERE program_id = #{programId} AND file_path = #{filePath}")
    String selectCodeContent(@Param("programId") int programId, @Param("filePath") String filePath);

}
