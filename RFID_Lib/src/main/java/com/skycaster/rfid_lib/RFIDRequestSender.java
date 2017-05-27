package com.skycaster.rfid_lib;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by 廖华凯 on 2017/5/26.
 */

public class RFIDRequestSender {
    private String TAG;
    private Handler mHandler;
    private static OutputStream mOutputStream;
    private static RFIDRequestSender mRFIDRequestSender=new RFIDRequestSender();

    private RFIDRequestSender() {
        if(Looper.myLooper()==null){
            Looper.prepare();
            Looper.loop();
        }
        TAG=getClass().getSimpleName();
        mHandler=new Handler();
    }

    public static synchronized RFIDRequestSender getInstance(OutputStream outputStream){
        mOutputStream=outputStream;
        return mRFIDRequestSender;
    }


    private synchronized void sendRequest(final byte[] data, final int len){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mOutputStream!=null){
                    try {
                        mOutputStream.write(data.clone(),0,len);
                        StringBuilder sb=new StringBuilder();
                        for(int i=0;i<len;i++){
                            sb.append("0x").append(String.format("%02X",data[i])).append(" ");
                        }
                        showLog(sb.toString().trim());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private synchronized void sendRequest(RequestBean bean){
        sendRequest(bean.toBytes(),bean.len());
    }

    public synchronized void requestVersionName(){
        sendRequest(new RequestBean(PackageType.TYPE_DEVICE_VERSION));
    }

    public synchronized void detectTag(){
        sendRequest(new RequestBean(PackageType.TYPE_DETECT_TAG));
    }

    public synchronized void readTag(){
        byte[] params=new byte[]{0x01,0x02,0x01};
        sendRequest(new RequestBean(PackageType.TYPE_READ_TAG,params));
    }


    private void showLog(String msg){
        Log.e(TAG,msg);
    }



}
