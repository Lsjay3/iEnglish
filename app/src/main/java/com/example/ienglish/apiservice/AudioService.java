package com.example.ienglish.apiservice;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.ienglish.Interface.PlayOver;

/**
 * 此服务为扫描结果播放服务
 */
public class AudioService extends Service {
    public AudioService() {
    }

    private MediaPlayer mp;
    private String query;
    private String f_pro;
    private PlayOver playOver;

    public void setPlayOver(PlayOver playOver) {
        this.playOver = playOver;
    }

    /**
     * 增加get()方法，供Activity调用
     * @return 下载进度
     */
    public int getPlayOver() {
        return 0;
    }

    @Override
    public void onCreate() {
        System.out.println("初始化音乐资源  ");
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        if (query != null && !query.equals(intent.getStringExtra("query")) && mp != null) {
            mp.start();
        } else {
            String f_pro = intent.getStringExtra("f_pron");    //判断发音
            String query = intent.getStringExtra("query");
            final String flag = intent.getStringExtra("flag");  //判断是否为最后一句标志
            // System.out.println("http://dict.youdao.com/dictvoice?audio=" + query);
            Uri location = Uri.parse("https://dict.youdao.com/dictvoice?type="+f_pro+"&audio=" + query);

            mp = MediaPlayer.create(this, location);
             //Log.d("text",String.valueOf(mp.getDuration()));
            mp.start();
            // 音乐播放完毕的事件处理
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    // 不循环播放
                    try {

                        if(!flag.equals("0"))
                        playOver.IsPlayOver(1);    //提醒Fragment开始读第二句
                        else{
                            mp.stop();                   //已经读完，释放资源
                            mp.release();
                        }
                        // mp.start();
                        System.out.println("stopped");
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            });

            // 播放音乐时发生错误的事件处理
            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // 释放资源
                    try {
                        mp.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }

        // super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        // 服务停止时停止播放音乐并释放资源
        mp.stop();
        mp.release();
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new AudioBinder();
    }
    public class AudioBinder extends Binder {
        /**
         * 获取当前Service的实例
         * @return
         */
        public AudioService getService(){
            return AudioService.this;
        }
    }
}
