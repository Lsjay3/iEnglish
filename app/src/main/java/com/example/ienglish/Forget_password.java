package com.example.ienglish;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ienglish.UserDB.UserDb;

public class Forget_password extends AppCompatActivity {

    private EditText user_name,user_pass,user_againpass,user_num;
    private SQLiteDatabase db;
    private UserDb userDb;
    private TextView wrong_name;
    private TextView wrong_pass,wrong_againpass,wrong_num;
    private String name;
    private Button btn_sure;
    private int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        InitView();
        Intent intent = getIntent();
        if(intent!=null){
            String username = intent.getStringExtra("username");
            user_name.setText(username);
            name = username;
        }
    }

    private void InitView() {
        user_name = findViewById(R.id.et_user_name);
        user_pass = findViewById(R.id.et_user_pass);
        user_againpass = findViewById(R.id.et_user_againpass);
        user_num = findViewById(R.id.et_user_num);
        wrong_name = findViewById(R.id.tv_wrong_name);
        wrong_pass = findViewById(R.id.tv_wrong_pass);
        wrong_num = findViewById(R.id.tv_wrong_num);
        btn_sure = findViewById(R.id.btn_sure);
        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JudgePass();
                if(flag == 2)
                GoToLogin();
                else{
                    Toast.makeText(Forget_password.this,"修改失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
        wrong_againpass = findViewById(R.id.tv_wrong_againpass);
        user_num.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    // 此处为得到焦点时的处理内容
                } else {
                    Judge_num();
                }
            }
        });
        user_againpass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    // 此处为得到焦点时的处理内容
                } else {
                    JudgePass();
                }
            }
        });
    }

    private void Judge_num() {
        name = user_name.getText().toString().trim();
        userDb = new UserDb(Forget_password.this,"user_msg",null,1);
        db = userDb.getWritableDatabase();
        String usernum = user_num.getText().toString().trim();
        Cursor cursor =  db.rawQuery("select * from user_msg where user_name = ?",new String[]{name});
        String num = Integer.toString(cursor.getCount());
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                if (usernum.equals(cursor.getString(cursor.getColumnIndex("user_number")))) {
                    wrong_num.setVisibility(View.INVISIBLE);
                    flag++;
                } else {
                    wrong_num.setText("号码错误");
                }
            }
        }
        else {
            wrong_name.setText("用户不存在");
        }
        cursor.close();
    }


    private void JudgePass() {
        String pass = user_pass.getText().toString();
        String againpass = user_againpass.getText().toString();
        if(pass.equals(againpass)){
            userDb = new UserDb(Forget_password.this,"user_msg",null,1);
            db = userDb.getWritableDatabase();
            String update = "update user_msg set user_pass = '" + pass + "' where user_name = '"+ name + "'";
            //Toast.makeText(Forget_password.this,pass,Toast.LENGTH_SHORT).show();
            db.execSQL(update);
            flag++;
        }
        else{
            wrong_againpass.setText("密码不一致");
        }
    }

    private void GoToLogin() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改成功！");
        builder.setMessage("前往登录？");
        //设置确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Forget_password.this,Login.class);
                intent.putExtra("username",name);
                startActivity(intent);
            }
        });
        //设置取消
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        //设置 是否可取消的 空白区域取消 默认为true
        builder.setCancelable(true);
        AlertDialog ad = builder.create();
        ad.show();
    }

}
