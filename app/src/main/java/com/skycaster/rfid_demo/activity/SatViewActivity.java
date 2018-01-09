package com.skycaster.rfid_demo.activity;

import android.location.Location;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.TextView;

import com.skycaster.inertial_navi_lib.GPGGA.TbGNGGABean;
import com.skycaster.inertial_navi_lib.GPGSA.GPGSABean;
import com.skycaster.inertial_navi_lib.GPGSA.GPGSAType;
import com.skycaster.inertial_navi_lib.GPGSV.GPGSVBean;
import com.skycaster.inertial_navi_lib.NaviDataExtractor;
import com.skycaster.inertial_navi_lib.NaviDataExtractorCallBack;
import com.skycaster.rfid_demo.R;
import com.skycaster.rfid_demo.base.BaseActivity;
import com.skycaster.rfid_demo.widgets.SatelliteMapView;

import java.io.File;
import java.io.InputStream;

import project.SerialPort.SerialPort;

public class SatViewActivity extends BaseActivity {

    private SatelliteMapView mSatView;
    private TextView mTvFixStatus;
    private TextView mTvSatCount;
    private TextView mTvLat;
    private TextView mTvLng;
    private TextView mTvAlt;
//    private String[] ss={
//            "$GPGSV,3,1,10,20,78,331,45,01,59,235,47,22,41,069,,13,32,252,45*70",
//            "$GPGSV,3,2,10,20,78,331,45,01,59,235,47,22,41,069,,13,32,252,45*70",
//            "$GPGSV,3,3,10,20,78,331,45,01,59,235,47,22,41,069,,13,32,252,45*70"
//    };
    private NaviDataExtractorCallBack mCallBack;
    private Handler mHandler;
//    private int i=0;
//    private Runnable mRunnable=new Runnable() {
//        @Override
//        public void run() {
//            GPGSVBean.getInstance().decipher(ss[i++],mCallBack);
//            i=i%ss.length;
//            mHandler.postDelayed(this,1000);
//        }
//    };
    private SerialPort mSerialPort;
    private byte[] mTemp;
    private InputStream mInputStream;
    private int len;
    private String mFixStatus;
    private int mSatCount;
    private Location mLocation;


    @Override
    protected int attachLayout() {
        return R.layout.activity_sat_view;
    }

    @Override
    protected void initViews() {
        mSatView= (SatelliteMapView) findViewById(R.id.main_sat_view);
        mTvFixStatus= (TextView) findViewById(R.id.main_tv_fix_status);
        mTvSatCount= (TextView) findViewById(R.id.main_tv_sat_count);
        mTvLat= (TextView) findViewById(R.id.main_tv_lat);
        mTvLng= (TextView) findViewById(R.id.main_tv_lng);
        mTvAlt= (TextView) findViewById(R.id.main_tv_alt);

    }

    @Override
    protected void initData() {
        mHandler=new Handler();
        mCallBack=new NaviDataExtractorCallBack(){
            @Override
            public void onGetTBGNGGABean(TbGNGGABean bean) {
                mLocation = bean.getLocation();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTvLat.setText("北纬："+mLocation.getLatitude());
                        mTvLng.setText("东经："+mLocation.getLongitude());
                        mTvAlt.setText("海拔："+mLocation.getAltitude());
                    }
                });
            }

            @Override
            public void onGetGPGSVBean(GPGSVBean bean) {
                mSatView.updateSatellites(bean);
                mSatCount = bean.getSatCount();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTvSatCount.setText("卫星数："+mSatCount);
                    }
                });
            }

            @Override
            public void onGetGPGSABean(GPGSABean bean) {
                GPGSAType type = bean.getType();
                if(type==GPGSAType.UNFIX){
                    mFixStatus ="UNFIX";
                }else if(type==GPGSAType.FIX3D){
                    mFixStatus ="FIX3D";
                }else {
                    mFixStatus ="FIX2D";
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTvFixStatus.setText(mFixStatus);
                    }
                });
                showLog(bean.toString());
            }
        };
        try {
            mSerialPort=new SerialPort(new File("/dev/ttyAMA1"),115200,0);
            mInputStream = mSerialPort.getInputStream();
            mTemp = new byte[1024];
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while ((len=mInputStream.read(mTemp))>0){
                            NaviDataExtractor.decipherData(mTemp,len,mCallBack);
                        }
                    } catch (Exception e) {
                        handleException(e);
                    }
                }
            }).start();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        String msg = e.getMessage();
        showLog(TextUtils.isEmpty(msg)?"Error not defined.":msg);
    }

    @Override
    protected void initListeners() {

    }

    @Override
    protected void onStart() {
        super.onStart();
//        mHandler.post(mRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSerialPort!=null){
            mSerialPort.close();
        }
    }
}
