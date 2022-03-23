package com.example.ienglish;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ienglish.UserDB.UserDb;

import java.util.HashMap;
import java.util.Map;

import static android.view.View.INVISIBLE;

public class Register extends AppCompatActivity {

    private EditText user_name,user_pass,user_number;
    private SQLiteDatabase db;
    private UserDb userDb;
    private String username,userpass,usernumber;
    private TextView is_exist,number_invalid , go_login , pass_is_null;
    private int flag_is_exist = 1 , flag_number_invalid = 1 , flag_pass = 1;
    private RelativeLayout rel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        }
        userDb = new UserDb(Register.this,"user_msg",null,1);
        db = userDb.getWritableDatabase();
        InitView();
    }

    private void InitView() {
        user_name = findViewById(R.id.et_user_name);
        user_pass = findViewById(R.id.et_user_pass);
        user_number = findViewById(R.id.et_user_number);
        is_exist = findViewById(R.id.tv_is_exist);
        number_invalid = findViewById(R.id.tv_number_invalid);
        go_login = findViewById(R.id.tv_go_login);
        rel = findViewById(R.id.Rela_regsiter);
        pass_is_null = findViewById(R.id.tv_pass_is_null);
        user_pass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    pass_is_null.setVisibility(INVISIBLE);
                }
                else {
                    Judge_pass();
                }
            }
        });
        user_name.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    is_exist.setVisibility(INVISIBLE);
                } else {
                    Judge_name();
                }
            }
        });
        user_number.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    number_invalid.setVisibility(INVISIBLE);
                    // 此处为得到焦点时的处理内容
                } else {
                    Judge_number();
                }
            }
        });
    }

    private void Judge_pass() {
        GetData();
        pass_is_null.setVisibility(View.VISIBLE);
        if(userpass == null || userpass.equals("")){
            pass_is_null.setText("不能为空");
            pass_is_null.setTextColor(Color.RED);
            flag_pass = 1;
        }
        else flag_pass = 0;
    }

    public void Judge_name(){
        GetData();
        is_exist.setVisibility(View.VISIBLE);
        if(username == null || username.equals("")){

            is_exist.setText("不能为空");
            is_exist.setTextColor(Color.RED);
            flag_is_exist = 1;
        }
        else{
            if(Judge_Unique(username) == 1){
                is_exist.setText("已存在!");
                is_exist.setTextColor(Color.RED);
                flag_is_exist = 1;
            }
            else {
                    is_exist.setText("合法!");
                    is_exist.setTextColor(Color.GREEN);
                    flag_is_exist = 0;
                }
            }
    }
    public void Judge_number(){
        GetData();
        number_invalid.setVisibility(View.VISIBLE);
        if(!orPhoneNumber(usernumber)){
            number_invalid.setText("不合法");
            number_invalid.setTextColor(Color.RED);
            flag_number_invalid = 1;
        }
        else{
            number_invalid.setText("合法");
            number_invalid.setTextColor(Color.GREEN);
            flag_number_invalid = 0;
        }
    }
    public static boolean orPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || "".equals(phoneNumber))
            return false;
        String regex = "^1[3|4|5|8][0-9]\\d{8}$";
        return phoneNumber.matches(regex);
    }
    public void Reg_activity(View view){
        Judge_name();
        Judge_number();
        if(flag_is_exist == 0 && flag_number_invalid == 0 && flag_pass == 0) {     //判断三项是否全部满足要求
            number_invalid.setVisibility(View.VISIBLE);
            is_exist.setVisibility(View.VISIBLE);
            number_invalid.setText("合法");
            number_invalid.setTextColor(Color.GREEN);
            is_exist.setText("合法!");
            is_exist.setTextColor(Color.GREEN);
            ContentValues values = new ContentValues();
            values.put("user_name", username);
            values.put("user_pass", userpass);
            values.put("user_number", usernumber);
            db.insert("user_msg", null, values);
            InsertToMsg(username,"user_detail_mesg");
            InsertToMsg(username,"words_book");
            //InsertToMsg(username,"essay");
            GotoLogin();
        }
    }
    private void GotoLogin(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("注册成功！");
        builder.setMessage("前往登录？");
        //设置确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Register.this,Login.class);
                intent.putExtra("username",username);
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
    private void InsertToMsg(String username,String table) {
        UserDb userDb = new UserDb(Register.this,table,null,1);
        SQLiteDatabase db = userDb.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_name", username);
        db.insert(table, null, values);
    }

    public int Judge_Unique(String username) {
        int flag_is_exist = 0;
        Cursor cursor = db.query("user_msg",new String[]{"user_name"}, null, null, null, null, null);
        while(cursor.moveToNext()){
            if(username.equals(cursor.getString(cursor.getColumnIndex("user_name")))){
                flag_is_exist = 1;
                break;
            }
        }
        cursor.close();
        return flag_is_exist;
    }

    public void GetData(){
        username = user_name.getText().toString().trim();
        userpass = user_pass.getText().toString().trim();
        usernumber = user_number.getText().toString().trim();
    }
    public void Exist_num(View view) {
        Intent intent = new Intent(Register.this,Login.class);
        startActivity(intent);
    }

    public void Go_Login(View view) {
        Intent intent = new Intent(Register.this,Login.class);
        startActivity(intent);
    }
}
