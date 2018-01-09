package com.skycaster.inertial_navi_lib;

import android.util.Log;

import com.skycaster.inertial_navi_lib.GPGGA.GPGGABean;
import com.skycaster.inertial_navi_lib.GPGGA.TbGNGGABean;
import com.skycaster.inertial_navi_lib.GPGSA.GPGSABean;
import com.skycaster.inertial_navi_lib.GPGSV.GPGSVBean;

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


    public static synchronized void startExtractingGPGGAData(final InputStream inputStream, final NaviDataExtractorCallBack callBack){
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
                       decipherData(temp,len,callBack);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static synchronized void decipherData(byte[] temp, int len, final NaviDataExtractorCallBack callBack){
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
//                    showLog("Source Get: "+source);
                    if(source.contains("GNGGA")){//新增专门解析天宝北斗模块的逻辑
                        //天宝的比较特殊，不用校验了，直接使用
                        callBack.onGetTBGNGGABean(new TbGNGGABean(source));
                    }else if(source.contains("GPGGA")){
                        if(isSourceValid(source)){//先校验一下
                            callBack.onGetGPGGABean(new GPGGABean(source));
                        }else {
                            showLog("GPGGA data is not valid. Data is abandon.");
                        }
                    }else if(source.contains("GPGSV")){
                        if(isSourceValid(source)){//先校验一下
                            GPGSVBean.getInstance().decipher(source,callBack);
                        }else {
                            showLog("GPGSV data is not valid. Data is abandon.");
                        }
                    }else if(source.contains("GPGSA")||source.contains("GNGSA")){
//                        if(isSourceValid(source)){//先校验一下
//                            callBack.onGetGPGSABean(new GPGSABean(source));
//                        }else {
//                            showLog("GPGSA data is not valid. Data is abandon.");
//                        }
                        //不用校验了，直接使用
                        callBack.onGetGPGSABean(new GPGSABean(source));
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


    }

    private static synchronized boolean isSourceValid(String source) {
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
                showLog("check sum="+String.format("%X",checkSum)+" vs source ="+data[1].trim());
                return Integer.toHexString(checkSum).equalsIgnoreCase(data[1].trim());
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



}
