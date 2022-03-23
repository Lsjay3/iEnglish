package com.example.ienglish.UserDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDb extends SQLiteOpenHelper {
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table user_msg(user_name varchar(50),user_pass varchar(50),user_number varchar(50))");
        db.execSQL("create table user_detail_mesg(user_name varchar(50),user_number varchar(50),user_head BLOB," +
                "user_sign varchar(200),user_nickname varchar(20),words_num varcahr(20),essay_num varchar(20))");
        db.execSQL("create table words_book(user_name varchar(50),words varchar(50))");
        db.execSQL("create table essay(user_name varchar(50),content varchar(10000),essay_name varchar(20))");
    }

    public UserDb(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
