package com.fta.skr.testmethod.sql;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SchoolDao {
    @Query("SELECT * FROM school")
    List<School> getAll();

    @Query("SELECT * FROM school WHERE uid IN (:schoolIds)")
    List<School> loadAllByIds(int[] schoolIds);


    @Insert
    void insertAll(School... schools);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSchool(School school);

    @Delete
    void delete(School school);
}
