package com.example.cloudmusic.appliction;

import android.app.Application;

import com.example.lib_audio.app.AudioHelper;

public class CloudMusicApplication extends Application {

    private static CloudMusicApplication mApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        //音频SDK初始化
        AudioHelper.init(this);
    }

    public static CloudMusicApplication getInstance(){
        return mApplication;
    }
}
