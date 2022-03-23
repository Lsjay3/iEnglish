package com.example.ienglish;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ienglish.UserDB.UserDb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EssayAdapter extends BaseAdapter {
    private String user_name,essay_name,essay_content;
    private String username,essayname,essaycontent;
    private SQLiteDatabase db;
    private UserDb userDb;

    private class Essay{
        TextView tv_essay_name;
        Button btn_delete_essay;
    }

    private List<Map<String,Object>> essay_msg;
    private LayoutInflater mInflater;
    private Context context;
    private String[] keyString;
    private int[] valueViewID;
    private Essay essay;
    private List<String> essay_name_list = new ArrayList<>();

    public EssayAdapter(List<Map<String, Object>> essay_msg, Context context, int resource,String[] from, int[] to) {
        this.essay_msg = essay_msg;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.keyString = new String[from.length];
        this.valueViewID = new int[to.length];
        System.arraycopy(from, 0, keyString, 0, from.length);
        System.arraycopy(to, 0, valueViewID, 0, to.length);
    }

    @Override
    public int getCount() {
        return essay_msg.size();
    }

    @Override
    public Object getItem(int i) {
        return essay_msg.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view != null){
            essay = (Essay) view.getTag();
        } else {
            view = mInflater.inflate(R.layout.essays_tip,null);
            essay = new Essay();
            essay.tv_essay_name = view.findViewById(valueViewID[0]);
            essay.btn_delete_essay = view.findViewById(valueViewID[1]);
            view.setTag(essay);
        }

        Map<String,Object> info = essay_msg.get(i);

        if(info != null){
            essayname = (String) info.get(keyString[0]);
            essay_name_list.add(essayname);
            username = (String) info.get(keyString[1]);
//            bookauthor = (String) info.get(keyString[1]);
//            bookintro = (String) info.get(keyString[2]);
            essay.tv_essay_name.setText(essayname);
            essay.btn_delete_essay.setOnClickListener(new BtnListener(i));
        }
        return view;
    }

    class BtnListener implements View.OnClickListener{
        private int position;            //目前点击的哪一个item

        public BtnListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            int vid = view.getId();
            if(vid == essay.btn_delete_essay.getId()){
                Delete(username,essay_name_list.get(position));
                notifyDataSetChanged();
            }

        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private void Delete(String username, String essayname) {
        userDb = new UserDb(context, "essay", null, 1);
        db = userDb.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from essay where essay_name = ? and user_name = ?",new String[]{essayname,username});
        if(cursor.getCount() > 0){
            db.execSQL("delete from essay where user_name = '"+ username+"' and essay_name = '"+ essayname +"'");
            UpdateMsg();
            db.close();
            Toast.makeText(context,"移除成功",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context,"该文章已被删除，请下滑刷新",Toast.LENGTH_SHORT).show();
        }

    }
    private void UpdateMsg(){
        String num_words = null;
        userDb = new UserDb(context,"user_detail_mesg",null,1);
        db = userDb.getWritableDatabase();
        Cursor cursor =  db.rawQuery("select * from user_detail_mesg where user_name = ?",new String[]{username});
        while (cursor.moveToNext()){
            if (cursor.getString(cursor.getColumnIndex("essay_num")) != null) {
                num_words = cursor.getString(cursor.getColumnIndex("essay_num"));
            }
        }
        cursor.close();
        int num = Integer.valueOf(num_words) - 1;
        String numwords = String.valueOf(num);
        String update = "update user_detail_mesg set essay_num = '" + numwords + "' where user_name = '"+ username + "'";
        db.execSQL(update);
        db.close();
    }
}
