package com.example.ienglish.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ienglish.Bean.Words;
import com.example.ienglish.R;
import com.example.ienglish.UserDB.UserDb;
import com.example.ienglish.apiservice.DictationService;
import com.example.ienglish.ieng_main_activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import static android.content.Context.BIND_AUTO_CREATE;


public class Dictation_Fragment extends Fragment implements ieng_main_activity.Send_Words{

    public static Handler handler;
    private ImageView im_start_dictation,im_pause_dictation;
    private TextView tv_tips;
    private LinearLayout ll_dictation;
    private EditText et_answer;
    private Button btn_submit;
    private TextView tv_dictation_advice;
    private String name;
    private SQLiteDatabase db;
    private UserDb userDb;
    private String word;
    private List<String> words = new ArrayList();
    private MediaPlayer mp;
    private Timer timer = null;
    private Thread runnable;
    private MyConn conn;
    private DictationService.ContentControl control = null;
//    private DictationThread dictationThread;

    private class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            control = (DictationService.ContentControl) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dictation, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitView(view);
        InitName();
        InitHandler();
        InitService();
    }

    private void InitHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
            }
        };
    }
    private void InitService() {
        conn = new MyConn();
        Intent intent = new Intent(getActivity(),DictationService.class);
        getActivity().bindService(intent, conn,BIND_AUTO_CREATE);
    }

    private void InitName() {
        Bundle bundle = getArguments();
        if(bundle != null)
            name = bundle.getString("user_name");
        else{
            im_start_dictation.setVisibility(View.GONE);
            tv_tips.setVisibility(View.GONE);
            Toast.makeText(getActivity(),"请前往“我的资料”登录",Toast.LENGTH_SHORT).show();
        }

        if(name != null){
            SearchWords(name);
        }
    }

    private void SearchWords(String name) {
        userDb = new UserDb(getActivity(), "words_book", null, 1);
        db = userDb.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from words_book where user_name = ?", new String[]{name});
        while (cursor.moveToNext()){
            if (cursor.getString(cursor.getColumnIndex("words")) != null) {
                words.add(cursor.getString(cursor.getColumnIndex("words")));
            }
        }
        if(words.size() > 0){
        }
        else{
            Toast.makeText(getActivity(),"您还未收藏单词",Toast.LENGTH_SHORT).show();
            im_start_dictation.setVisibility(View.GONE);
            tv_tips.setVisibility(View.GONE);
        }
        cursor.close();
        db.close();
    }

    private void InitView(View view) {
        im_start_dictation = view.findViewById(R.id.im_start_dictation);
        im_pause_dictation = view.findViewById(R.id.im_pause_dictation);
        tv_tips = view.findViewById(R.id.tv_tips_dication);
        tv_dictation_advice = view.findViewById(R.id.tv_dictation_advice);
        ll_dictation = view.findViewById(R.id.Ll_dictation);
        et_answer = view.findViewById(R.id.et_answer);
        btn_submit = view.findViewById(R.id.btn_submit);
        im_start_dictation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                im_start_dictation.setVisibility(View.GONE);
                tv_tips.setVisibility(View.GONE);
                tv_dictation_advice.setVisibility(View.VISIBLE);
                im_pause_dictation.setVisibility(View.VISIBLE);
                ll_dictation.setVisibility(View.VISIBLE);
                for (int i = 0;i< words.size();i++){
                    control.play(words.get(i));
                }
            }
        });
        im_pause_dictation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(im_pause_dictation.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.ic_pause_dictation).getConstantState())){
                    im_pause_dictation.setImageResource(R.drawable.ic_start_dictation);
                }
                else{
                    im_pause_dictation.setImageResource(R.drawable.ic_pause_dictation);
                }
            }
        });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
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
}
