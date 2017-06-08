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
    private static int DATA_LEN=512;
    private static byte[] temp=new byte[DATA_LEN];
    private static byte[] GPGGAData =new byte[DATA_LEN];
    private static AtomicBoolean isExtractingGPGGAData =new AtomicBoolean(false);
    private static boolean isGPGGAHeadConfirmed;
    private static int index;
    private NaviDataExtractor(){}


    public static synchronized void startExtractingGPGGAData(final InputStream inputStream, final CallBack callBack){
        isExtractingGPGGAData.compareAndSet(false,true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    inputStream.skip(9999);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (isExtractingGPGGAData.get()){
                    try {
//                        showLog("InputStream Available:"+inputStream.available());
                        int len = inputStream.read(temp);
//                        showLog("len="+len);
                        // $GPGGA,235949.042,0000.0000,N,00000.0000,E,0,00,,0.0,M,0.0,M,,0000*45
                        if(len<1){
                            continue;
                        }
                        for(int i=0;i<len;i++){
                            if(!isGPGGAHeadConfirmed){
//                                showLog("Ack Head not confirmed.");
                                if(temp[i]=='$'){
                                    isGPGGAHeadConfirmed =true;
//                                    showLog("Ack Head is confirmed!");
                                    index=0;
                                    GPGGAData[index++]=temp[i];
                                }else {
//                                    char c = (char) temp[i];
//                                    showLog(String.valueOf(c)+"is forfeit.");
                                }
                            }else {
                                if(temp[i]=='$'){
//                                    showLog("tail is reached.");
                                    byte[] clone = GPGGAData.clone();
//                                    showHint(toHexString(clone,index));
                                    String source = new String(clone, 0, index);
//                                    showLog("GPS source: "+source);
//                                        onGpsBeanGot(new GPGGABean(source));
                                    if(isSourceValid(source)){
//                                        showLog("source GPGGAData is valid.");
                                        callBack.onGetGPGGABean(new GPGGABean(source));
                                    }else {
                                        showLog("GPGGA data is not valid. Data is abandon.");
                                    }
                                    index=0;
                                }
                                GPGGAData[index++]=temp[i];
                                if(index==DATA_LEN-1){
                                    index=0;
                                    showLog("data len exceed boundary, reset index to 0");
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

    private static synchronized boolean isSourceValid(String source) {
//        showLog("begin to check if source valid...");
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
//                showLog("check sum="+Integer.toHexString(checkSum)+" vs source ="+data[1]);
                return Integer.toHexString(checkSum).equalsIgnoreCase(data[1]);
            }
        }
        return false;
    }

    private static void showLog(String msg) {
        Log.e("NaviDataExtractor",msg);
    }


    public static synchronized boolean stopExtractingGPGGAData(){
        return isExtractingGPGGAData.compareAndSet(true,false);
    }


    public interface CallBack{
        void onGetGPGGABean(GPGGABean bean);
    }
}
