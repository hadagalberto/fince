package com.hadagalberto.fince;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("yDoVrAoTOfkIVipv3LkPaetYgGlPHtBmEDLcdHxx")
                .clientKey("ZFBgdJCxkQGc9nSK1AiPb0o0CqVobDpEAzMT9gbq")
                .server("https://parseapi.back4app.com/")
                .enableLocalDataStore()
                .build()
        );
    }
}
