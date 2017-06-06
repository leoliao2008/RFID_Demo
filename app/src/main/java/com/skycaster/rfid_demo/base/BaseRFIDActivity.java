package com.skycaster.rfid_demo.base;

import android.content.SharedPreferences;

import com.skycaster.rfid_lib.RFIDAckCallBack;
import com.skycaster.rfid_lib.RFIDAckReader;
import com.skycaster.rfid_lib.RFIDRequestSender;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import project.SerialPort.SerialPort;

/**
 * Created by 廖华凯 on 2017/5/26.
 */

public abstract class BaseRFIDActivity extends BaseActivity {

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private RFIDRequestSender mRFIDRequestSender;
    private RFIDAckReader mRFIDAckReader;
    private String mSerialPortPath;
    private int mSerialPortBdRate;
    private static final String PORT_PATH="current_serial_port_path";
    private static final String PORT_RATE="current_serial_port_baud_rate";
    private SharedPreferences mSharedPreferences;
    private AtomicBoolean isSerialPortOpen =new AtomicBoolean(false);


    @Override
    protected void initData() {
        mSharedPreferences=getSharedPreferences("Config",MODE_PRIVATE);
        mSerialPortPath=mSharedPreferences.getString(PORT_PATH,"/dev/ttyAMA4");
        mSerialPortBdRate=mSharedPreferences.getInt(PORT_RATE,9600);
//        isSerialPortOpen.compareAndSet(false,openSerialPort(mSerialPortPath,mSerialPortBdRate));
        initRFIDData();
    }


    public boolean openSerialPort(String path, int rate) {
        closeSerialPort();
        setSerialPortPath(path);
        setSerialPortBdRate(rate);
        if(mRFIDAckReader!=null){
            mRFIDAckReader.unRegisterAckReaderCallBack();
        }
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
            mInputStream=mSerialPort.getInputStream();
            mOutputStream=mSerialPort.getOutputStream();
            mRFIDRequestSender=RFIDRequestSender.getInstance(mOutputStream);
            mRFIDAckReader=RFIDAckReader.getInstance();
            mRFIDAckReader.registerAckReaderCallback(mInputStream,initRFIDAckCallBack());
            showHint("串口打开成功，当前串口路径："+path+",波特率："+rate+"。");
            return true;
        }else {
            showHint("串口打开失败：串口路径："+path+",波特率："+rate+"。");
            return false;
        }
    }

    protected abstract RFIDAckCallBack initRFIDAckCallBack();

    private void closeSerialPort(){
        if(mSerialPort!=null){
            try {
                mSerialPort.getInputStream().close();
                mSerialPort.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSerialPort.close();
        }
    }

    public RFIDRequestSender getRFIDRequestSender() throws NullPointerException {
        if(mRFIDRequestSender==null){
            showHint("串口还未打开，请检查串口权限。");
            throw new NullPointerException("串口还未打开，请检查串口权限。");
        }
        return mRFIDRequestSender;
    }

    protected abstract void showHint(String message);

    protected abstract void initRFIDData();

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
    protected void onDestroy() {
        super.onDestroy();
        if(mRFIDAckReader!=null){
            mRFIDAckReader.unRegisterAckReaderCallBack();
        }
        closeSerialPort();
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
