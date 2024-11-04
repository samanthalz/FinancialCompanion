package com.example.financialcompanion;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Set Firebase Database persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}