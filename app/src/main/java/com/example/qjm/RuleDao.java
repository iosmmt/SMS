package com.example.qjm;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface RuleDao {
    @Query("SELECT * FROM rule")
    List<Rule> getAllRules();
    
    @Query("SELECT * FROM rule WHERE enabled = 1")
    List<Rule> getEnabledRules();

    @Insert
    void insert(Rule rule);

    @Update
    void update(Rule rule);

    @Delete
    void delete(Rule rule);
}