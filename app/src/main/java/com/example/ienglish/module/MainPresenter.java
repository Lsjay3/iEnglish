package com.example.ienglish.module;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.example.ienglish.apiservice.BaiduOCRService;
import com.example.ienglish.Bean.AccessTokenBean;
import com.example.ienglish.Bean.RecognitionResultBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author jay
 * @Description : MainPresenter
 * @class : MainPresenter
 * @time Create at 14/5/2020 4:27 PM
 * 百度识图参考博客：https://www.jianshu.com/p/0ed2c5656035
 */


public class MainPresenter implements MainContract.Presenter{

    private MainContract.View mView;
    private BaiduOCRService baiduOCRService;

    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String API_KEY = "wUzqWSngEyXRfGWfuSZyG7Xg";
    private static final String SECRET_KEY = "9gsiC924UsSdZQc0pLFonZB8d5VwTzwx";
    private static final String ACCESS_TOKEN = "24.a932eeade30a3b8cbd83f797e49e63f5.2592000.1625215150.282335-23993292";

    public MainPresenter(MainContract.View mView) {

        this.mView = mView;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://aip.baidubce.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        baiduOCRService = retrofit.create(BaiduOCRService.class);

    }


    @Override
    public void getAccessToken() {

        baiduOCRService.getAccessToken(CLIENT_CREDENTIALS,API_KEY,SECRET_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccessTokenBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(AccessTokenBean accessTokenBean) {
                        Log.e("Access token",accessTokenBean.getAccess_token());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Access token","error");
                        if (e instanceof UndeliverableException) {
                            e = e.getCause();
                        }
                        if ((e instanceof IOException)) {
                            // fine, irrelevant network problem or API that throws on cancellation
                            return;
                        }
                        if (e instanceof InterruptedException) {
                            // fine, some blocking code was interrupted by a dispose call
                            return;
                        }
                        if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                            // that's likely a bug in the application
                            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                            return;
                        }
                        if (e instanceof IllegalStateException) {
                            // that's a bug in RxJava or in a custom operator
                            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                            return;
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }



    @Override
    public void getRecognitionResultByImage(Bitmap bitmap) {

        String encodeResult = bitmapToString(bitmap);

        baiduOCRService.getRecognitionResultByImage(ACCESS_TOKEN,encodeResult)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RecognitionResultBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(RecognitionResultBean recognitionResultBean) {
                        //Log.e("onnext",recognitionResultBean.toString());

                        StringBuilder s = new StringBuilder();
                        ArrayList<String> wordList = new ArrayList<>();
                        List<RecognitionResultBean.WordsResultBean> wordsResult = recognitionResultBean.getWords_result();
                        for (RecognitionResultBean.WordsResultBean words:wordsResult) {
                            wordList.add(words.getWords());
                            s.append(words.getWords() + " ");  //等到识别结果
                            //Log.e("onnext",words.getWords());
                        }

                        //ArrayList<String> numbs = RegexUtils.getNumbs(wordList);


                        ///Log.e("onnext",recognitionResultBean.toString());
                        /*for (String numb : numbs) {

                        }*/
                        mView.updateUI(s.toString());

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("onerror",e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }



    private String bitmapToString(Bitmap bitmap){     //将图像转为64位字节
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


}
