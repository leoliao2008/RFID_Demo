package com.skycaster.inertial_navi_lib;

import android.location.Location;
import android.util.Log;

/**
 * Created by 廖华凯 on 2017/6/6.
 */

public class GPSBean {

    private Location location;
    private String latitudeDirection;
    private String longitudeDirection;
    private int satelliteCount;
    private FixQuality fixQuality;
    private float horizontalDelusion;
    private float geoidHeight;
    private int secSinceLastUpdate;
    private int DGPSStationId;


    public GPSBean(String source){
        // $GPGGA,235949.042,0000.0000,N,00000.0000,E,0,00,,0.0,M,0.0,M,,0000*45
        // $GPGGA,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>,<9>,<10>,<11>,<12>,<13>,<14>*<15><CR><LF>
        //<1> UTC时间，格式为hhmmss.sss。
        //<2> 纬度，格式为ddmm.mmmm（前导位数不足则补0）。
        //<3> 纬度半球，N或S（北纬或南纬）。
        //<4> 经度，格式为dddmm.mmmm（前导位数不足则补0）。
        //<5> 经度半球，E或W（东经或西经）。
        //<6> 定位质量指示，0=定位无效，1=定位有效。
        //<7> 使用卫星数量，从00到12（前导位数不足则补0）。
        //<8> 水平精确度，0.5到99.9。
        //<9> 天线离海平面的高度，-9999.9到9999.9米
        //<10> 高度单位，M表示单位米。
        //<11> 大地椭球面相对海平面的高度（-9999.9到9999.9）。
        //<12> 高度单位，M表示单位米。
        //<13> 差分GPS数据期限（RTCM SC-104），最后设立RTCM传送的秒数量。
        //<14> 差分参考基站标号，从0000到1023（前导位数不足则补0）。
        //<15> 校验和。
        location=new Location("");
        String[] data = source.split(",");
        try {

            double s = Double.parseDouble(data[1]);
            short h= (short) (s/10000);
            short m= (short) (s/100-h*100);
            double milliSec=(s-h*10000-m*100)*1000;
            milliSec=Math.ceil(milliSec);
            long timeMillis=(long) (h*3600*1000+m*60*1000+milliSec);
            location.setTime(timeMillis);
            //<2> 纬度，格式为ddmm.mmmm（前导位数不足则补0）。
            location.setLatitude(Double.parseDouble(data[2])/100);
            latitudeDirection=data[3];
            //<4> 经度，格式为dddmm.mmmm（前导位数不足则补0）。
            location.setLongitude(Double.parseDouble(data[4])/100);
            longitudeDirection=data[5];
            //Fix quality: 0 = invalid
            //1 = GPS fix (SPS)
            //2 = DGPS fix
            //3 = PPS fix
            //4 = Real Time Kinematic
            //5 = Float RTK
            //6 = estimated (dead reckoning) (2.3 feature)
            //7 = Manual input mode
            //8 = Simulation mode
            int qualityCode=Integer.valueOf(data[6]);
            if(qualityCode>=0&&qualityCode<=8){
                fixQuality=FixQuality.values()[qualityCode];
            }else {
                fixQuality=FixQuality.values()[9];
            }
            satelliteCount=Integer.parseInt(data[7]);
            try {
                horizontalDelusion=Float.parseFloat(data[8]);
            }catch (NumberFormatException e){
                horizontalDelusion=0;
            }
            location.setAltitude(Float.parseFloat(data[9]));
            geoidHeight=Float.parseFloat(data[11]);
            try {
                secSinceLastUpdate=Integer.parseInt(data[13]);
            }catch (NumberFormatException e){
                secSinceLastUpdate=0;
            }
            String[] tail = data[14].split("/*");
            try {
                DGPSStationId=Integer.parseInt(tail[0]);
            }catch (NumberFormatException e){
                DGPSStationId=0;
            }
        }catch (ArrayIndexOutOfBoundsException e){
            Log.e(getClass().getSimpleName(), "GPGGA format invalid. Index out of array.");
        }
    }

    private void showLog(String msg){
        Log.e(getClass().getSimpleName(), msg);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getLatitudeDirection() {
        return latitudeDirection;
    }

    public void setLatitudeDirection(String latitudeDirection) {
        this.latitudeDirection = latitudeDirection;
    }

    public String getLongitudeDirection() {
        return longitudeDirection;
    }

    public void setLongitudeDirection(String longitudeDirection) {
        this.longitudeDirection = longitudeDirection;
    }

    public int getSatelliteCount() {
        return satelliteCount;
    }

    public void setSatelliteCount(int satelliteCount) {
        this.satelliteCount = satelliteCount;
    }

    public FixQuality getFixQuality() {
        return fixQuality;
    }

    public void setFixQuality(FixQuality fixQuality) {
        this.fixQuality = fixQuality;
    }

    public float getHorizontalDelusion() {
        return horizontalDelusion;
    }

    public void setHorizontalDelusion(float horizontalDelusion) {
        this.horizontalDelusion = horizontalDelusion;
    }

    public float getGeoidHeight() {
        return geoidHeight;
    }

    public void setGeoidHeight(float geoidHeight) {
        this.geoidHeight = geoidHeight;
    }

    public int getSecSinceLastUpdate() {
        return secSinceLastUpdate;
    }

    public void setSecSinceLastUpdate(int secSinceLastUpdate) {
        this.secSinceLastUpdate = secSinceLastUpdate;
    }

    public int getDGPSStationId() {
        return DGPSStationId;
    }

    public void setDGPSStationId(int DGPSStationId) {
        this.DGPSStationId = DGPSStationId;
    }
}
