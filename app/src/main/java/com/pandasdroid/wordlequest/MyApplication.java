package com.pandasdroid.wordlequest;

import android.app.Application;
import android.util.Log;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MyApplication extends Application {
    public static boolean isAdmobInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        AudienceNetworkAds.initialize(getApplicationContext());
        MobileAds.initialize(this, initializationStatus -> {
            isAdmobInitialized = true;
        });
    }
}
