package com.example.covdecisive.demos.web.service;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.covdecisive.demos.web.model.Program;
import com.example.covdecisive.demos.web.mapper.ProgramMapper;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.ResultSet;
import java.sql.SQLException;


@Service
public class ProgramService {
    @Autowired
    private ProgramMapper programMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Program> getAll(Integer userId) {
        return programMapper.getAll(userId);
    }

    public void insert(Program program) {
        programMapper.insert(program);
    }

    public List<Program> getProgramsByUserID(int userID) {
        return programMapper.getProgramsByUserID(userID);
    }


}
