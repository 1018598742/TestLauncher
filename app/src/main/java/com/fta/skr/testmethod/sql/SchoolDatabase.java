package com.fta.skr.testmethod.sql;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {School.class}, version = 1,exportSchema = true)
public abstract class SchoolDatabase extends RoomDatabase {
    public abstract SchoolDao schoolDao();

    final static Object object = new Object();

    private static SchoolDatabase appDatabase;


    public static SchoolDatabase getInstance(Context context){
        synchronized (object){
            if (appDatabase == null){
                synchronized (object){
                    if (appDatabase == null){
                        appDatabase = Room
                                .databaseBuilder(context.getApplicationContext(),SchoolDatabase.class,"school.db")
                                .build();
                    }
                }
            }
        }
        return appDatabase;
    }
}
