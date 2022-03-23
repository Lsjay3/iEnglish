package com.example.ienglish.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.LevelListDrawable;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.ienglish.Bean.Words;
import com.example.ienglish.ChangeMesg.ChangeSign;
import com.example.ienglish.MainActivity;
import com.example.ienglish.R;
import com.example.ienglish.UserDB.UserDb;
import com.example.ienglish.apiservice.AudioService;
import com.example.ienglish.ieng_main_activity;
import com.nostra13.universalimageloader.utils.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class First_Fragment extends Fragment implements ieng_main_activity.Send_Words {


    private TextView tv_input,tv_pron_uk,tv_pron_us,tv_explain,tv_web_explain,tv_translate,tv_web_ex,tv_short;
    ListView lv_short_words;
    final List<Map<String,Object>> short_wordss = new ArrayList<>();
    private String input = "";
    private String explain = "";
    private String web_explain = "";
    private String translate = "";
    private Map<String, Object> short_words;
    private LinearLayout linearLayout;
    private String user_name = null;
    private String num_words = "0";
    private UserDb userDb;
    private SQLiteDatabase db;
    private Button btn_coll;
    private TextView tv_tips;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_first, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitView(view);

    }

    private void SetResult() {
        tv_input.setText(input);
        tv_explain.setText(explain);
        tv_explain.setVisibility(View.VISIBLE);
        tv_web_explain.setText(web_explain);
        tv_web_explain.setVisibility(View.VISIBLE);
        tv_translate.setText("[释义] "+translate);
        if(!short_wordss.isEmpty()) {
            SimpleAdapter myAdapter = new SimpleAdapter(
                    getActivity(),
                    short_wordss,
                    R.layout.lv_shortwords,
                    new String[]{"short_key", "short_w"},
                    new int[]{R.id.lv_tv_key, R.id.lv_tv_value}
            );
            lv_short_words.setAdapter(myAdapter);
            lv_short_words.setVisibility(View.VISIBLE);
        }
    }

    private void InitView(View view) {
        tv_input = view.findViewById(R.id.tv_input);
        tv_pron_uk = view.findViewById(R.id.tv_pron_e);
        tv_pron_us = view.findViewById(R.id.tv_pron_a);
        tv_explain = view.findViewById(R.id.tv_explain);
        tv_tips = view.findViewById(R.id.tv_tips);
        tv_web_explain = view.findViewById(R.id.tv_web_explain);
        lv_short_words = view.findViewById(R.id.lv_shortwords);
        linearLayout = view.findViewById(R.id.linearlayout);
        tv_translate = view.findViewById(R.id.tv_translate);
        btn_coll = view.findViewById(R.id.btn_collection);
        tv_web_ex = view.findViewById(R.id.tv_web_ex);
        tv_short = view.findViewById(R.id.tv_short);
        btn_coll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collection_words();
            }
        });
        tv_pron_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Speak("0",input);
            }
        });
        tv_pron_uk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Speak("1",input);
            }
        });
    }

    public void Collection_words(){
        if(user_name != null){
            userDb = new UserDb(getActivity(),"user_detail_mesg",null,1);
            db = userDb.getWritableDatabase();
            Cursor cursor =  db.rawQuery("select * from user_detail_mesg where user_name = ?",new String[]{user_name});
            while (cursor.moveToNext()){
                if (cursor.getString(cursor.getColumnIndex("words_num")) != null) {
                    num_words = cursor.getString(cursor.getColumnIndex("words_num"));
                }
            }
            cursor.close();
            int num = Integer.valueOf(num_words) + 1;
            String numwords = String.valueOf(num);
            String update = "update user_detail_mesg set words_num = '" + numwords + "' where user_name = '"+ user_name + "'";
            db.execSQL(update);
            db.close();
            userDb = new UserDb(getActivity(),"words_book",null,1);
            db = userDb.getWritableDatabase();
            Cursor cursor1 = db.rawQuery("select * from words_book where words = ? and user_name = ?",new String[]{input,user_name});
            if(cursor1.getCount() == 0){
                ContentValues values = new ContentValues();
                values.put("words",input);
                values.put("user_name",user_name);
                long index = db.insert("words_book",null,values);
                if(index > 0)
                    Toast.makeText(getActivity(),"收藏成功",Toast.LENGTH_SHORT).show();
                else
                if(index > 0)
                    Toast.makeText(getActivity(),"收藏失败",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(),"单词本里已有该单词哦", Toast.LENGTH_SHORT).show();
            }
            db.close();
        }
        else{
            Toast.makeText(getActivity(),"请先登录",Toast.LENGTH_SHORT).show();
        }
    }
    public void Speak(String f_pron,String query){
        Intent intent = new Intent(getActivity(), AudioService.class);
        intent.putExtra("query", query);
        intent.putExtra("f_pron", f_pron);
        intent.putExtra("flag", "0");
        getActivity().startService(intent);
    }
    @Override
    public void sendPron(String uk, String us,String name) {
        if(uk != null && us != null) {
            linearLayout.setVisibility(View.VISIBLE);
            tv_pron_us.setText("美/" + us + "/");
            tv_pron_uk.setText("英/" + uk + "/");
        }
        user_name = name;
    }

    @Override
    public void getResult(Words words) {     //得到Gson对象后将数据提取到控件
        btn_coll.setVisibility(View.GONE);   //初始时VIew全都看不见
        tv_short.setVisibility(View.GONE);
        tv_web_ex.setVisibility(View.GONE);
        linearLayout.setVisibility(View.GONE);
        if (words.getErrorCode() == 0) {
            tv_tips.setVisibility(View.GONE);
            input = words.getQuery();
            translate = words.getTranslation().get(0);
            short_wordss.clear();
            explain = "";
            int explain_l = 0, web_l = 0, short_l = 0;
            if (words.getBasic() != null)
                explain_l = words.getBasic().getExplains().size();
            for (int i = 0; i < explain_l; i++) {
                if (words.getBasic().getExplains().get(i) != null)
                    explain = explain + words.getBasic().getExplains().get(i) + "\n";
            }
            web_explain = "";
            if (words.getWeb() != null) {
                web_l = words.getWeb().get(0).getValue().size();
                tv_web_ex.setVisibility(View.VISIBLE);
                short_l = words.getWeb().size();
            }
            for (int i = 0; i < web_l; i++) {
                if (words.getWeb().get(0).getValue().get(i) != null)
                    web_explain = web_explain + words.getWeb().get(0).getValue().get(i) + "\n";
            }
            for (int i = 1; i < short_l; i++) {
                String short_w = "";
                for (int j = 0; j < words.getWeb().get(i).getValue().size(); j++) {
                    if (words.getWeb().get(i).getValue().get(j) != null) {
                        tv_short.setVisibility(View.VISIBLE);
                        btn_coll.setVisibility(View.VISIBLE);
                        short_w = short_w + words.getWeb().get(i).getValue().get(j) + "\n";
                    }

                }
                String short_key = "";
                if (words.getWeb().get(i).getKey() != null)
                    short_key = words.getWeb().get(i).getKey();
                short_words = new HashMap<>();
                //Log.d("Text",short_w + "\n" + short_key);
                if (short_w != null && short_key != null) {
                    short_words.put("short_w", short_w);
                    short_words.put("short_key", short_key);
                    short_wordss.add(short_words);
                }
            }
            SetResult();
        }
        else{
            Toast.makeText(getActivity(),"请输入合法字符",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void getResultInChines(String sentence, String trans_res) {
        btn_coll.setVisibility(View.GONE);   //初始时VIew全都看不见
        tv_short.setVisibility(View.GONE);
        tv_web_ex.setVisibility(View.GONE);
        tv_explain.setVisibility(View.GONE);
        tv_web_explain.setVisibility(View.GONE);
        tv_short.setVisibility(View.GONE);
        linearLayout.setVisibility(View.GONE);
        lv_short_words.setVisibility(View.GONE);
        tv_tips.setVisibility(View.GONE);
        tv_input.setText(sentence);
        tv_translate.setText("[释义] \n"+trans_res);
    }
}
