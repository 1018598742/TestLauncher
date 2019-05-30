package com.fta.skr.testmethod.sql;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.fta.skr.testmethod.R;

import java.util.List;

public class SqlActivity extends AppCompatActivity {

    private static final String TAG = "My_Test";
    private Context mContext;

    AppDatabase appDatabase;

    SchoolDatabase schoolDatabase;

    public static void startSqlActivity(Context context) {
        context.startActivity(new Intent(context, SqlActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sql);
        mContext = this;
        appDatabase = AppDatabase.getInstance(mContext);
        schoolDatabase = SchoolDatabase.getInstance(mContext);
    }

    public void initSql(View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "SqlActivity-run: 插入数据");
                User user = new User();
                user.setUid(1);
                user.setFirstName("firstName");
                user.setLastName("lastName");
                appDatabase.userDao().insertUser(user);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                School school = new School();
                school.setSchoolName("schoolName");
                schoolDatabase.schoolDao().insertSchool(school);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(mContext);
                SQLiteDatabase writableDatabase = myDatabaseHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("id",1);
                contentValues.put("name","localName");
                writableDatabase.insert("local",null,contentValues);
            }
        }).start();
    }

    public void querySql(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<User> all = appDatabase.userDao().getAll();
                if (all != null && all.size() > 0){
                    for (int i = 0; i < all.size(); i++) {
                        Log.i(TAG, "SqlActivity-run: "+all.get(i).toString());
                    }
                }else {
                    Log.i(TAG, "SqlActivity-run: is null");
                }
            }
        }).start();
    }
}
