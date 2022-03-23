package com.example.ienglish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.ienglish.Bean.Words;
import com.example.ienglish.My_api_Application;
import com.example.ienglish.R;
import com.example.ienglish.apiservice.ContentPlayService;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Play_Content extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView im_prev,im_next,im_play;
    private TextView tv_yuanwen,tv_now_pos,tv_duration;
    public static Handler handler = null;
    private SeekBar sb;
    private Spinner spinner;
    private String speed , essay_content = null, essay_name = null;
    private int flag = 0;
    private ContentPlayService.ContentControl control = null;
    private List<String> content = new ArrayList<>();
    private MyConn conn;
    private TextView title;
    private int num = 0;
    private TextView tv_content , tv_content_trans , tv_content_hidden;
    private int slow = 0;      //判断是否为慢放
    private int time ;         //不均匀速度误差
    private String now_speed = "1.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play__content);
        setAndroidNativeLightStatusBar(this,true);
        InitView();
        InitHander();
        InitService();
        InitSeekBar();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void InitSeekBar() {
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Log.d("text",String.valueOf(control.isOver()));
                int x = seekBar.getProgress();
                //Log.d("text",String.valueOf(tv_content.getText().toString().length()));
                Log.d("text",x+" "+seekBar.getMax());
                if(slow != 1){
                    time = 30;
                }
                else {
                    time = 300;
                }
                if(seekBar.getMax()-seekBar.getProgress() < time){   //播放完毕，按照估计的误差
                    seekBar.setProgress(0);
                    im_play.setImageResource(R.drawable.ic_bofang);
                    num++;
                    control.stop();
                    if(num < content.size())
                    {
                        tv_content.setText(content.get(num));
                        Play();
                        SetSpeed();
                        SetShow();
                    }
                    else{
                        tv_content.setText("播放完毕！");
                        tv_content_trans.setVisibility(View.GONE);
                    }

                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int x = seekBar.getProgress();
                control.seekToPlay(x);
                im_play.setImageResource(R.drawable.ic_zanting);
            }
        });
    }


    private class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            control = (ContentPlayService.ContentControl) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }
    private void InitService() {
        conn = new MyConn();
        Intent intent = new Intent(this,ContentPlayService.class);
        bindService(intent, conn,BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(conn!=null)
        unbindService(conn);
    }

    private void InitHander() {
            handler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    Bundle bundle = msg.getData();
                    int duration = bundle.getInt("duration");
                    int now_pos = bundle.getInt("now_pos");
                    sb.setMax(duration);
                    sb.setProgress(now_pos);
                    tv_duration.setText(TransTime(duration));
                    tv_now_pos.setText(TransTime(now_pos));
                }
            };
    }
    public String TransTime(int time){               // 将时间格式改为00:00
        String strm = null,strs = null;
        int minute,second;
        minute = time/1000/60;
        second = time/1000%60;
        if(minute < 10){
            strm = "0" + minute;
        }
        else strm = "" + minute;
        if(second < 10){
            strs = "0" + second;
        }
        else strs = "" + second;
        String strtime = strm+":"+strs;
        return strtime;
    }

    private void InitView() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        tv_content_trans = findViewById(R.id.tv_play_content_trans);
        tv_content = findViewById(R.id.tv_play_content);
        title = findViewById(R.id.toolbar_title);
        tv_duration = findViewById(R.id.tv_duration);
        tv_now_pos = findViewById(R.id.tv_progress);
        im_prev = findViewById(R.id.im_prev);
        im_play = findViewById(R.id.im_play);
        spinner = findViewById(R.id.spinner);
        im_next = findViewById(R.id.im_next);
        tv_content_hidden = findViewById(R.id.tv_play_hidden);
        sb = findViewById(R.id.sb_content);
        tv_yuanwen = findViewById(R.id.tv_yuanwen);
        Intent intent = getIntent();
        essay_content = intent.getStringExtra("content");
        content = ContentSplit(essay_content);
        //Toast.makeText(this,String.valueOf(content.size()),Toast.LENGTH_SHORT).show();
        tv_content.setText(content.get(num));
        TransContent(tv_content.getText().toString());
        essay_name = intent.getStringExtra("essay_name");
        title.setText(essay_name);
        tv_yuanwen.setText("原文");
        tv_yuanwen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tv_yuanwen.getText().toString().equals("原文")){
                    tv_yuanwen.setText("译文");
                    tv_content.setVisibility(View.GONE);
                    tv_content_trans.setVisibility(View.VISIBLE);
                }
                else if(tv_yuanwen.getText().toString().equals("译文")){
                    tv_yuanwen.setText("原/译文");
                    tv_content.setVisibility(View.VISIBLE);
                    tv_content_trans.setVisibility(View.VISIBLE);
                }
                else if(tv_yuanwen.getText().toString().equals("原/译文")) {
                    tv_yuanwen.setText("隐藏");
                    tv_content.setVisibility(View.GONE);
                    tv_content_trans.setVisibility(View.GONE);
                    tv_content_hidden.setVisibility(View.VISIBLE);
                }
                else if(tv_yuanwen.getText().toString().equals("隐藏")){
                    tv_yuanwen.setText("原文");
                    tv_content.setVisibility(View.VISIBLE);
                    tv_content_trans.setVisibility(View.GONE);
                    tv_content_hidden.setVisibility(View.GONE);
                }
            }
        });
        im_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Play_Prev();
            }
        });
        im_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Play();
            }
        });
        im_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Play_Next();
            }
        });
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter
                .createFromResource(this, R.array.speed,
                        android.R.layout.simple_spinner_item);
        // 设置下拉列表样式
        arrayAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 为下拉列表设置适配器
        spinner.setAdapter(arrayAdapter);
        // 为下拉列表绑定事件监听器
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                                speed = parent.getItemAtPosition(position).toString();
                                if(flag != 0){
                                    switch (speed){
                                        case "1.0x":control.changeplayerSpeed(1f);now_speed = "1.0";break;
                                        case "0.5x":control.changeplayerSpeed(0.5f);
                                            now_speed = "0.5";
                                            slow = 1;
                                            break;
                                        case "2.0x":control.changeplayerSpeed(2.0f);now_speed = "2.0";break;
                                        case "1.5x":control.changeplayerSpeed(1.5f);now_speed = "1.5";break;
                                    }
                                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void SetShow() {
        TransContent(tv_content.getText().toString());
        switch (tv_yuanwen.getText().toString()){
            case "原文":
                tv_content_hidden.setVisibility(View.GONE);
                tv_content.setVisibility(View.VISIBLE);
                tv_content_trans.setVisibility(View.GONE);break;
            case "译文":
                tv_content_hidden.setVisibility(View.GONE);
                tv_content.setVisibility(View.GONE);
                tv_content_trans.setVisibility(View.VISIBLE);break;
            case "原/译文":
                tv_content_hidden.setVisibility(View.GONE);
                tv_content.setVisibility(View.VISIBLE);
                tv_content_trans.setVisibility(View.VISIBLE);break;
            case "隐藏":
                tv_content_hidden.setVisibility(View.VISIBLE);
                tv_content.setVisibility(View.GONE);
                tv_content_trans.setVisibility(View.GONE);break;
        }
    }
    private void SetSpeed(){                                    //设置速度
        switch (now_speed){
            case "1.0":control.changeplayerSpeed(1f);break;
            case "0.5":control.changeplayerSpeed(0.5f);break;
            case "1.5":control.changeplayerSpeed(1.5f);break;
            case "2.0":control.changeplayerSpeed(2.0f);break;
        }
    }
    private void TransContent(String s) {
        String url = "https://fanyi.youdao.com/openapi.do?keyfrom=wangtuizhijia&key=1048394636&type=data&doctype=json&version=1.1&q="+s;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();

                        Words words = gson.fromJson(response.toString(),Words.class);
                        if(words.getTranslation() != null){
                            tv_content_trans.setText(words.getTranslation().get(0));
                        }
                        else{
                            tv_content_trans.setText("warning:The text is too long");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley",error.toString());
                    }
                }
        );
        My_api_Application.addRequest(jsonObjectRequest,"Play_Content");
    }

    private void Play_Prev() {                      //上一句
        if( num!= 0){
            num--;
            sb.setProgress(0);
            im_play.setImageResource(R.drawable.ic_bofang);
            control.stop();
            tv_content.setText(content.get(num));
            Play();
            SetShow();
            SetSpeed();
        }
        else{
            Toast.makeText(this,"这已经是第一句了哦",Toast.LENGTH_SHORT).show();
        }
    }
    private void Play_Next() {                              //下一句
        if( num != content.size()-1){
            num++;
            sb.setProgress(0);
            im_play.setImageResource(R.drawable.ic_bofang);
            control.stop();
            tv_content.setText(content.get(num));
            Play();
            SetShow();
            SetSpeed();
        }
        else{
            Toast.makeText(this,"这已经是最后一句了哦",Toast.LENGTH_SHORT).show();
        }
    }

    private void Play() {                       //开始播放
        if(flag == 0)flag=1;
        if(im_play.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.ic_bofang).getConstantState())){
            if(!tv_content.getText().toString().equals("播放完毕！")) {
                control.play(tv_content.getText().toString());
                im_play.setImageResource(R.drawable.ic_zanting);
            }
        }
        else{
            control.pause();
            im_play.setImageResource(R.drawable.ic_bofang);
        }
    }
    private List<String> ContentSplit(String str) {      //将大段文章分割
        StringBuffer sb = new StringBuffer(300);
        String regEx="[。？！?.!;:]";
        Pattern p = Pattern.compile(regEx);
        List<String> list = new ArrayList<String>();
        Matcher m = p.matcher(str);
        if(str.length() >= 200) {
            /*按照句子结束符分割句子*/
            String[] substrs = p.split(str);
            //Log.d("ttt",substrs[1]);
            /*将句子结束符连接到相应的句子后*/
            if (substrs.length > 0) {
                int count = 0;
                while (count < substrs.length) {
                    if (m.find()) {
                        substrs[count] += m.group();
                    }
                    count++;
                }
            }
            for (int i = 0; i < substrs.length; i++) {    //如果分割后的句子长度超过290，则将其根据","再次分割一次
                if(substrs[i].length() > 290){
                    String st = substrs[i].substring(100,200);
                    list.add(substrs[i].substring(0,st.indexOf(",")+101));
                   // Log.d("A",String.valueOf(st.indexOf(",")));
                    list.add(substrs[i].substring(st.indexOf(",")+101));
                }
                else if (substrs[i].length() < 100) {    //语句小于要求的分割粒度
                    sb.append(substrs[i]);
                    //sb.append("||");
                    // Log.d("ttt",sb.toString());
                    if (sb.length() > 100) {
                        //System.out.println("A New TU: " + sb.toString());
                        list.add(sb.toString());
                        sb.delete(0, sb.length());
                    }
                } else {    //语句满足要求的分割粒度
                    if (sb.length() != 0)    //此时如果缓存有内容则应该先将缓存存入再存substrs[i]的内容  以保证原文顺序
                    {
                        list.add(sb.toString());
                        //System.out.println("A New Tu:"+sb.toString());
                        sb.delete(0, sb.length());
                    }
                    list.add(substrs[i]);
                }
            }
        }
        else{
            list.add(str);
        }
        Log.d("text",list.get(0));
        return list;
    }

    //设置监听事件
    public boolean onOptionsItemSelected(MenuItem item) {     //点击返回键
        switch (item.getItemId()) {
            case android.R.id.home:
                control.stop();
                finish();
                handler.removeCallbacksAndMessages(null);
                break;
        }
        return true;
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
