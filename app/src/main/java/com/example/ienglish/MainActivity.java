package com.example.ienglish;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.ienglish.UserDB.UserDb;
import com.githang.statusbar.StatusBarCompat;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private UserDb userDb;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  //隐藏状态栏
        setAndroidNativeLightStatusBar(this,true);
        setContentView(R.layout.activity_main);
        Thread myThread = new Thread() {//创建子线程
            @Override
            public void run() {
                try {
                    sleep(1000);//使程序休眠一秒
                    Intent it = new Intent(getApplicationContext(), ieng_main_activity.class);
                    startActivity(it);
                    finish();//关闭当前活动
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
        /*DeleteDB("user_msg");      //删除数据库
        DeleteDB("user_detail_mesg");
        DeleteDB("words_book");
        DeleteDB("essay");*/
    }
    private static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {   //更改状态栏字体
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    public void SetStauts(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)
        {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
    private void DeleteDB(String tablename) {
        userDb = new UserDb(MainActivity.this,tablename,null,1);
        db = userDb.getWritableDatabase();
        db.delete(tablename,null,null);
        db.close();
    }

    public void Reg(View view) {
        Intent intent = new Intent(MainActivity.this,Register.class);
        startActivity(intent);
    }

    public void Login(View view) {
        Intent intent = new Intent(MainActivity.this,Login.class);
        startActivity(intent);
    }

    public void Travel(View view) {
        Intent intent = new Intent(MainActivity.this,ieng_main_activity.class);
        startActivity(intent);
    }
}
