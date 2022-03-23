package com.example.ienglish.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ienglish.Bean.Words;
import com.example.ienglish.ChangeMesg.ChangeHead;
import com.example.ienglish.Login;
import com.example.ienglish.MainActivity;
import com.example.ienglish.MainGo;
import com.example.ienglish.R;
import com.example.ienglish.UserDB.UserDb;
import com.example.ienglish.ieng_main_activity;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;


public class Mymsg_Fragment extends Fragment implements ieng_main_activity.Send_Words, ieng_main_activity.FragmentBackListener {


    private TextView username,nickname,sign,usernum,num_words,num_essays;
    private ImageView head;
    private SQLiteDatabase db;
    private UserDb userDb;
    private String name;
    private Button btn_go_login;
    private ieng_main_activity activity; ///  按下返回键的监听事件
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mymsg, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitView(view);
        Bundle bundle = getArguments();
        if(bundle != null)
            name = bundle.getString("user_name");
        if(name != null){
        SetMsg(name);
        SetPhone(name);}
        else
            btn_go_login.setText("前往登录/注册");
    }

    private void SetPhone(String name) {
        userDb = new UserDb(getActivity(), "user_msg", null, 1);
        db = userDb.getWritableDatabase();
        Cursor cursor2 = db.rawQuery("select * from user_msg where user_name = ?", new String[]{name});
        while (cursor2.moveToNext()) {
            if (cursor2.getString(cursor2.getColumnIndex("user_number")) != null) {
                usernum.setText(cursor2.getString(cursor2.getColumnIndex("user_number")));
            }
        }
        cursor2.close();
    }

    private void SetMsg(String name) {
            userDb = new UserDb(getActivity(), "user_detail_mesg", null, 1);
            db = userDb.getWritableDatabase();
            Cursor cursor = db.rawQuery("select * from user_detail_mesg where user_name = ?", new String[]{name});

            while (cursor.moveToNext()) {
            if (cursor.getBlob(cursor.getColumnIndex("user_head")) != null) {
                byte pic[] = cursor.getBlob(cursor.getColumnIndex("user_head"));
                Bitmap b = BitmapFactory.decodeByteArray(pic, 0, pic.length);
                head.setImageBitmap(b);
            }
            if (cursor.getString(cursor.getColumnIndex("user_sign")) != null) {
                sign.setText(cursor.getString(cursor.getColumnIndex("user_sign")));
            }
            if (cursor.getString(cursor.getColumnIndex("user_nickname")) != null) {
                nickname.setText(cursor.getString(cursor.getColumnIndex("user_nickname")));
            }
            if (cursor.getString(cursor.getColumnIndex("user_name")) != null) {
                username.setText(cursor.getString(cursor.getColumnIndex("user_name")));
            }
            if (cursor.getString(cursor.getColumnIndex("words_num")) != null) {
                num_words.setText(cursor.getString(cursor.getColumnIndex("words_num")));
            } else {
                num_words.setText("0");
            }
            if (cursor.getString(cursor.getColumnIndex("essay_num")) != null) {
                num_essays.setText(cursor.getString(cursor.getColumnIndex("essay_num")));
            } else {
                num_essays.setText("0");
            }
        }
        cursor.close();
    }

    private void InitView(View view) {
        username = view.findViewById(R.id.tv_username);
        nickname = view.findViewById(R.id.tv_nickname);
        sign = view.findViewById(R.id.tv_sign);
        usernum = view.findViewById(R.id.tv_usernum);
        head = view.findViewById(R.id.im_head1);
        num_essays = view.findViewById(R.id.tv_num_essay);
        num_words = view.findViewById(R.id.tv_num_words);
        btn_go_login = view.findViewById(R.id.btn_go_login);
        btn_go_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Go_or_Exit(name);
            }
        });
        head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(name != null) {
                    Intent intent = new Intent(getActivity(), ChangeHead.class);
                    intent.putExtra("user_name", name);
                    startActivity(intent);
                    getActivity().finish();
                }
                else{
                    Toast.makeText(getActivity(),"请先登录哦",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void Go_or_Exit(String name) {
        editor.clear();
        editor.commit();
        if(name != null){
            Intent intent = new Intent(getActivity(), MainGo.class);
            startActivity(intent);
            getActivity().finish();
        }
        else{
            Intent intent = new Intent(getActivity(), Login.class);
            intent.putExtra("flag","1");
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void sendPron(String uk, String us, String name) {

    }

    @Override
    public void getResult(Words words) {

    }

    @Override
    public void getResultInChines(String sentence, String trans_res) {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        preferences = context.getSharedPreferences("UserName", Context.MODE_PRIVATE);
        editor = preferences.edit();
        activity = (ieng_main_activity) context;
        activity.setBackListener(this);
    }
    @Override
    public void onDetach() {
        super.onDetach();
        activity.setBackListener(null);
    }

    @Override
    public void onBackForward() {
        activity.setInterception(false);
    }
}
