package com.skycaster.rfid_demo.base;

import android.app.Application;
import android.os.Handler;

/**
 * Created by 廖华凯 on 2017/5/23.
 */

public class BaseApplication extends Application {
    private static Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler=new Handler();
    }

    public static void post(Runnable runnable){
        handler.post(runnable);
    }

    public static void postDelayed(Runnable runnable,long millis){
        handler.postDelayed(runnable,millis);
    }

    public static void removeCallback(Runnable runnable){
        handler.removeCallbacks(runnable);
    }
}
