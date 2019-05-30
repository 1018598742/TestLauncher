package com.fta.skr.testmethod.sql;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.SparseArray;

@Entity(tableName = "school")
public class School {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "school_name")
    private String schoolName;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }
}
