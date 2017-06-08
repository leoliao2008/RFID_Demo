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
//                    try {
//                        inputStream.skip(4095);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    while (isReading){
                        try {
                            final int len = inputStream.read(temp);
                            if(len>0){
                                extractData(temp,len);
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
        mRFIDAckCallBack=null;
    }

    private void extractData(byte[] data, int len) {
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
                                //ack[1]表示ack后面字节的有效长度，2表示ack前面两个字节的长度，加起来就是整个ack的有效长度。
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


    private void decipherPackage(byte[] ack) {
        showLog("decipher ack:");
        StringBuilder sb=new StringBuilder();
        if(mRFIDAckCallBack!=null){
            AckBeanWithFixedLength fixedLenAck = new AckBeanWithFixedLength(ack);
            String info=fixedLenAck.getStatusDescription();
            boolean isSuccess=fixedLenAck.getStatusCode()==0;
            switch (PackageType.getTypeByByte(ack[2])){
                case TYPE_DEVICE_VERSION://设备版本号
                    // V (0X05).(0X37)
                    sb.append("Ver ").append(String.format("%d",ack[4])).append(".").append(String.format("%d",ack[5]));
                    mRFIDAckCallBack.onVersionNameGet(sb.toString());
                    break;
                case TYPE_DETECT_TAG://检测TAG
                    if(isLenNotFixed(ack)){//成功
                        for(int i=0;i<12;i++){
                            sb.append(String.format("%02X",ack[5+i]&0xff)).append(" ");
                        }
                        String antennaId=String.format("%02d",ack[4]);
                        String tagId=sb.toString().trim();
                        mRFIDAckCallBack.onTagDetected(true,antennaId,tagId,"天线号："+antennaId+" ，标签ID: "+tagId);
                    }else {//失败
                        mRFIDAckCallBack.onTagDetected(false,"获取失败","获取失败",fixedLenAck.getStatusDescription());
                    }
                    break;
                case TYPE_READ_TAG_DATA://读取标签一个字的数据
                    if(isLenNotFixed(ack)){//成功
                        //ack[7] ack[8]
                        sb.append("写入内容：").append(toHexString(ack[7])).append(toHexString(ack[8]));
                        mRFIDAckCallBack.onTagDataRead(true,ack[7],ack[8],sb.toString().trim());
                    }else {
                        //失败
                        mRFIDAckCallBack.onTagDataRead(false,(byte) 0,(byte) 0, info);
                    }
                    break;
                case TYPE_WRITE_TAG_DATA://写入一个字的数据到标签
                    mRFIDAckCallBack.onTagDataWritten(isSuccess,info);
                    break;
                case TYPE_LOCK_TAG:
                    mRFIDAckCallBack.onTagLock(isSuccess,info);
                    break;
                case TYPE_UNLOCK_TAG:
                    mRFIDAckCallBack.onTagUnlock(isSuccess,info);
                    break;
                case TYPE_KILL_TAG:
                    mRFIDAckCallBack.onKillTag(isSuccess,info);
                    break;
                case TYPE_RESET_TAG:
                    mRFIDAckCallBack.onResetTag(isSuccess,info);
                    break;
                case TYPE_RESET_DEVICE:
                    mRFIDAckCallBack.onResetDevice(isSuccess,info);
                    break;
                case TYPE_READING_STOP:
                    mRFIDAckCallBack.onStopReading(isSuccess,info);
                    break;
                case TYPE_READING_RESTART:
                    mRFIDAckCallBack.onRestartReading(isSuccess,info);
                    break;
                case TYPE_CONTROL_RELAY:
                    mRFIDAckCallBack.onControlRelay(isSuccess,info);
                    break;
                case TYPE_SET_BAUD_RATE:
                    if(isSuccess){
                        info=info+"，设备重启后将生效。";
                    }
                    mRFIDAckCallBack.onSetBaudRate(isSuccess,info);
                    break;
                case TYPE_NOT_FOUND://配对失败

                    break;
            }
        }
    }

    private boolean isLenNotFixed(byte[] ack){
        return (ack[0]&0xff)==0xE0;
    }



    private void showLog(String msg){
        Log.e(TAG,msg);
    }


}
