package com.example.ienglish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ienglish.UserDB.UserDb;

public class Login extends AppCompatActivity {

    private EditText user_name,user_pass;
    private SQLiteDatabase db;
    private UserDb userDb;
    private TextView wrong_name;
    private TextView wrong_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        InitView();
        Intent intent = getIntent();
        if(intent!=null){
            String username = intent.getStringExtra("username");
            user_name.setText(username);
        }
    }

    private void InitView() {
        user_name = findViewById(R.id.et_user_name);
        user_pass = findViewById(R.id.et_user_pass);
        wrong_name = findViewById(R.id.tv_wrong_name);
        wrong_pass = findViewById(R.id.tv_wrong_pass);

    }

    public void Login_activity(View view) {
        userDb = new UserDb(Login.this,"user_msg",null,1);
        db = userDb.getWritableDatabase();
        String username = user_name.getText().toString().trim();
        String userpass = user_pass.getText().toString().trim();
        Cursor cursor =  db.rawQuery("select user_pass from user_msg where user_name = ?",new String[]{username});
        String num = Integer.toString(cursor.getCount());
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                if (userpass.equals(cursor.getString(cursor.getColumnIndex("user_pass")))) {
                    Toast.makeText(Login.this,"登录成功！",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login.this, ieng_main_activity.class);
                    intent.putExtra("user_name",username);
                    SaveLoginSate(username);                       //保存登录状态
                    startActivity(intent);
                } else {
                    wrong_pass.setText("密码错误");
                }
            }
        }
        else {
            wrong_name.setText("用户不存在");
        }
        cursor.close();
    }

    private void SaveLoginSate(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserName", Context.MODE_PRIVATE); //私有数据

        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器

        editor.putString("username", username);

        editor.commit();//提交修改
    }

    public void Forget_pass(View view) {
        Intent intent = new Intent(Login.this, Forget_password.class);
        intent.putExtra("username",user_name.getText().toString());
        startActivity(intent);
    }

    public void Go_Register(View view) {
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
    }
}
