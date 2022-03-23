package com.example.ienglish.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.ienglish.Bean.Words;
import com.example.ienglish.IseResult.Result;
import com.example.ienglish.IseResult.xml.XmlResultParser;
import com.example.ienglish.My_api_Application;
import com.example.ienglish.R;
import com.example.ienglish.UserDB.UserDb;
import com.example.ienglish.apiservice.AudioService;
import com.example.ienglish.ieng_main_activity;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.cloud.EvaluatorListener;
import com.iflytek.cloud.EvaluatorResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;
import com.iflytek.cloud.SpeechUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;
import static com.iflytek.cloud.VerifierResult.TAG;


public class Fourth_Fragment extends Fragment implements ieng_main_activity.Send_Words{

    private String pron_uk,pron_us;
    private String input = "";
    private String explain = "";
    private RelativeLayout relativeLayout;
    private List<String> words = new ArrayList();
    private int i = 0;
    private ImageView im_uk;
    //图片下标序号
    private int count = 0;
    private TextView tv_input,tv_pron_uk,tv_explain;
    //定义手势监听对象
    private GestureDetector gestureDetector;
    private SQLiteDatabase db;
    private UserDb userDb;
    private String name;
    private Button btn_delete_words,btn_hidden_trans;
    private String word;
    private String user_name;
    private SwipeRefreshLayout sp;
    private AlertDialog mDlg;
    private String mLastResult;

    private TextView tv_translate;
    private ieng_main_activity.MyOnTouchListener myOnTouchListener;  // 设置手势滑动切换单词
    private ImageView im_record;
    private RelativeLayout rl_record;
    private TextView tv_grades_tips;
    private TextView tv_grades;
    private SpeechEvaluator mIse;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fourth, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitView(view);
        SpeechUtility.createUtility(getActivity(), SpeechConstant.APPID + "=5eba65f6");
        mIse = SpeechEvaluator.createEvaluator(getActivity(), null);
    }

    private void CalWords(String name) {
        userDb = new UserDb(getActivity(), "words_book", null, 1);
        db = userDb.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from words_book where user_name = ?", new String[]{name});
        while (cursor.moveToNext()){
            if (cursor.getString(cursor.getColumnIndex("words")) != null) {
               words.add(cursor.getString(cursor.getColumnIndex("words")));
            }
        }
        if(words.size() > 0){
            query(words.get(0));
            word = words.get(0);
            btn_delete_words.setVisibility(View.VISIBLE);
            btn_hidden_trans.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.VISIBLE);
        }

        else{
            Toast.makeText(getActivity(),"您还未收藏单词",Toast.LENGTH_SHORT).show();
            relativeLayout.setVisibility(View.GONE);
        }
        sp.setRefreshing(false);    //设置下拉悬浮球消失
        cursor.close();
        db.close();
    }

    private void InitView(View view) {
        tv_grades_tips = view.findViewById(R.id.tv_grades_tips);
        tv_grades = view.findViewById(R.id.tv_grades);
        rl_record = view.findViewById(R.id.rl_record);
        tv_input = view.findViewById(R.id.tv_input);
        tv_pron_uk = view.findViewById(R.id.tv_pron_e);
        tv_explain = view.findViewById(R.id.tv_explain);
        tv_translate = view.findViewById(R.id.tv_translate);
        relativeLayout = view.findViewById(R.id.ll);
        btn_delete_words = view.findViewById(R.id.btn_delete_words);
        btn_hidden_trans = view.findViewById(R.id.btn_hidden_trans);
        btn_hidden_trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btn_hidden_trans.getText().toString().equals("显示词义")){
                    tv_explain.setVisibility(View.VISIBLE);
                    btn_hidden_trans.setText("隐藏词义");
                }
                else {
                    tv_explain.setVisibility(View.INVISIBLE);
                    btn_hidden_trans.setText("显示词义");
                }
            }
        });
        im_record = view.findViewById(R.id.im_start_record);
        im_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Show_Record();
            }
        });
        im_uk = view.findViewById(R.id.im_pron_uk);
        sp = view.findViewById(R.id.mswipeRefreshLayout);
        im_uk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Speak("1",input);
            }
        });
        btn_delete_words.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(word != null&&!word.equals(""))
                MakeSure();
                else
                    Toast.makeText(getActivity(),"请先收藏单词！",Toast.LENGTH_SHORT).show();
            }
        });
        Bundle bundle = getArguments();
        if(bundle != null)
            name = bundle.getString("user_name");
        else{
            btn_delete_words.setVisibility(View.GONE);
            btn_hidden_trans.setVisibility(View.GONE);
            Toast.makeText(getActivity(),"请前往“我的资料”登录",Toast.LENGTH_SHORT).show();
        }

        if(name != null){
            CalWords(name);
        user_name = name;}
        gestureDetector = new GestureDetector(getActivity(),onGestureListener);
        myOnTouchListener = new ieng_main_activity.MyOnTouchListener() {
            @Override
            public boolean onTouch(MotionEvent ev) {
                return gestureDetector.onTouchEvent(ev);
            }
        };
        ((ieng_main_activity)getActivity()).registerMyOnTouchListener(myOnTouchListener);
        sp.setColorSchemeResources(R.color.swipeColor1,R.color.swipeColor2,R.color.swipeColor3,R.color.swipeColor4);
        /*设置下拉刷新的颜色
         * 设置下拉刷新的监听
         */
        sp.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                words.clear();
                CalWords(name);
            }
        });
    }


    // 评测监听接口
    private EvaluatorListener mEvaluatorListener = new EvaluatorListener() {

        @Override
        public void onResult(EvaluatorResult result, boolean isLast) {
            Log.d(TAG, "evaluator result :" + isLast);

            if (isLast) {
                StringBuilder builder = new StringBuilder();
                builder.append(result.getResultString());
                if(!TextUtils.isEmpty(builder)) {
                    tv_grades.setText(builder.toString());
                }
                mLastResult = builder.toString();
            }
        }
        @Override
        public void onError(SpeechError error) {
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.d(TAG, "evaluator begin");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.d(TAG, "evaluator stoped");
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

    };
    private void Show_Record() {
        AlertDialog.Builder builder =new AlertDialog.Builder(getContext());
        View layout = getLayoutInflater().inflate(R.layout.dialog_start_record,null);
        builder.setView(layout);
        mDlg = builder.create();
        Window window = mDlg.getWindow();
        window.setGravity(Gravity.BOTTOM);
        mDlg.setCanceledOnTouchOutside(true);
        mDlg.show();

        WindowManager manager = getActivity().getWindowManager();
        Display d=manager.getDefaultDisplay();
        WindowManager.LayoutParams p= mDlg.getWindow().getAttributes();
        p.width=d.getWidth();

        window.setBackgroundDrawable(new ColorDrawable(0));

        final TextView tv_record_tips = layout.findViewById(R.id.tv_record_tips);
        ImageView im_start_record = layout.findViewById(R.id.im_start_record);
        ImageView im_delete_record = layout.findViewById(R.id.im_record_delete);
        TextView tv_cancel = layout.findViewById(R.id.cancel);

        im_start_record.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                tv_record_tips.setText("正在录音...");
                if (mIse == null) {
                    return false;
                }
                mLastResult = null;
                setParams();                    //设置录音项，开始录音
                int ret = mIse.startEvaluating(word, null, mEvaluatorListener);  //将当前单词作为参数传入
                return false;
            }
        });
        im_start_record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (view.getId() == R.id.im_start_record ) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        tv_record_tips.setText("长按开始发音");
                        mDlg.dismiss();
                        ShowResult();
                    }
                }
                return false;
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDlg.dismiss();
            }
        });


    }
    private void setParams() {
        mIse.setParameter(SpeechConstant.LANGUAGE, "zh_en");
        mIse.setParameter(SpeechConstant.ISE_CATEGORY, "read_word");
        mIse.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        mIse.setParameter(SpeechConstant.RESULT_LEVEL, "complete");
        mIse.setParameter(SpeechConstant.AUDIO_FORMAT_AUE,"opus");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIse.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIse.setParameter(SpeechConstant.ISE_AUDIO_PATH, Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/ise.wav");
        //通过writeaudio方式直接写入音频时才需要此设置
        //mIse.setParameter(SpeechConstant.AUDIO_SOURCE,"-1");
    }
    private int flag = 1;
    private void ShowResult() {
        rl_record.setVisibility(View.VISIBLE);
        tv_grades_tips.setVisibility(View.VISIBLE);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        XmlResultParser resultParser = new XmlResultParser();
        Result result = resultParser.parse(mLastResult);

        if (null != result) {
            int final_score = (int)(result.total_score/5)*100;
            tv_grades.setText(String.valueOf(final_score));
        } else {
            //Toast.makeText(getActivity(),"解析结果为空",Toast.LENGTH_SHORT).show();
        }
        tv_grades.setVisibility(View.VISIBLE);
    }

    private void MakeSure() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("确认移除？");
            //设置确定按钮
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DeleteWords(word);
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

    private void DeleteWords(String word) {
        userDb = new UserDb(getActivity(), "words_book", null, 1);
        db = userDb.getWritableDatabase();
        Cursor cursor =  db.rawQuery("select * from words_book where words = ? and user_name = ?",new String[]{word,name});
        if(cursor.getCount()>0){
            db.execSQL("delete from words_book where user_name = '"+ name+"' and words = '"+ word +"'");
            words.remove(word);
            cursor.close();
            UpdateMsg();
            db.close();
            Toast.makeText(getActivity(),"移除成功",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getActivity(),"该单词已被移除,请下滑刷新",Toast.LENGTH_SHORT).show();
        }
    }
    private void UpdateMsg(){
        String num_words = null;
        userDb = new UserDb(getActivity(),"user_detail_mesg",null,1);
        db = userDb.getWritableDatabase();
        Cursor cursor =  db.rawQuery("select * from user_detail_mesg where user_name = ?",new String[]{user_name});
        while (cursor.moveToNext()){
            if (cursor.getString(cursor.getColumnIndex("words_num")) != null) {
                num_words = cursor.getString(cursor.getColumnIndex("words_num"));
            }
        }
        cursor.close();
        int num = Integer.valueOf(num_words) - 1;
        String numwords = String.valueOf(num);
        String update = "update user_detail_mesg set words_num = '" + numwords + "' where user_name = '"+ user_name + "'";
        db.execSQL(update);
        db.close();
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

    }

    @Override
    public void getResult(Words words) {

    }

    @Override
    public void getResultInChines(String sentence, String trans_res) {

    }

    private GestureDetector.OnGestureListener onGestureListener           //Fragment中的手势滑动
            = new GestureDetector.SimpleOnGestureListener(){
        //当识别的手势是滑动手势时回调onFinger方法
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                //得到手触碰位置的起始点和结束点坐标 x , y ，并进行计算
                float x = e2.getX()-e1.getX();
                float y = e2.getY()-e1.getY();
                //通过计算判断是向左还是向右滑动
                if(x > 0){
                    count--;
                    count=(count+(words.size()))%(words.size());

                }else if(x < 0){
                    count++;
                    count%=(words.size());
                }
                if(words.size() > 0){
                    query(words.get(count));
                    word = words.get(count);
                }
                else
                    Toast.makeText(getActivity(),"请先收藏单词",Toast.LENGTH_SHORT).show();
                return true;
         }
    };
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
                            pron_us = basic.get("us-phonetic").toString();      //得到美音
                            pron_uk = basic.get("uk-phonetic").toString();      //得到英音

                        } catch (JSONException e) {
                            pron_us = null;
                            pron_uk = null;
                            e.printStackTrace();
                        }
                        explain = "";
                        Words words = gson.fromJson(response.toString(),Words.class);   //得到包含数据的Gson对象
                        input = words.getQuery();
                        int explain_l = 0 ;
                        if(words.getBasic()!=null)
                            explain_l = words.getBasic().getExplains().size();
                        for(int i = 0;i < explain_l;i++){
                            if(words.getBasic().getExplains().get(i) != null)
                                explain = explain + words.getBasic().getExplains().get(i)+"\n";
                        }
                        flag = 1;
                        tv_input.setText(input);
                        tv_explain.setText(explain);
                        rl_record.setVisibility(View.GONE);
                        if(pron_uk!=null && pron_us!=null) {
                            tv_pron_uk.setText("/" + pron_uk + "/");
                        }
                        else {
                            tv_pron_uk.setText("发音");
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

        My_api_Application.addRequest(jsonObjectRequest,"Fourth_Fragment");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((ieng_main_activity) getActivity()).unregisterMyOnTouchListener(myOnTouchListener);  //将触摸监听取消注册

    }

}
