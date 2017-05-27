package com.skycaster.rfid_demo.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

/**
 * Created by 廖华凯 on 2017/5/23.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private String TAG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG=getClass().getSimpleName();
        setContentView(attachLayout());
        initViews();
        initData();
        initListeners();
    }

    protected abstract int attachLayout();

    protected abstract void initViews();

    protected abstract void initData();

    protected abstract void initListeners();

    protected void showLog(String msg){
        Log.e(TAG,msg);
    }

    protected void onClick(int viewId, View.OnClickListener listener){
        findViewById(viewId).setOnClickListener(listener);
    }
}
