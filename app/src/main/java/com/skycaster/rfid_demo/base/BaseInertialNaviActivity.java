package com.skycaster.rfid_demo.base;

import android.content.SharedPreferences;

import com.skycaster.inertial_navi_lib.GPGGABean;
import com.skycaster.inertial_navi_lib.NaviDataExtractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import project.SerialPort.SerialPort;

/**
 * Created by 廖华凯 on 2017/5/26.
 */

public abstract class BaseInertialNaviActivity extends BaseActivity {

    private SerialPort mSerialPort;
    private InputStream mInputStream;
    private String mSerialPortPath;
    private int mSerialPortBdRate;
    private static final String PORT_PATH="current_serial_port_path";
    private static final String PORT_RATE="current_serial_port_baud_rate";
    private SharedPreferences mSharedPreferences;
    private AtomicBoolean isSerialPortOpen =new AtomicBoolean(false);
    private NaviDataExtractor mNaviDataExtractor;
    private static int DATA_LEN=512;
    private byte[] temp=new byte[DATA_LEN];
    private byte[] data=new byte[DATA_LEN];
    protected AtomicBoolean isRunning=new AtomicBoolean(false);
    private boolean isHeadConfirmed;
    private int index;



    @Override
    protected void initData() {
        mSharedPreferences=getSharedPreferences("Config",MODE_PRIVATE);
        mSerialPortPath=mSharedPreferences.getString(PORT_PATH,"/dev/ttyAMA4");
        mSerialPortBdRate=mSharedPreferences.getInt(PORT_RATE,9600);
        mNaviDataExtractor=NaviDataExtractor.getInstance();
        initInertialData();
    }

    protected abstract void initInertialData();


    public boolean openSerialPort(String path, int rate) {
        closeSerialPort();
        setSerialPortPath(path);
        setSerialPortBdRate(rate);
        try {
            mSerialPort = new SerialPort(new File(path),rate,0);
        } catch (IOException e) {
            if(e.getMessage()!=null){
                showHint(e.getMessage());
            }
        } catch (SecurityException e){
            showHint("串口权限受限。");
        }
        if(mSerialPort!=null){
            if(isSerialPortOpen.compareAndSet(false,true)){
                mInputStream=mSerialPort.getInputStream();
                showHint("串口打开成功，当前串口路径："+path+",波特率："+rate+"。");
                return true;
            }
        }else {
            showHint("串口打开失败：串口路径："+path+",波特率："+rate+"。");
            return false;
        }
        return false;
    }

    protected void startExtracting() {
//        isRunning.compareAndSet(false,true);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (isRunning.get()){
//                    try {
//                        int len = mInputStream.read(temp);
////                        showLog("len="+len);
//                        // $GPGGA,235949.042,0000.0000,N,00000.0000,E,0,00,,0.0,M,0.0,M,,0000*45
//                        for(int i=0;i<len;i++){
//                            if(!isHeadConfirmed){
////                                showLog("Ack Head not confirmed.");
//                                if(temp[i]=='$'){
//                                    isHeadConfirmed=true;
////                                    showLog("Ack Head is confirmed!");
//                                    index=0;
//                                    data[index++]=temp[i];
//                                }else {
////                                    char c = (char) temp[i];
////                                    showLog(String.valueOf(c)+"is forfeit.");
//                                }
//                            }else {
//                                if(temp[i]=='$'){
////                                    showLog("tail is reached.");
//                                    byte[] clone = data.clone();
////                                    showHint(toHexString(clone,index));
//                                    String source = new String(clone, 0, index);
////                                    showLog("GPS source: "+source);
////                                        onGPGGABeanGot(new GPGGABean(source));
//                                    if(isSourceValid(source)){
////                                        showLog("source data is valid.");
//                                        onGPGGABeanGot(new GPGGABean(source));
//                                    }else {
//                                        showLog("source data is not valid.");
//                                    }
//                                    index=0;
//                                }
//                                data[index++]=temp[i];
//                                if(index==DATA_LEN-1){
//                                    index=0;
//                                    showLog("reset index to 0");
//                                }
//                            }
//                        }
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();

        mNaviDataExtractor.startExtracting(mInputStream,new NaviDataExtractor.CallBack() {
            @Override
            public void onGetGPGGABean(GPGGABean bean) {
                onGPGGABeanGot(bean);
            }
        });

    }

    protected boolean stopExtracting(){
//        return isRunning.compareAndSet(true,false);
       return mNaviDataExtractor.stopExtracting();
    }

    private String toHexString(byte[] bytes,int len){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<len;i++){
            sb.append("0x").append(String.format("%02X",bytes[i])).append(" ");
        }
        return sb.toString().trim();
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

    protected abstract void onGetData(byte[] data, int len);

    protected abstract void onGPGGABeanGot(GPGGABean bean);

    private void closeSerialPort(){
        isRunning.compareAndSet(true,false);
        stopExtracting();
        if(mSerialPort!=null){
            isSerialPortOpen.compareAndSet(true,false);
            try {
                mSerialPort.getInputStream().close();
                mSerialPort.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSerialPort.close();
        }
    }

    protected abstract void showHint(String message);


    public String getSerialPortPath() {
        return mSerialPortPath;
    }

    private void setSerialPortPath(String serialPortPath) {
        mSerialPortPath = serialPortPath;
        mSharedPreferences.edit().putString(PORT_PATH,serialPortPath).apply();
    }

    public int getSerialPortBdRate() {
        return mSerialPortBdRate;
    }

    private void setSerialPortBdRate(int serialPortBdRate) {
        mSerialPortBdRate = serialPortBdRate;
        mSharedPreferences.edit().putInt(PORT_RATE,serialPortBdRate).apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeSerialPort();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isSerialPortOpen.compareAndSet(false,openSerialPort(mSerialPortPath,mSerialPortBdRate));
    }
}
