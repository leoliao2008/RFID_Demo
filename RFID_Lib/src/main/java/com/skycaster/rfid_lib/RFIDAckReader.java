package com.skycaster.rfid_lib;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by 廖华凯 on 2017/5/26.
 */

public class RFIDAckReader {
    private static RFIDAckReader mRFIDAckReader=new RFIDAckReader();
    private boolean isReading;
    private static final int DATA_LEN=512;
    private byte[] temp=new byte[DATA_LEN];
    private byte[] ack=new byte[DATA_LEN];
    private boolean isAckHeadMarked =false;
    private int ackIndex;
    private int packageLen;
    private boolean isPackageLenConfirmed;
    private Handler mHandler;
    private RFIDAckCallBack mRFIDAckCallBack;
    private String TAG;

    private RFIDAckReader() {
        if(Looper.getMainLooper()==null){
            Looper.prepare();
            Looper.loop();
        }
        mHandler=new Handler();
        TAG=getClass().getSimpleName();
    }

    public static synchronized RFIDAckReader getInstance(){
        return mRFIDAckReader;
    }

    public synchronized void registerAckReaderCallback(final InputStream inputStream, RFIDAckCallBack callBack){
        if(!isReading){
            isReading=true;
            mRFIDAckCallBack=callBack;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isReading){
                        try {
                            final int len = inputStream.read(temp);
                            if(len>0){
                                acquirePackage(temp,len);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //这里不需要关闭 inputStream,留给调用方清理。
                }
            }).start();
        }
    }

    public synchronized void unRegisterAckReaderCallBack(){
        isReading=false;
    }

    private void acquirePackage(byte[] data, int len) {
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<len;i++){
            sb.append(toHexString(data[i]));
            if(!isAckHeadMarked){
                showLog("this byte = "+toHexString(data[i]));
                if((data[i]&0xff)==0xE0||(data[i]&0xff)==0xE4){
                    isAckHeadMarked=true;
                    isPackageLenConfirmed =false;
                    ackIndex=0;
                    ack[ackIndex++]=data[i];
                    showLog("ack head is marked.");
                }else {
                    showLog("ack head is not marked and this byte is forfeit");
                }
            }else {
                if(!isPackageLenConfirmed){
                    packageLen=data[i];
                    ack[ackIndex++]=data[i];
                    isPackageLenConfirmed=true;
                    showLog("ack len is confirmed. len= "+packageLen);
                }else {
                    if(packageLen>1){
                        --packageLen;
                        ack[ackIndex++]=data[i];
                        showLog("fill this byte into container...");
                    }else if(packageLen==1){
                        ack[ackIndex]=data[i];
                        showLog("reach len, begin to test if ack validate...");
                        isAckHeadMarked=false;
                        isPackageLenConfirmed =false;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //ack[1]表示ack后面的长度，2表示ack前面两个字节的长度，加起来就是整个ack的长度。
                                byte[] result = Arrays.copyOf(ack, ack[1] + 2);
                                if(checkIfValidate(result)){
                                    showLog("ack is validate");
                                    decipherPackage(result);
                                }else {
                                    showLog("ack is not validate");
                                }
                            }
                        });
                    }
                }
            }
        }
        showLog(sb.toString().trim());
    }

    private String toHexString(byte b){
        return "0x"+String.format("%02X",b)+" ";
    }

    private boolean checkIfValidate(byte[] result) {
        byte b=0x00;
        int len=result.length;
        for(int i=0;i<len-1;i++){
            b+=result[i];
        }
        b= (byte) ((~b+1)&0xff);
        return b==result[len-1];
    }


    private void decipherPackage(byte[] data) {
        showLog("decipher ack:");
        StringBuilder sb=new StringBuilder();
        switch (PackageType.getTypeByByte(data[2])){
            case TYPE_DEVICE_VERSION://设备版本号
                // V (0X05).(0X37)
                sb.append("Ver ").append(String.format("%d",data[4])).append(".").append(String.format("%d",data[5]));
                mRFIDAckCallBack.onVersionNameGet(sb.toString());
                break;
            case TYPE_DETECT_TAG://检测TAG
                if((data[0]&0xff)==0xE0){//成功
                    for(int i=0;i<12;i++){
                        sb.append(String.format("%02X",data[5+i]&0xff)).append(" ");
                    }
                    String antennaId=String.format("%02d",data[4]);
                    String tagId=sb.toString().trim();
                    mRFIDAckCallBack.onTagDetected(true,antennaId,tagId,"天线号："+antennaId+" ，标签ID: "+tagId);
                }else {//失败
                    AckBeanWithFixedLength bean = new AckBeanWithFixedLength(data);
                    mRFIDAckCallBack.onTagDetected(false,"获取失败","获取失败",bean.getStatusDescription());
                }
                break;
            case TYPE_NOT_FOUND://配对失败

                break;
        }
    }



    private void showLog(String msg){
        Log.e(TAG,msg);
    }


}
