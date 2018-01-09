package com.skycaster.inertial_navi_lib.GPGGA;

import android.location.Location;
import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * Created by 廖华凯 on 2017/10/27.
 */

public class TbGNGGABean {
    // $GNGGA,235949.042,0000.0000,N,00000.0000,E,0,00, .0.0 .M.0.0 .M. .0000*45
    // $GPGGA,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>.<9>.<10>.<11>.<12>.<13>.<14>*<15><CR><LF>
    //<1> UTC时间，格式为hhmmss.sss。
    //<2> 纬度，格式为ddmm.mmmm（前导位数不足则补0）。
    //<3> 纬度半球，N或S（北纬或南纬）。
    //<4> 经度，格式为dddmm.mmmm（前导位数不足则补0）。
    //<5> 经度半球，E或W（东经或西经）。
    //<6> 定位质量指示，0=定位无效，1=定位有效。
    //<7> 使用卫星数量，从00到12（前导位数不足则补0）。
    //<8> 后面的不用管啦
    private String mRawString;
    private Location mLocation;
    private long mTimeMillis;
    private String mLatitudeDirection;
    private String mLongitudeDirection;
    private FixQuality mFixQuality;
    private int mSatelliteCount;
    private boolean isValid;//表示是否符合有效格式

    public TbGNGGABean(String rawString) {
        mRawString = rawString;
        String[] data = mRawString.split(Pattern.quote(","));
        mLocation=new Location("");
        if(data.length>=9){
            isValid=true;
            //<1> UTC时间
            try {
                double s = Double.parseDouble(data[1]);
                short h= (short) (s/10000);
                short m= (short) (s/100-h*100);
                double milliSec=(s-h*10000-m*100)*1000;
                milliSec=Math.ceil(milliSec);
                mTimeMillis = (long) (h*3600*1000+m*60*1000+milliSec);
            }catch (NumberFormatException e){
                mTimeMillis=0;
            }
            mLocation.setTime(mTimeMillis);
            //<2> 纬度，格式为ddmm.mmmm（前导位数不足则补0）。
            try {
                double lat = Double.parseDouble(data[2]);
                int lat_h= (int) (lat/100);
                double lat_m=lat-lat_h*100;
                mLocation.setLatitude(lat_h+lat_m/60);
            }catch (NumberFormatException e){
                mLocation.setLatitude(0);
            }
            if(!TextUtils.isEmpty(data[3])){
                mLatitudeDirection =data[3];
            }else {
                mLatitudeDirection ="N";
            }

            //<4> 经度，格式为dddmm.mmmm（前导位数不足则补0）。
            try {
                double lng = Double.parseDouble(data[4]);
                int lng_h= (int) (lng/100);
                double lng_m=lng-lng_h*100;
                mLocation.setLongitude(lng_h+lng_m/60);
            }catch (NumberFormatException e){
                mLocation.setLongitude(0);
            }
            if(!TextUtils.isEmpty(data[5])){
                mLongitudeDirection =data[5];
            }else {
                mLongitudeDirection ="E";
            }

            //Fix quality: 0 = invalid
            //1 = GPS fix (SPS)
            //2 = DGPS fix
            //3 = PPS fix
            //4 = Real Time Kinematic
            //5 = Float RTK
            //6 = estimated (dead reckoning) (2.3 feature)
            //7 = Manual input mode
            //8 = Simulation mode
            try {
                int qualityCode=Integer.valueOf(data[6]);
                if(qualityCode>=0&&qualityCode<=8){
                    mFixQuality=FixQuality.values()[qualityCode];
                }else {
                    mFixQuality=FixQuality.values()[9];
                }
            }catch (NumberFormatException e){
                mFixQuality=FixQuality.values()[9];
            }
            try {
                mSatelliteCount =Integer.parseInt(data[7]);
            }catch (NumberFormatException e){
                mSatelliteCount =0;
            }
        }
    }

    public String getRawString() {
        return mRawString;
    }

    public Location getLocation() {
        return mLocation;
    }

    public long getTimeMillis() {
        return mTimeMillis;
    }

    public String getLatitudeDirection() {
        return mLatitudeDirection;
    }

    public String getLongitudeDirection() {
        return mLongitudeDirection;
    }

    public FixQuality getFixQuality() {
        return mFixQuality;
    }

    public int getSatelliteCount() {
        return mSatelliteCount;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public String toString() {
        return "TbGNGGABean{" +
                "mRawString='" + mRawString + '\'' +
                ", mLocation=" + mLocation +
                ", mTimeMillis=" + mTimeMillis +
                ", mLatitudeDirection='" + mLatitudeDirection + '\'' +
                ", mLongitudeDirection='" + mLongitudeDirection + '\'' +
                ", mFixQuality=" + mFixQuality +
                ", mSatelliteCount=" + mSatelliteCount +
                '}';
    }
}
