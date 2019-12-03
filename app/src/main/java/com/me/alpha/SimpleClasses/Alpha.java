package com.me.alpha.SimpleClasses;

import android.app.Application;

import com.google.firebase.FirebaseApp;


public class Alpha extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

    }

}
