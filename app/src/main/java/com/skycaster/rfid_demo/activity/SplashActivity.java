package com.skycaster.rfid_demo.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.skycaster.rfid_demo.R;
import com.skycaster.rfid_demo.base.BaseActivity;
import com.skycaster.rfid_demo.base.BaseApplication;
import com.skycaster.rfid_demo.data.Constants;
import com.skycaster.rfid_demo.utils.AlertDialogUtil;
import com.skycaster.rfid_demo.utils.RFIDUtil;

public class SplashActivity extends BaseActivity {

    private static final int REQUEST_SYS_PERMISSIONS = 843;

    @Override
    protected int attachLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initViews() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListeners() {

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(isPermissionsGranted()){
            toMainActivity();
        }else {
            requestPermissions();
        }
    }

    private boolean isPermissionsGranted(){
        boolean isGranted=true;
        for(String p: Constants.SYSTEM_PERMISSIONS){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(PackageManager.PERMISSION_GRANTED!=checkSelfPermission(p)){
                    isGranted=false;
                    break;
                }
            }
        }
        return isGranted;
    }

    private void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(Constants.SYSTEM_PERMISSIONS,REQUEST_SYS_PERMISSIONS);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int len=permissions.length;
        boolean isGranted=true;
        for(int i=0;i<len;i++){
            if(PackageManager.PERMISSION_GRANTED!=grantResults[i]){
                isGranted=false;
                StringBuilder sb=new StringBuilder();
                sb.append("为了安装RFID底层库，需要获取以下系统权限，否则将有可能无法运行本程序：\n");
                for(String s:permissions){
                    sb.append(s).append("\n");
                }
                if(shouldShowRequestPermissionRationale(permissions[i])){
                    sb.append("点击确认将重新申请权限，点击取消将退出本程序。");
                    AlertDialogUtil.showAlertDialog(
                            SplashActivity.this,
                            sb.toString(),
                            new Runnable() {
                                @Override
                                public void run() {
                                    requestPermissions();
                                }
                            }, new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            });
                }else {
                    sb.append("您已经永久禁用了本程序使用以上权限，请先到系统设置-应用管理中给本程序设置以上权限后再运行本程序。");
                    AlertDialogUtil.showAlertDialog(
                            SplashActivity.this,
                            sb.toString(),
                            new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            },
                            null
                    );
                }
                break;
            }
        }
        if(isGranted){
//            checkIfDllInstall();
            toMainActivity();
        }
    }

    private boolean checkIfDllInstall() {
        boolean isInstalled;
        isInstalled=RFIDUtil.checkIfDllInstalled(this);
        if(isInstalled){
            toMainActivity();
        }else {
            if(RFIDUtil.installDll(this)){
                toMainActivity();
            }else {
                AlertDialogUtil.showAlertDialog(this, "写入Dll库失败，无法运行本程序。请检查系统储存空间是否足够。", new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },null);
            }
        }
        return isInstalled;
    }

    private void toMainActivity(){
        BaseApplication.postDelayed(new Runnable() {
            @Override
            public void run() {
//                startActivity(new Intent(SplashActivity.this,RFIDActivity.class));
                startActivity(new Intent(SplashActivity.this,SatViewActivity.class));
                finish();
            }
        },500);
    }


}
