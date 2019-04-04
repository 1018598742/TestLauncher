package com.android.launcher3.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDbHelperTest extends SQLiteOpenHelper {

    public void test(){
        Context context = null;
        SQLiteOpenHelper sqLiteOpenHelper = new SQLiteDbHelperTest(context);
        SQLiteDatabase writableDatabase = sqLiteOpenHelper.getWritableDatabase();
        writableDatabase.insert("","",new ContentValues());
        writableDatabase.execSQL("");
//        sqLiteOpenHelper.getReadableDatabase().query()
    }

    public SQLiteDbHelperTest(Context context) {
        this(context, "test.db", null, 1);
    }

    public SQLiteDbHelperTest(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static final String DB_NAME = "database.db";

    public static final int DB_VERSION = 1;

    public static final String TABLE_STUDENT = "students";

    //创建 students 表的 sql 语句
    private static final String STUDENTS_CREATE_TABLE_SQL = "create table " + TABLE_STUDENT + "("
            + "id integer primary key autoincrement,"
            + "name varchar(20) not null,"
            + "tel_no varchar(11) not null,"
            + "cls_id integer not null"
            + ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 在这里通过 db.execSQL 函数执行 SQL 语句创建所需要的表
        // 创建 students 表
        db.execSQL(STUDENTS_CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
}
