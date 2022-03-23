package com.example.ienglish.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.ienglish.Bean.Words;
import com.example.ienglish.Interface.PlayOver;
import com.example.ienglish.My_api_Application;
import com.example.ienglish.R;
import com.example.ienglish.UserDB.UserDb;
import com.example.ienglish.apiservice.AudioService;
import com.example.ienglish.ieng_main_activity;
import com.example.ienglish.module.MainContract;
import com.example.ienglish.module.MainPresenter;
import com.google.gson.Gson;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileWithBitmapCallback;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Second_Fragment extends Fragment implements MainContract.View,ieng_main_activity.Send_Words{


    private static final String TAG ="Second_Fragment";
    private static final int MIN_THRESHOLD = 50;
    private static final int MAX_THRESHOLD = 100;
    private static List<String>  sentence_list = new ArrayList<String>();
    private static List<String>  sentence_list_scan= new ArrayList<String>();
    private AlertDialog mDlg;
    private static final int MY_ADD_CASE_CALL_PHONE = 6;
    //调用系统相册请求码
    private static final int MY_ADD_CASE_CALL_PHONE2 = 7;
    private String content_trans = null,content_scan = null;
    private ImageView mImg,im_play,im_play_scan;
    private TextView content,your_photo,scan_result,tv_content_trans,tv_tras_result;
    private MainPresenter mPresenter;
    private FragmentManager manager;
    private FragmentTransaction ft;
    private Button again_scan;
    private TextView tv_scanning;
    private final int CUPREQUEST = 102;
    private Uri uritempFile,uritempCancle;
    private AudioService audioService;
    private int i = 1;   //判断阅读句子
    int isScan = 0;
    private String bookname = null, num_essay = null;
    private UserDb userDb;
    private SQLiteDatabase db;

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            audioService = ((AudioService.AudioBinder)iBinder).getService();
            audioService.setPlayOver(new PlayOver() {
                @Override
                public void IsPlayOver(int flag) {
                    if(flag == 1){
                        if(isScan == 1)
                        NextPlay(sentence_list_scan);
                        else {
                            NextPlay(sentence_list);
                        }
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private ScrollView sc;
    private Button btn_add_res;
    private Button btn_add_scan;
    private String name;

    private void NextPlay(List<String> sentence_list) {
        if(i < sentence_list.size()){
            if(i != sentence_list.size()-1)
                PlayResult(sentence_list.get(i),"1");
            else{
                PlayResult(sentence_list.get(i),"0");   //如果是最后一句，则读完关闭服务
            }
            i++;
        }
        else {
            //PlayResult(null,"0");
            getActivity().stopService(intent);
            getActivity().unbindService(conn);  //销毁时解绑服务
        }
    }

    private Intent intent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InitView(view);
        showDilg();
    }

    private void bindService(){
        final Intent intent = new Intent(getActivity(), AudioService.class);
        getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }
    private void InitView(View view) {
        manager = getFragmentManager();
        mImg = view.findViewById(R.id.image);
        content = view.findViewById(R.id.tv_content);
        mPresenter = new MainPresenter(this);
        scan_result = view.findViewById(R.id.tv_scan_result);
        your_photo = view.findViewById(R.id.tv_your_photo);
        tv_content_trans = view.findViewById(R.id.tv_content_trans);
        tv_tras_result = view.findViewById(R.id.tv_tras_result);
        again_scan = view.findViewById(R.id.btn_again_scan);
        tv_scanning = view.findViewById(R.id.tv_scanning);
        im_play = view.findViewById(R.id.im_play);
        sc = view.findViewById(R.id.sv_scroll);
        btn_add_res = view.findViewById(R.id.btn_add_result);
        btn_add_scan = view.findViewById(R.id.btn_add_scan);
        im_play_scan = view.findViewById(R.id.im_play_scan);
        Bundle bundle = getArguments();
        if(bundle != null)
            name = bundle.getString("user_name");
        btn_add_res.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name != null) {
                if (content_trans.equals("warning:The text is too long")) {
                    Toast.makeText(getActivity(), "收藏失败", Toast.LENGTH_SHORT).show();
                } else {
                    if (isContainChinese(content_trans)) {
                        Toast.makeText(getActivity(), "收藏纯英文效果最大哦", Toast.LENGTH_SHORT).show();
                    } else
                        showDilg_Name(content_trans);
                }
            }
                else{
                    Toast.makeText(getActivity(),"请前往“我的资料”登录",Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_add_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(name!=null){
                    if(isContainChinese(content_scan)){
                        Toast.makeText(getActivity(),"收藏纯英文效果最大哦",Toast.LENGTH_SHORT).show();
                    }
                    else
                        showDilg_Name(content_scan);
                }
                else
                    Toast.makeText(getActivity(),"请前往“我的资料”登录",Toast.LENGTH_SHORT).show();

            }
        });
        im_play_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bindService();
                i = 1;
                isScan = 1;
                sentence_list.clear();
                sentence_list_scan = ContentSplit(content_scan);
                PlayResult(sentence_list_scan.get(0),"1");
            }
        });
        im_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bindService();
                sentence_list_scan.clear();
                sentence_list = ContentSplit(content_trans);
                i = 1;
                PlayResult(sentence_list.get(0),"1");
                Log.d("text",sentence_list.get(0));
            }
        });
        again_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sentence_list.clear();
                sentence_list_scan.clear();
                sc.setVisibility(View.INVISIBLE);
                showDilg();
            }
        });
    }
    public static boolean isContainChinese(String str) {   //判断是否含有中文

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    private void Add_book(String s) {
        if(name != null){
            userDb = new UserDb(getActivity(),"user_detail_mesg",null,1);
            db = userDb.getWritableDatabase();
            Cursor cursor =  db.rawQuery("select * from user_detail_mesg where user_name = ?",new String[]{name});
            while (cursor.moveToNext()){
                if (cursor.getString(cursor.getColumnIndex("essay_num")) != null) {
                    num_essay = cursor.getString(cursor.getColumnIndex("essay_num"));
                }
                else{
                    num_essay = "0";
                }
            }
            cursor.close();
            int num = Integer.valueOf(num_essay) + 1;
            String numwords = String.valueOf(num);
            String update = "update user_detail_mesg set essay_num = '" + numwords + "' where user_name = '"+ name + "'";
            db.execSQL(update);
            db.close();
            userDb = new UserDb(getActivity(),"essay",null,1);
            db = userDb.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("content",s);
            values.put("user_name",name);
            values.put("essay_name",bookname);
            long index = db.insert("essay",null,values);
            if(index > 0)
                Toast.makeText(getActivity(),"收藏成功",Toast.LENGTH_SHORT).show();
            else
            if(index > 0)
                Toast.makeText(getActivity(),"收藏失败",Toast.LENGTH_SHORT).show();
            db.close();
        }
        else{
            Toast.makeText(getActivity(),"请先登录",Toast.LENGTH_SHORT).show();
        }
    }

    private void showDilg_Name(final String str){
            AlertDialog.Builder builder =new AlertDialog.Builder(getContext());
            View layout = getLayoutInflater().inflate(R.layout.dialog_setname,null);
            builder.setView(layout);
            mDlg = builder.create();
            Window window = mDlg.getWindow();
            window.setGravity(Gravity.CENTER);
            mDlg.setCanceledOnTouchOutside(true);
            mDlg.show();
            WindowManager manager = getActivity().getWindowManager();
            Display d=manager.getDefaultDisplay();
            WindowManager.LayoutParams p= mDlg.getWindow().getAttributes();
            p.width=d.getWidth();

            window.setBackgroundDrawable(new ColorDrawable(0));   // 设置背景透明

        final EditText et_name = layout.findViewById(R.id.et_book_name);
        Button btn_save = layout.findViewById(R.id.btn_save);

        btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bookname = et_name.getText().toString();
                    if(!IsNameExist(bookname)){
                        Add_book(str);
                        mDlg.dismiss();
                    }
                    else
                        Toast.makeText(getActivity(),"书名重复了哦，更换一个吧",Toast.LENGTH_SHORT).show();
                }
            });
    }

    private boolean IsNameExist(String bookname) {
        userDb = new UserDb(getActivity(),"essay",null,1);
        db = userDb.getWritableDatabase();
        Cursor cursor =  db.rawQuery("select * from essay where user_name = ?",new String[]{name});
        while (cursor.moveToNext()){
            if (bookname.equals(cursor.getString(cursor.getColumnIndex("essay_name")))) {
                return true;
            }
        }
        cursor.close();
        db.close();
        return false;
    }

    public void PlayResult(String s,String flag){
        if(intent == null)
        intent = new Intent(getActivity(), AudioService.class);
        intent.putExtra("query", s);
        intent.putExtra("f_pron", "0");
        intent.putExtra("flag",flag);
        getActivity().startService(intent);
    }
    private void showDilg(){
        AlertDialog.Builder builder =new AlertDialog.Builder(getContext());
        View layout = getLayoutInflater().inflate(R.layout.dialog_select_photo,null);
        builder.setView(layout);
        mDlg = builder.create();
        Window window = mDlg.getWindow();
        window.setGravity(Gravity.BOTTOM);
        mDlg.setCanceledOnTouchOutside(true);
        Log.e(TAG, "showDilg: " );
        mDlg.show();

        WindowManager manager = getActivity().getWindowManager();
        Display d=manager.getDefaultDisplay();
        WindowManager.LayoutParams p= mDlg.getWindow().getAttributes();
        p.width=d.getWidth();

        window.setBackgroundDrawable(new ColorDrawable(0));

        TextView button1 = layout.findViewById(R.id.photograph);
        TextView button2 = layout.findViewById(R.id.photo);
        TextView button3 = layout.findViewById(R.id.cancel);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan_result.setVisibility(View.GONE);
                tv_tras_result.setVisibility(View.GONE);
                im_play.setVisibility(View.GONE);
                tv_content_trans.setText("");
                content.setText("");
                CallCamera();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan_result.setVisibility(View.GONE);
                tv_tras_result.setVisibility(View.GONE);
                im_play.setVisibility(View.GONE);
                tv_content_trans.setText("");
                content.setText("");
                CallingAlbum();
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDlg.dismiss();
            }
        });
    }
    private void CallingAlbum() {
        //6.0动态申请权限，摄像头权限，SD卡写入取权限
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_ADD_CASE_CALL_PHONE
            );
        }else {
            choosePhoto();
        }

        mDlg.dismiss();
    }

    /**
     * 打开相册
     */
    private void choosePhoto() {
        //这是打开系统默认的相册(就是你系统怎么分类,就怎么显示,首先展示分类列表)
        Intent picture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(picture, 2);
    }

    private void CallCamera() {
        //6.0动态申请权限，摄像头权限，SD卡读取权限
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
                    ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_ADD_CASE_CALL_PHONE);
        }else {
            try{
                takePhoto();
            }catch (Exception e){e.printStackTrace();}
        }

        mDlg.dismiss();
    }

    private void takePhoto() throws IOException {
        Intent intent = new Intent();

        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        // 获取文件

        File file = createFileIfNeed("UserIcon.png");
        Log.d("Text",file.toString());

        //拍照后原图回存入此路径下
        Uri uri;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            uri = Uri.fromFile(file);
        } else {
            /**
             * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
             * 并且这样可以解决MIUI系统上拍照返回size为0的情况
             */

            uri = FileProvider.getUriForFile(getActivity(), "com.example.ienglish.provider", file);
        }
        uritempFile = uri;
        uritempCancle = uri;
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, 1);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_ADD_CASE_CALL_PHONE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    takePhoto();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //"权限拒绝");
                Toast.makeText(getActivity(), "请求摄像头权限被拒绝了", Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == MY_ADD_CASE_CALL_PHONE2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhoto();
            } else {
                //"权限拒绝");
                Toast.makeText(getActivity(), "请求SDka权限被拒绝了", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    // 在sd卡中创建一保存图片（原图和缩略图共用的）文件夹
    private File createFileIfNeed(String fileName) throws IOException {
        String fileA = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/nbinpic";

        File fileJA = new File(fileA);
        if (!fileJA.exists()) {
            Log.d("Text",fileA);
            fileJA.mkdirs();
        }
        File file = new File(fileA, fileName);
        if (!file.exists()) {
            Log.d("Text",file.toString());
        }

        return file;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode != Activity.RESULT_CANCELED) {    //照相

            String state = Environment.getExternalStorageState();
            if (!state.equals(Environment.MEDIA_MOUNTED)) return;
            photoClip(uritempFile);

        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK             //选择相册
                && null != data) {
            try {
                Uri selectedImage = data.getData();
                photoClip(selectedImage);
                uritempCancle = selectedImage;
            } catch (Exception e) {
                //"上传失败");
            }
        }
        if(requestCode == 3 && resultCode == Activity.RESULT_OK ){
            if (uritempFile != null) {
                TinyImg(uritempFile);
            }
        }
        else if(requestCode == 3 && resultCode == Activity.RESULT_CANCELED){
            TinyImg(uritempCancle);
        }

    }

    public void TinyImg(Uri uri){
        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
        Tiny.getInstance().source(uri).asFile().withOptions(options).compress(new FileWithBitmapCallback() {
            @Override
            public void callback(boolean isSuccess, Bitmap bitmap, String outfile, Throwable t) {
                if(bitmap == null){
                    Toast.makeText(getActivity(),"aaa",Toast.LENGTH_SHORT).show();
                }
                mImg.setImageBitmap(bitmap);
                mPresenter.getRecognitionResultByImage(bitmap);
                your_photo.setVisibility(View.VISIBLE);
                tv_scanning.setVisibility(View.VISIBLE);
                //saveImageToServer(bitmap, outfile);
            }
        });
    }

    /**
     * 从保存原图的地址读取图片
     */
    private String readpic() {
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/nbinpic/" + "UserIcon.png";

        return filePath;
    }

    private void saveImageToServer(final Bitmap bitmap, String outfile) {

        File file = new File(outfile);
        //Log.d(TAG, bitmap.getWidth() + " - " + bitmap.getHeight());
        mImg.setImageBitmap(bitmap);
    }

    @Override
    public void updateUI(String s) {
        scan_result.setVisibility(View.VISIBLE);
        content.setText(s);
        tv_tras_result.setVisibility(View.VISIBLE);
        tv_scanning.setVisibility(View.GONE);
        content_scan = s;
        //sentence_list_scan = ContentSplit(s);
        query(s);
    }


    private List<String> ContentSplit(String str) {
        StringBuffer sb = new StringBuffer(MAX_THRESHOLD);
        String regEx="[。？！?.!;]";
        Pattern p = Pattern.compile(regEx);
        List<String> list = new ArrayList<String>();
        Matcher m = p.matcher(str);
        if(str.length() >= 50) {
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
            for (int i = 0; i < substrs.length; i++) {

                if (substrs[i].length() < MIN_THRESHOLD) {    //语句小于要求的分割粒度
                    sb.append(substrs[i]);
                    //sb.append("||");
                    // Log.d("ttt",sb.toString());
                    if (sb.length() > MIN_THRESHOLD) {
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

    public void query(String s){
        String url = "https://fanyi.youdao.com/openapi.do?keyfrom=wangtuizhijia&key=1048394636&type=data&doctype=json&version=1.1&q="+s;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();

                        Words words = gson.fromJson(response.toString(),Words.class);
                        sc.setVisibility(View.VISIBLE);
                        if(words.getTranslation() != null){
                            //Log.d("Volley",words.getTranslation().get(0));
                            im_play.setVisibility(View.VISIBLE);
                            im_play_scan.setVisibility(View.VISIBLE);
                            tv_content_trans.setText(words.getTranslation().get(0));
                            content_trans = words.getTranslation().get(0);
                            //sentence_list = ContentSplit(words.getTranslation().get(0));
                        }
                        else{
                            im_play.setVisibility(View.VISIBLE);
                            im_play_scan.setVisibility(View.VISIBLE);
                            tv_content_trans.setText("warning:The text is too long");
                            content_trans = "warning:The text is too long";
                            //sentence_list = ContentSplit("warning:The text is too long");
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

        My_api_Application.addRequest(jsonObjectRequest,"Second_Fragment");

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

    private void photoClip(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        // aspectX aspectY 是宽高的比例
        //这个是处理华为裁剪是圆形框的问题
        if (Build.MANUFACTURER.equals("HUAWEI")) {
            intent.putExtra("aspectX", 9998);
            intent.putExtra("aspectY", 9999);
        } else {
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        }
        // outputX outputY 是裁剪图片宽高  这个值越大清晰度越高  但是太大了会崩
        intent.putExtra("outputX", 800);
        intent.putExtra("outputY", 800);
        intent.putExtra("return-data", false);

        uritempFile = Uri.parse("file://" + "/" +       Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);

        startActivityForResult(intent, 3);
    }
}
