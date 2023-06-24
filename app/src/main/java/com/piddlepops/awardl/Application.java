package com.piddlepops.awardl;

import com.facebook.ads.AudienceNetworkAds;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AudienceNetworkAds.initialize(getApplicationContext());
    }
}
