package com.example.ienglish;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.ienglish.UserDB.UserDb;

import java.io.File;

public class MainGo extends AppCompatActivity {

    private UserDb userDb;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_go);
        setAndroidNativeLightStatusBar(this,true);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

    }

        /*DeleteDB("user_msg");      //删除数据库
        DeleteDB("user_detail_mesg");
        DeleteDB("words_book");
        DeleteDB("essay");*/
}

    private void DeleteDB(String tablename) {
        userDb = new UserDb(MainGo.this,tablename,null,1);
        db = userDb.getWritableDatabase();
        db.delete(tablename,null,null);
        db.close();
    }

    public void Reg(View view) {
        Intent intent = new Intent(MainGo.this,Register.class);
        startActivity(intent);
    }

    public void Login(View view) {
        clear(this);
        Intent intent = new Intent(MainGo.this,Login.class);
        startActivity(intent);
    }

    public static void clear(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("UserName", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
    private static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {   //更改状态栏字体
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
}
