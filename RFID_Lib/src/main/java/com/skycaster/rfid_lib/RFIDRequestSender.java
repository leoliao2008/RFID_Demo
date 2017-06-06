package com.skycaster.rfid_lib;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
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
    private int mCurrentDdRateIndex;
    private AlertDialog mAlertDialog;

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

    /**
     * 获取设备系统版本号
     */
    public synchronized void requestVersionName(){
        sendRequest(new RequestBean(PackageType.TYPE_DEVICE_VERSION));
    }

    /**
     * 识别标签
     */
    public synchronized void detectTag(){
        sendRequest(new RequestBean(PackageType.TYPE_DETECT_TAG));
    }

    /**
     * 读取标签单个字的数据
     */
    public synchronized void readTagData(){
        byte[] params=new byte[]{0x01,0x02,0x01};//内存地址类型为0x01 epc，地址为从0x02开始，0x01为一个字的数据（数据长度2个字节）
        sendRequest(new RequestBean(PackageType.TYPE_READ_TAG_DATA,params));
    }

    /**
     * 在标签中写入单个字的数据（长度为两个字节）
     * @param data1 数据的第一个字节
     * @param data2 数据的第二个字节
     */
    public synchronized void writeTagData(byte data1,byte data2){
        byte[] params=new byte[]{0x00,0x01,0x02,0x01,data1,data2};//写入模式0x00 单个字写入模式，0x01 内存地址类型epc,从地址0x02开始，0x01为一个字的数据，写入数据为data1，data2
        sendRequest(new RequestBean(PackageType.TYPE_WRITE_TAG_DATA,params));
    }

    /**
     * 锁定标签的EPC区
     */
    public synchronized void lockTag(){
        byte[] params=new byte[]{0x12,0x34,0x56,0x78,0x02};//密码12345678，0x02锁定EPC区
        sendRequest(new RequestBean(PackageType.TYPE_LOCK_TAG,params));
    }

    /**
     * 解锁标签的EPC区
     */
    public synchronized void unlockTag(){
        byte[] params=new byte[]{0x12,0x34,0x56,0x78,0x02};//密码12345678，0x02解锁EPC区
        sendRequest(new RequestBean(PackageType.TYPE_UNLOCK_TAG,params));
    }

    /**
     * 杀死标签？
     */
    public synchronized void killTag(){
        byte[] params=new byte[]{0x00,0x12,0x34,0x56,0x78};
        sendRequest(new RequestBean(PackageType.TYPE_KILL_TAG,params));
    }

    /**
     * 初始化标签
     */
    public synchronized void resetTag(){
        sendRequest(new RequestBean(PackageType.TYPE_RESET_TAG));
    }

    /**
     * 设备复位
     */
    public synchronized void resetDevice(){
        sendRequest(new RequestBean(PackageType.TYPE_RESET_DEVICE));
    }

    /**
     * 停止读取标签
     */
    public synchronized void stopReadingTag(){
        sendRequest(new RequestBean(PackageType.TYPE_READING_STOP));
    }

    /**
     * 重新开始读取标签
     */
    public synchronized void restartReadingTag(){
        sendRequest(new RequestBean(PackageType.TYPE_READING_RESTART));
    }

    /**
     * 控制续电器
     * @param isToActivate true为打开，false为关闭。
     */
    public synchronized void activateDelay(boolean isToActivate){
        byte[] params=new byte[1];
        params[0]= (byte) (isToActivate?1:0);
        sendRequest(new RequestBean(PackageType.TYPE_CONTROL_RELAY,params));
    }

    /**
     * 跳出一个对话窗口，设置设备波特率，执行成功后需重启设备。
     * @param context 上下文环境，启动对话框时需用到。
     * @param currentBdRate 当前设备的波特率
     */
    public synchronized void setBaudRate(Context context,String currentBdRate){
        final byte[] bdRateCodes=new byte[]{0x00,0x01,0x02,0x03,0x04};
        String[] bdRates=new String[]{"9600","19200","38400","57600","115200"};
        mCurrentDdRateIndex = 0;
        for(int i=0;i<bdRates.length;i++){
            if(currentBdRate.equals(bdRates[i])){
                mCurrentDdRateIndex =i;
                break;
            }
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        mAlertDialog = builder.setTitle("设置波特率").setSingleChoiceItems(bdRates, mCurrentDdRateIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCurrentDdRateIndex = which;
            }
        }).setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                byte[] params=new byte[1];
                params[0]=bdRateCodes[mCurrentDdRateIndex];
                sendRequest(new RequestBean(PackageType.TYPE_SET_BAUD_RATE,params));
                mAlertDialog.dismiss();

            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlertDialog.dismiss();
            }
        }).create();
        mAlertDialog.show();
    }

    /**
     * 设置设备波特率
     * @param baudRate 波特率
     */
    public synchronized void setBaudRate(BaudRate baudRate){
        sendRequest(new RequestBean(PackageType.TYPE_SET_BAUD_RATE,new byte[]{baudRate.toByte()}));
    }


    private void showLog(String msg){
        Log.e(TAG,msg);
    }



}
