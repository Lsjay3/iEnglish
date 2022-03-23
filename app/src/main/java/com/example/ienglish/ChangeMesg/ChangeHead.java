package com.example.ienglish.ChangeMesg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ienglish.R;
import com.example.ienglish.UserDB.UserDb;
import com.example.ienglish.ieng_main_activity;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileWithBitmapCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

public class ChangeHead extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView im_head;
    private AlertDialog mDlg;
    private static final int MY_ADD_CASE_CALL_PHONE = 6;
    //调用系统相册请求码
    private static final int MY_ADD_CASE_CALL_PHONE2 = 7;
    private String filename,user_name;
    private UserDb userDb;
    private SQLiteDatabase db;
    private Bitmap newhead = null;
    private Uri uritempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_head);
        setAndroidNativeLightStatusBar(this,true);
        InitView();
        Intent intent = getIntent();
        user_name = intent.getStringExtra("user_name");
        FindHead();
        showDilg();
    }

    private void FindHead() {
        userDb = new UserDb(ChangeHead.this,"user_detail_mesg",null,1);
        db = userDb.getWritableDatabase();
        Cursor cursor =  db.rawQuery("select * from user_detail_mesg where user_name = ?",new String[]{user_name});
        while (cursor.moveToNext()) {
            if (cursor.getBlob(cursor.getColumnIndex("user_head")) != null) {
                byte[] in = cursor.getBlob(cursor.getColumnIndex("user_head"));
                ByteArrayInputStream stream = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex("user_head")));
                im_head.setImageDrawable(Drawable.createFromStream(stream, "im_head_change"));
            }
        }
    }

    private void InitView() {
        toolbar = findViewById(R.id.toolbar);
        im_head = findViewById(R.id.im_head_change);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        im_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDilg();
            }
        });
    }
    private void showDilg(){
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        View layout = getLayoutInflater().inflate(R.layout.dialog_select_photo,null);
        builder.setView(layout);
        mDlg = builder.create();
        Window window = mDlg.getWindow();
        window.setGravity(Gravity.BOTTOM);
        mDlg.setCanceledOnTouchOutside(true);
        mDlg.show();

        WindowManager manager = getWindowManager();
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
                CallCamera();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_ADD_CASE_CALL_PHONE
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
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


        filename = String.valueOf(new Date().getTime());
        File file = createFileIfNeed(filename+".png");
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

            uri = FileProvider.getUriForFile(this, "com.example.ienglish.provider", file);
        }
        uritempFile = uri;
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
                Toast.makeText(this, "请求摄像头权限被拒绝了", Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == MY_ADD_CASE_CALL_PHONE2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhoto();
            } else {
                //"权限拒绝");
                Toast.makeText(this, "请求SDka权限被拒绝了", Toast.LENGTH_SHORT).show();
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {   //选择操作后的处理函数
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode != Activity.RESULT_CANCELED) {    //照相

            String state = Environment.getExternalStorageState();
            if (!state.equals(Environment.MEDIA_MOUNTED)) return;
            // 把原图显示到界面上
            /*Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
            Tiny.getInstance().source(readpic()).asFile().withOptions(options).compress(new FileWithBitmapCallback() {
                @Override
                public void callback(boolean isSuccess, Bitmap bitmap, String outfile, Throwable t) {
                    im_head.setImageBitmap(bitmap);
                    File file = new File(outfile);
                    //startPhotoZoom(Uri.fromFile(file));
                    newhead = bitmap;
                }
            });*/
            photoClip(uritempFile);

        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK             //选择相册
                && null != data) {
            try {
                Uri selectedImage = data.getData();

                /*Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
                Tiny.getInstance().source(selectedImage).asFile().withOptions(options).compress(new FileWithBitmapCallback() {
                    @Override
                    public void callback(boolean isSuccess, Bitmap bitmap, String outfile, Throwable t) {
                        im_head.setImageBitmap(bitmap);
                        newhead = bitmap;
                    }
                });*/
                photoClip(selectedImage);
               // startPhotoZoom(selectedImage);
            } catch (Exception e) {
                //"上传失败");
            }
        }
        if(requestCode == 3){         //如果是截取图像
            if (uritempFile != null) {
                Bitmap bitmap= decodeUriBitmap(uritempFile);
                im_head.setImageBitmap(bitmap);
                newhead = bitmap;
            }
        }
    }
    private Bitmap decodeUriBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }


    /**
     * 从保存原图的地址读取图片
     */
    private String readpic() {
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/nbinpic/" + filename + ".png";

        return filePath;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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

    public void Change(View view) {           //更改头像
        if (newhead != null) {
            userDb = new UserDb(ChangeHead.this, "user_detail_mesg", null, 1);
            db = userDb.getWritableDatabase();
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            newhead.compress(Bitmap.CompressFormat.PNG, 100, os);
            Log.d("Text", os.toByteArray().toString());
            ContentValues values = new ContentValues();
            values.put("user_head", os.toByteArray()); // 对应表字段img
            db.update("user_detail_mesg", values, "user_name = ?", new String[]{user_name});
            Toast.makeText(this, "修改成功！", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this,"头像未修改",Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(ChangeHead.this, ieng_main_activity.class);
        intent.putExtra("user_name",user_name);
        startActivity(intent);
    }
    private void photoClip(Uri uri) {    //裁减图片
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        // aspectX aspectY 是宽高的比例
        //这个是处理华为裁剪是圆形框的问题
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
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
