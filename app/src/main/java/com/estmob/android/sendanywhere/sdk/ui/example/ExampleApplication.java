package com.estmob.android.sendanywhere.sdk.ui.example;

import android.app.Application;
import android.os.Environment;

import com.estmob.sdk.transfer.SendAnywhere;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;

/**
 * Created by francisco on 2016-12-14.
 */

public class ExampleApplication extends Application {

    private static final String API_KEY = "d225967173e9d95550b8f2fca8e659bc5c0fbddc";

    @Override
    public void onCreate() {
        super.onCreate();
        SendAnywhere.init(this, API_KEY);
        SendAnywhere.Settings settings = SendAnywhere.getSettings(this);
        settings.setDownloadDir(new File(Environment.getExternalStorageDirectory(), "SendAnywhere"));
        settings.setDeviceToken(FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SendAnywhere.shutdown();
    }
}