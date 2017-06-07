package com.skycaster.inertial_navi_lib;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Created by 廖华凯 on 2017/6/7.
 */

public class NaviDataExtractor {
    private static NaviDataExtractor mExtractor=new NaviDataExtractor();
    public static NaviDataExtractor getInstance(){
        return mExtractor;
    }
    private static int DATA_LEN=512;
    private byte[] temp=new byte[DATA_LEN];
    private byte[] data=new byte[DATA_LEN];
    private AtomicBoolean isRunning=new AtomicBoolean(false);
    private boolean isHeadConfirmed;
    private int index;
    private NaviDataExtractor(){}

    public synchronized void startExtracting(final InputStream inputStream, final CallBack callBack){
        isRunning.compareAndSet(false,true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning.get()){
                    try {
                        int len = inputStream.read(temp);
                        showLog("len="+len);
                        // $GPGGA,235949.042,0000.0000,N,00000.0000,E,0,00,,0.0,M,0.0,M,,0000*45
                        for(int i=0;i<len;i++){
                            if(!isHeadConfirmed){
//                                showLog("Ack Head not confirmed.");
                                if(temp[i]=='$'){
                                    isHeadConfirmed=true;
//                                    showLog("Ack Head is confirmed!");
                                    index=0;
                                    data[index++]=temp[i];
                                }else {
//                                    char c = (char) temp[i];
//                                    showLog(String.valueOf(c)+"is forfeit.");
                                }
                            }else {
                                if(temp[i]=='$'){
//                                    showLog("tail is reached.");
                                    byte[] clone = data.clone();
//                                    showHint(toHexString(clone,index));
                                    String source = new String(clone, 0, index);
//                                    showLog("GPS source: "+source);
//                                        onGpsBeanGot(new GPGGABean(source));
                                    if(isSourceValid(source)){
//                                        showLog("source data is valid.");
                                        callBack.onGetGPGGABean(new GPGGABean(source));
                                    }else {
                                        showLog("source data is not valid.");
                                    }
                                    index=0;
                                }
                                data[index++]=temp[i];
                                if(index==DATA_LEN-1){
                                    index=0;
                                    showLog("reset index to 0");
                                }
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private boolean isSourceValid(String source) {
        showLog("begin to check if source valid...");
        int checkSum=0;
        String[] data = source.split(Pattern.quote("*"));
        if(data.length==2){
            String[] dataCheck = data[0].split(Pattern.quote("$"));
//            for(String s:dataCheck){
//                showLog("dataCheck: "+s);
//            }
            if(dataCheck.length==2){
                char[] chars = dataCheck[1].toCharArray();
                for(char c:chars){
                    checkSum^=c;
                }
                showLog("check sum="+Integer.toHexString(checkSum)+" vs source ="+data[1]);
                return Integer.toHexString(checkSum).equals(data[1]);
            }
        }
        return false;
    }

    private void showLog(String msg) {
        Log.e(getClass().getSimpleName(),msg);
    }


    public boolean stopExtracting(){
        return isRunning.compareAndSet(true,false);
    }


    public interface CallBack{
        void onGetGPGGABean(GPGGABean bean);
    }
}
