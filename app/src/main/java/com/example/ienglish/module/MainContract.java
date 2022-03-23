package com.example.ienglish.module;

import android.graphics.Bitmap;

/**
 * @author chaochaowu
 * @Description : MainContract
 * @class : MainContract
 * @time Create at 14/5/2020 4:27 PM
 */


public interface MainContract {

    interface View{
        void updateUI(String s);
    }

    interface Presenter{
        void getAccessToken();
        void getRecognitionResultByImage(Bitmap bitmap);
    }

}
