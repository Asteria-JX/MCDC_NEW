package com.example.covdecisive.demos.web.mapper;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.*;
import com.example.covdecisive.demos.web.model.Program;

@Mapper
public interface ProgramMapper {
    @Select("SELECT * FROM programs where user_id=#{user_id}")
    @Results({
            @Result(property = "programId", column = "program_id"),
            @Result(property = "programName", column = "program_name"),
            @Result(property = "user_id", column = "user_id")
    })
    List<Program> getAll(Integer userId);

    @Insert("INSERT INTO programs (program_name, user_id) VALUES (#{programName}, #{user_id})")
    @Options(useGeneratedKeys = true, keyProperty = "programId")
    void insert(Program program);

    @Select("SELECT * FROM programs WHERE user_id=#{userID}")
    @Results({
            @Result(property = "programId", column = "program_id"),
            @Result(property = "programName", column = "program_name"),
    })
    List<Program> getProgramsByUserID(int userID);

    @Select("SELECT * FROM programs WHERE program_id=#{programID}")
    @Results({
            @Result(property = "programId", column = "program_id"),
            @Result(property = "programName", column = "program_name"),
    })
    Program getProgramsByProgramID(int programID);

    @Select("SELECT program_id, program_name, user_id FROM programs WHERE program_id = #{programId}")
    Optional<Program> selectByProgramId(Integer programId);

    @Select("SELECT program_name FROM programs WHERE program_id=#{program_id}")
    String selectProgramNameById(Integer programId);
}
