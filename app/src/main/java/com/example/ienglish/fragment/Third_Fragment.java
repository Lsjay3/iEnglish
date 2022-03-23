package com.example.ienglish.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ienglish.Bean.Words;
import com.example.ienglish.EssayAdapter;
import com.example.ienglish.R;
import com.example.ienglish.UserDB.UserDb;
import com.example.ienglish.ieng_main_activity;
import com.example.ienglish.Play_Content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Third_Fragment extends Fragment implements ieng_main_activity.Send_Words{

    private ListView lv_essays;
    private String user_name;
    final List<Map<String,Object>> essay_list = new ArrayList<>();
    private UserDb userDb;
    private SQLiteDatabase db;
    private List<String> content = new ArrayList<>();
    private List<String> essay_name = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_third, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitView(view);
        content.clear();
        essay_list.clear();
        essay_name.clear();
        if(user_name!=null)
        FindEssay(user_name);
        else
            Toast.makeText(getActivity(),"请前往“我的资料”登录",Toast.LENGTH_SHORT).show();
    }

    private void FindEssay(String user_name) {
        swipeRefreshLayout.setRefreshing(false);
        userDb = new UserDb(getActivity(),"essay",null,1);
        db = userDb.getWritableDatabase();
        Cursor cursor =  db.rawQuery("select * from essay where user_name = ?",new String[]{user_name});
        if(cursor.getCount()>0){
            while (cursor.moveToNext()){

                if(cursor.getString(cursor.getColumnIndex("essay_name"))!=null){
                    Map<String,Object> item = new HashMap<>();
                    item.put("essay_name",cursor.getString(cursor.getColumnIndex("essay_name")));
                    item.put("user_name",cursor.getString(cursor.getColumnIndex("user_name")));
                    content.add(cursor.getString(cursor.getColumnIndex("content")));
                    essay_name.add(cursor.getString(cursor.getColumnIndex("essay_name")));
                    ShowEssayList(item);
                }
            }
        }
        else{
            Toast.makeText(getActivity(),"您还没收藏过文章哦",Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    private void ShowEssayList(Map<String, Object> item) {
        essay_list.add(item);
        EssayAdapter essayAdapter = new EssayAdapter(
                essay_list,
                getActivity(),
                R.layout.essays_tip,
                new String[]{"essay_name","user_name"},
                new int[]{R.id.tv_essay_name,R.id.btn_delete_essay}
        );
        lv_essays.setAdapter(essayAdapter);
    }

    public void InitView(View view){
        lv_essays = view.findViewById(R.id.lv_essays);
        Bundle bundle = getArguments();
        if(bundle != null){
            if(bundle.getString("user_name")!=null)
                user_name = bundle.getString("user_name");
            else
                Toast.makeText(getActivity(),"请先前往“我的资料”登录",Toast.LENGTH_SHORT).show();
        }
        lv_essays.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(FindEssayExist(user_name,essay_name.get(i))){
                    Intent intent = new Intent(getActivity(),Play_Content.class);
                    intent.putExtra("content",content.get(i));
                    intent.putExtra("user_name",user_name);
                    intent.putExtra("essay_name",essay_name.get(i));
                    //Toast.makeText(getActivity(),content.get(i),Toast.LENGTH_SHORT).show();
                    getActivity().startActivity(intent);
                }
                else{
                    Toast.makeText(getActivity(),"该文章不存在",Toast.LENGTH_SHORT).show();
                }
            }
        });
        swipeRefreshLayout = view.findViewById(R.id.srl_fresh_essay_list);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                content.clear();
                essay_list.clear();
                essay_name.clear();
                FindEssay(user_name);
            }
        });
    }

    private boolean FindEssayExist(String user_name, String essay_name) {
        userDb = new UserDb(getActivity(), "essay", null, 1);
        db = userDb.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from essay where essay_name = ? and user_name = ?",new String[]{essay_name,user_name});
        if(cursor.getCount() > 0){
            return true;
        }
        else{
            return false;
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

}
