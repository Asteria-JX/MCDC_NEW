package com.example.covdecisive.demos.web.mapper;
import java.util.List;
import org.apache.ibatis.annotations.*;
import com.example.covdecisive.demos.web.model.Program;

@Mapper
public interface ProgramMapper {
    @Select("SELECT * FROM programs where user_id=#{user_id}")
    @Results({
            @Result(property = "programId", column = "program_id"),
            @Result(property = "programName", column = "program_name"),
            @Result(property = "version", column = "version"),
            @Result(property = "description", column = "description"),
            @Result(property = "user_id", column = "user_id")
    })
    List<Program> getAll(Integer userId);

    @Insert("INSERT INTO programs (program_name, version, description,user_id) VALUES (#{programName}, #{version}, #{description}, #{user_id})")
    @Options(useGeneratedKeys = true, keyProperty = "programId")
    void insert(Program program);

    @Select("SELECT * FROM programs WHERE user_id=#{userID}")
    @Results({
            @Result(property = "programId", column = "program_id"),
            @Result(property = "programName", column = "program_name"),
            @Result(property = "version", column = "version"),
            @Result(property = "description", column = "description")
    })
    List<Program> getProgramsByUserID(int userID);

    @Select("SELECT * FROM programs WHERE program_id=#{programID}")
    @Results({
            @Result(property = "programId", column = "program_id"),
            @Result(property = "programName", column = "program_name"),
            @Result(property = "version", column = "version"),
            @Result(property = "description", column = "description")
    })
    Program getProgramsByProgramID(int programID);
}
