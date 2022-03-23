package com.example.ienglish;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.ienglish.Bean.Words;
import com.example.ienglish.ChangeMesg.ChangeHead;
import com.example.ienglish.ChangeMesg.ChangeNick;
import com.example.ienglish.ChangeMesg.ChangeSign;
import com.example.ienglish.SerializableMap.SerializableMap;
import com.example.ienglish.UserDB.UserDb;
import com.example.ienglish.fragment.Dictation_Fragment;
import com.example.ienglish.fragment.First_Fragment;
import com.example.ienglish.fragment.Fourth_Fragment;
import com.example.ienglish.fragment.Meiwei_Fragment;
import com.example.ienglish.fragment.Mymsg_Fragment;
import com.example.ienglish.fragment.Second_Fragment;
import com.example.ienglish.fragment.Test_Fragment;
import com.example.ienglish.fragment.Third_Fragment;
import com.githang.statusbar.StatusBarCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.utils.L;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ieng_main_activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentManager fm;
    private TextView toolbar_title;
    private Toolbar toolbar;
    private SearchView searchView;
    private TextView tv_input;
    private String pron_uk,pron_us;
    private Send_Words send_words;
    private Send_Name send_name;
    private Fragment fragment;
    public static Words words;
    private TextView nickname;
    private TextView tv_sign;
    private ImageView im_head;
    private String user_name = null;
    private UserDb userDb;
    private SQLiteDatabase db;
    private WebView wv_hujiang;
    private SharedPreferences share;

    public interface Send_Words{      //英文速查的数据接口
        public void sendPron(String uk,String us,String name);
        public void getResult(Words words);
        public void getResultInChines(String sentence,String trans_res);
    }

    public interface Send_Name{      //向各Fragment传递用户名名字
        public void sendName(String name);
    }

    public interface FragmentBackListener {     //主界面设置返回键，防止整个app退出
        void  onBackForward();
    }
    private FragmentBackListener backListener;
    private boolean isInterception = false;
    public FragmentBackListener getBackListener() {
        return backListener;
    }

    public void setBackListener(FragmentBackListener backListener) {
        this.backListener = backListener;
    }

    public boolean isInterception() {
        return isInterception;
    }

    /**
     * 区别Activity和Fragment返回键的监听事件
     * 可以自主在Fragment中设置监听事件
     * @param isInterception
     */
    public void setInterception(boolean isInterception) {
        this.isInterception = isInterception;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            //Toast.makeText(ieng_main_activity.this,"aaa",Toast.LENGTH_SHORT).show();
            if(!isInterception){
                //处理Fragment中的返回键监听事件
                if (backListener != null) {
                    backListener.onBackForward();
                    return false;
                }
            }else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ieng_main_activity);
        //StatusBarCompat.setStatusBarColor(this, Color.WHITE);    //更改状态栏颜色
        InitView();
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_search:
                        searchView = (SearchView) item.getActionView();
                        searchView.setQueryHint("在此输入");
                        searchView.onActionViewExpanded();
                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String s) {   ////点击软键盘搜索的时候执行
                                if(isChinese(s))
                                    queryBySeq2Seq(s);
                                else
                                query(s);
                                switchFragment("英文速查");
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String s) {    //搜索框文本发生改变的时候执行
                                return false;
                            }
                        });
                        //Toast.makeText(ieng_main_activity.this,"aaaa",Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
        //toolbar.setOnMenuItemClickListener();
        //setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);
        InitMesView(view);                                                      //左侧侧滑栏资料VIew初始化
        fm = this.getSupportFragmentManager();
        switchFragment("美文推荐");
        setAndroidNativeLightStatusBar(this,true);
        Intent intent = getIntent();
        if(intent.getStringExtra("user_name") == null)
            GetusernameFromShare();
        else
        user_name = intent.getStringExtra("user_name");

        //Toast.makeText(ieng_main_activity.this,user_name,Toast.LENGTH_SHORT).show();
        if(user_name != null)
        SetPersonMsg(user_name);
    }



    private void GetusernameFromShare() {
        share = getSharedPreferences("UserName",Activity.MODE_PRIVATE);
        if(share.getString("username","")!=null&&!share.getString("username","").equals(""))
        user_name = share.getString("username","");


    }


    private void SetPersonMsg(String user_name) {         //更新登录基本信息
        userDb = new UserDb(ieng_main_activity.this,"user_detail_mesg",null,1);
        db = userDb.getWritableDatabase();
        Cursor cursor =  db.rawQuery("select * from user_detail_mesg where user_name = ?",new String[]{user_name});
        while (cursor.moveToNext()) {
            if (cursor.getBlob(cursor.getColumnIndex("user_head"))!=null) {
                ByteArrayInputStream stream = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex("user_head")));
                im_head.setImageDrawable(Drawable.createFromStream(stream, "im_head"));
            }
            if(cursor.getString(cursor.getColumnIndex("user_sign"))!=null){
                tv_sign.setText(cursor.getString(cursor.getColumnIndex("user_sign")));
            }
            if(cursor.getString(cursor.getColumnIndex("user_nickname"))!=null){
                nickname.setText(cursor.getString(cursor.getColumnIndex("user_nickname")));
            }
        }
    }

    private void InitMesView(View view) {
        nickname = view.findViewById(R.id.tv_nickname);
        tv_sign = view.findViewById(R.id.tv_sign);
        im_head = view.findViewById(R.id.im_head);
        nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user_name!=null) {
                    Intent intent = new Intent(ieng_main_activity.this, ChangeNick.class);
                    intent.putExtra("user_name", user_name);
                    startActivity(intent);
                }
                else
                    Toast.makeText(ieng_main_activity.this,"请前往“我的资料”登录",Toast.LENGTH_SHORT).show();
            }
        });
        tv_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user_name!=null) {
                    Intent intent = new Intent(ieng_main_activity.this, ChangeSign.class);
                    intent.putExtra("user_name", user_name);
                    startActivity(intent);
                }
                else
                    Toast.makeText(ieng_main_activity.this,"请前往“我的资料”登录",Toast.LENGTH_SHORT).show();
            }
        });
        im_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user_name!=null) {
                    Intent intent = new Intent(ieng_main_activity.this, ChangeHead.class);
                    intent.putExtra("user_name", user_name);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(ieng_main_activity.this,"请前往“我的资料”登录",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void InitView() {
        tv_input = findViewById(R.id.tv_input);
        toolbar = findViewById(R.id.toolbar);
        toolbar_title = findViewById(R.id.toolbar_title);
    }

    private static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {   //更改状态栏字体
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ieng_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String title = item.getTitle().toString();
        switchFragment(title);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    String[] titles = {"美文推荐","英文速查","美文发现","我的文章","单词本","我的资料","单词听写","测试"};
    Fragment[] fs = {new Meiwei_Fragment(),new First_Fragment(),new Second_Fragment(),new Third_Fragment(),new Fourth_Fragment(),new Mymsg_Fragment(),new Dictation_Fragment(),new Test_Fragment()};
    Fragment mfragmenti = new First_Fragment();  //存在一个Fragment
    public void switchFragment(String title){

        FragmentTransaction t = fm.beginTransaction();
        for(int i=0;i<titles.length;i++){
            if(titles[i].equals(title)){
                if(user_name != null){
                    Bundle bundle=new Bundle();
                    bundle.putString("user_name",user_name);
                    fs[i].setArguments(bundle);
                }
                t.replace(R.id.relativeLayout,fs[i],title).commit();
            }
        }
       toolbar_title.setText(title);
    }

    private void queryBySeq2Seq(String s) {
        String url = "http://192.168.43.20:5000/translate?s="+s;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Volley",response.toString());
                        try {
                            String res = response.getString("trans_res");

                            send_words.getResultInChines(s,res.substring(0, res.length() - 5));
                        } catch (JSONException e) {
                            e.printStackTrace();
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

        My_api_Application.addRequest(jsonObjectRequest,"ieng_main_activity");
    }
    public void query(String s){
        String url = "https://fanyi.youdao.com/openapi.do?keyfrom=wangtuizhijia&key=1048394636&type=data&doctype=json&version=1.1&q="+s;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        try {
                            JSONObject basic = null;
                            if(response.getJSONObject("basic")!=null)
                            basic = response.getJSONObject("basic");
                            pron_us = basic.get("us-phonetic").toString();
                            pron_uk = basic.get("uk-phonetic").toString();

                        } catch (JSONException e) {
                            pron_us = null;
                            pron_uk = null;
                            e.printStackTrace();
                        }
                        Words words = gson.fromJson(response.toString(),Words.class);
                        send_words.getResult(words);
                        send_words.sendPron(pron_uk,pron_us,user_name);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley",error.toString());
                    }
                }

        );

        My_api_Application.addRequest(jsonObjectRequest,"ieng_main_activity");

    }

    public static boolean isChinese(String s) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(s);
        if (m.find()) {
            return true;
        }
        return false;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {

        send_words = (Send_Words) fragment;

        super.onAttachFragment(fragment);
    }
    //1.触摸事件接口
    public interface MyOnTouchListener {
        public boolean onTouch(MotionEvent ev);
    }
    //2. 保存MyOnTouchListener接口的列表
    private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>();
    //3.分发触摸事件给所有注册了MyOnTouchListener的接口
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (MyOnTouchListener listener : onTouchListeners) {
            listener.onTouch(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
    //4.提供给Fragment通过getActivity()方法来注册自己的触摸事件的方法
    public void registerMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        onTouchListeners.add(myOnTouchListener);
    }
    //5.提供给Fragment通过getActivity()方法来注销自己的触摸事件的方法
    public void unregisterMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        onTouchListeners.remove(myOnTouchListener);
    }

    public static void clear(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("UserName", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}
