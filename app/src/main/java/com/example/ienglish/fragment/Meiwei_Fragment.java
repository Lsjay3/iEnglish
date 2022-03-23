package com.example.ienglish.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ienglish.Bean.Words;
import com.example.ienglish.R;
import com.example.ienglish.ieng_main_activity;


public class Meiwei_Fragment extends Fragment implements ieng_main_activity.Send_Words, ieng_main_activity.FragmentBackListener {


    private WebView webView;

    private boolean isClose;

    private ieng_main_activity activity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meiwen, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitView(view);
    }


    private void InitView(View view) {
        webView = view.findViewById(R.id.wv_hujiangEnglish);
        webView.loadUrl("https://m.hujiang.com/en_meiwen/shuangyu/");
        webView.getSettings().setJavaScriptEnabled(true);//启用js
        webView.getSettings().setLoadsImagesAutomatically(true); //支持自动加载图片
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //使用WebView加载显示url
                view.loadUrl(url);
                //返回true
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                view.getSettings().setBlockNetworkImage(true);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    view.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                if (isClose) { //如果线程正在运行就不用重新开启一个线程了
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        isClose = true;
                        while (isClose) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessage(0x001);
                        }
                    }
                }).start();
            }
            Handler handler = new Handler() {          //清除原来页面的标题栏 参考博客https://www.jianshu.com/p/d2f82ab4ea3c
                @Override
                public void handleMessage(Message msg) {
                    String js = getClearAdDivJs(getContext());
                    //Log.v("adJs", js);
                    webView.loadUrl(js); //加载js方法代码
                    webView.loadUrl("javascript:hideAd();"); //调用js方法
                }
            };

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.getSettings().setBlockNetworkImage(false);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    view.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                isClose = false;
            }
        });

    }
    public String getClearAdDivJs(Context context) {
        String js = "javascript:function hideAd() {";
        Resources res = context.getResources();
        String[] adDivs = res.getStringArray(R.array.adBlockDiv);
        for (int i = 0; i < adDivs.length; i++) {
            //通过div的id属性删除div元素
            //js += "var adDiv"+i+"= document.getElementById('"+adDivs[i]+"');if(adDiv"+i+" != null)adDiv"+i+".parentNode.removeChild(adDiv"+i+");";
            //通过div的class属性隐藏div元素
            js += "var adDiv" + i + "= document.getElementsByClassName('" + adDivs[i] + "');if(adDiv" + i + " != null)" +
                    "{var x; for (x = 0; x < adDiv" + i + ".length; x++) {adDiv" + i + "[x].style.display='none';}}";
        }
        js += "}";
        return js;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.clearCache(true);
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (ieng_main_activity) context;
        activity.setBackListener(this);
    }
    @Override
    public void onDetach() {
        super.onDetach();
        activity.setBackListener(null);
    }


    @Override
    public void onBackForward() {
        if (webView!=null && webView.canGoBack()) {
            webView.goBack();                                      //返回上一页面
        }else {
            activity.setInterception(false);
        }
    }
}
