package com.example.ienglish;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class Splash extends AppCompatActivity {        //启动页

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAndroidNativeLightStatusBar(this,true);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  //隐藏状态栏
        setContentView(R.layout.activity_splash);
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
