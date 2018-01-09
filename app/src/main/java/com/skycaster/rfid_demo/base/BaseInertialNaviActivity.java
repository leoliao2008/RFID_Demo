package com.skycaster.rfid_demo.base;

import android.content.SharedPreferences;

import com.skycaster.inertial_navi_lib.GPGGA.GPGGABean;
import com.skycaster.inertial_navi_lib.NaviDataExtractor;
import com.skycaster.inertial_navi_lib.NaviDataExtractorCallBack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

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
    protected AtomicBoolean isRunning=new AtomicBoolean(false);

    @Override
    protected void initData() {
        mSharedPreferences=getSharedPreferences("Config",MODE_PRIVATE);
        mSerialPortPath=mSharedPreferences.getString(PORT_PATH,"/dev/ttyAMA4");
        mSerialPortBdRate=mSharedPreferences.getInt(PORT_RATE,9600);
        initRegularData();
    }

    protected abstract void initRegularData();


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
        isRunning.compareAndSet(false,true);
        NaviDataExtractor.startExtractingGPGGAData(mInputStream,new NaviDataExtractorCallBack() {
            @Override
            public void onGetGPGGABean(GPGGABean bean) {
                onGPGGABeanGot(bean);
            }
        });

    }

    protected boolean stopExtracting(){
        isRunning.compareAndSet(true,false);
       return NaviDataExtractor.stopExtractingGPGGAData();
    }

    protected abstract void onGPGGABeanGot(GPGGABean bean);

    private void closeSerialPort(){
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
