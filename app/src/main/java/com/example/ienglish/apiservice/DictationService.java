package com.example.ienglish.apiservice;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import com.example.ienglish.fragment.Dictation_Fragment;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 此服务为听写播放服务
 */
public class DictationService extends Service {
    public MediaPlayer mediaPlayer = null;
    private Timer timer = null;

    public DictationService() {
    }

    private void addTimer(){
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //每隔50毫秒执行一次
                Message msg = Dictation_Fragment.handler.obtainMessage();
                if(mediaPlayer==null)return;
                int duration = mediaPlayer.getDuration();
                int now_pos = mediaPlayer.getCurrentPosition();
                Bundle bundle = new Bundle();
                bundle.putInt("duration",duration);
                bundle.putInt("now_pos",now_pos);
                msg.setData(bundle);
                Dictation_Fragment.handler.sendMessage(msg);
            }
        };
        timer.schedule(timerTask,1,50);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return new ContentControl();
    }
    public class ContentControl extends Binder{

        private int isover = 0;

        public void play(String s){
            Uri location = Uri.parse("https://dict.youdao.com/dictvoice?type=0&audio=" + s);
            if(mediaPlayer==null){
                isover = 0;
                mediaPlayer = MediaPlayer.create(getApplicationContext(),location);
                mediaPlayer.start();
                //Log.d("A",String.valueOf(mediaPlayer.getDuration()));
            }
            else{
                isover = 0;
                seekToPlay(mediaPlayer.getCurrentPosition());     //暂停后播放
                mediaPlayer.start();
            }
            addTimer();
        }
        public void seekToPlay(int x){         //拖动进度条
            isover = 0;
            mediaPlayer.seekTo(x);
            mediaPlayer.start();
        }
        public void pause(){
            isover = 0;
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }
        }
        public void stop(){
            /*停止音乐函数*/
            if (mediaPlayer!=null) {
                mediaPlayer.seekTo(0);
                mediaPlayer.stop();
                mediaPlayer.release();
                timer.cancel();
                mediaPlayer = null;
            }
        }
        public void changeplayerSpeed(float speed) {        //调整播放速度
            // this checks on API 23 and up
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
                }
            }
        }
        public int isOver(){                              //播放完成标志
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isover = 1;
                }
            });
            return isover;
        }
    }
}
