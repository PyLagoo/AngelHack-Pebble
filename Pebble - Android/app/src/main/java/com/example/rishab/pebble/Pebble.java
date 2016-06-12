package com.example.rishab.pebble;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by naman_000 on 12-06-16.
 */
public class Pebble extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }

}
