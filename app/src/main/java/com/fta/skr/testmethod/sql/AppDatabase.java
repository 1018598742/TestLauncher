package com.fta.skr.testmethod.sql;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {User.class}, version = 1,exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    final static Object object = new Object();

    private static AppDatabase appDatabase;


    public static AppDatabase getInstance(Context context){
        synchronized (object){
            if (appDatabase == null){
                synchronized (object){
                    if (appDatabase == null){
                        appDatabase = Room
                                .databaseBuilder(context.getApplicationContext(),AppDatabase.class,"user")
                                .build();
                    }
                }
            }
        }
        return appDatabase;
    }
}
