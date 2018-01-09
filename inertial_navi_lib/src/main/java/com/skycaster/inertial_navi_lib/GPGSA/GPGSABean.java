package com.skycaster.inertial_navi_lib.GPGSA;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * GPS DOP and Active Satellites（GSA）当前卫星信息。
 * 可以获取卫星定位模式、定位类型、PRN码（伪随机噪声码）、PDOP综合位置精度因子、
 * HDOP水平精度因子、VDOP垂直精度因子等信息。
 * 参考网站：http://blog.csdn.net/lzyzuixin/article/details/6161507
 */

public class GPGSABean {
//    $GPGSA,A,3,01,20,19,13,,,,,,,,,40.4,24.4,32.2*0A
//    字段0：$GPGSA，语句ID，表明该语句为GPS DOP and Active Satellites（GSA）当前卫星信息
//    字段1：定位模式，A=自动2D/3D，M=手动2D/3D
//    字段2：定位类型，1=未定位，2=2D定位，3=3D定位
//    字段3：PRN码（伪随机噪声码），第1信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段4：PRN码（伪随机噪声码），第2信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段5：PRN码（伪随机噪声码），第3信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段6：PRN码（伪随机噪声码），第4信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段7：PRN码（伪随机噪声码），第5信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段8：PRN码（伪随机噪声码），第6信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段9：PRN码（伪随机噪声码），第7信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段10：PRN码（伪随机噪声码），第8信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段11：PRN码（伪随机噪声码），第9信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段12：PRN码（伪随机噪声码），第10信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段13：PRN码（伪随机噪声码），第11信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段14：PRN码（伪随机噪声码），第12信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段15：PDOP综合位置精度因子（0.5 - 99.9）
//    字段16：HDOP水平精度因子（0.5 - 99.9）
//    字段17：VDOP垂直精度因子（0.5 - 99.9）
//    字段18：校验值
    private GPGSAMode mMode;//定位模式
    private GPGSAType mType;//定位类型
    private byte[] mPrns=new byte[12];//PRN码（01~32）
    private int validCount;//当前有效PRN码个数（0-12）
    private float mPdop;//PDOP综合位置精度因子（0.5 - 99.9）
    private float mHdop;//HDOP水平精度因子（0.5 - 99.9）
    private float mVdop;//VDOP垂直精度因子（0.5 - 99.9）
    private String mSrcString;//原始字符串
    private boolean isValid;//表示该bean是否一个有效的GPGSA，默认false

    private GPGSABean() {
    }

    public GPGSABean(String srcString) {
        mSrcString = srcString;
        int endIndex = mSrcString.indexOf('*');
        if(endIndex<0){
            return;
        }
        //把*及后面的验证码截掉
        String substring = mSrcString.substring(0, endIndex);
        String[] splits = substring.split(Pattern.quote(","));
        //如果格式不符合标准，不再跑下面的逻辑
        if(splits.length!=18){
            return;
        }
        isValid=true;
        //字段1：定位模式，A=自动2D/3D，M=手动2D/3D
        mMode=splits[1].equals("A")?GPGSAMode.AUTO:GPGSAMode.MANUAL;
        //字段2：定位类型，1=未定位，2=2D定位，3=3D定位
        switch (splits[2]){
            case "2":
                mType=GPGSAType.FIX2D;
                break;
            case "3":
                mType=GPGSAType.FIX3D;
                break;
            default://"1"
                mType=GPGSAType.UNFIX;
                break;
        }
        //PRN码
        for(int i=0;i<12;i++){
            String temp = splits[3 + i];
            if(TextUtils.isEmpty(temp)){
                mPrns[i]= (byte) 0xff;
            }else {
                mPrns[i]=Byte.valueOf(temp);
                validCount++;
            }
        }
        //PDOP综合位置精度因子（0.5 - 99.9）
        mPdop=Float.valueOf(splits[15]);
        //HDOP水平精度因子（0.5 - 99.9）
        mHdop=Float.valueOf(splits[16]);
        //VDOP垂直精度因子（0.5 - 99.9）
        mVdop=Float.valueOf(splits[17]);
    }

    public GPGSAMode getMode() {
        return mMode;
    }

    public GPGSAType getType() {
        return mType;
    }

    public byte[] getPrns() {
        return mPrns;
    }

    public int getValidCount() {
        return validCount;
    }

    public float getPdop() {
        return mPdop;
    }

    public float getHdop() {
        return mHdop;
    }

    public float getVdop() {
        return mVdop;
    }

    public String getSrcString() {
        return mSrcString;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public String toString() {
        return "GPGSABean{" +
                "mMode=" + mMode +
                ", mType=" + mType +
                ", mPrns=" + Arrays.toString(mPrns) +
                ", validCount=" + validCount +
                ", mPdop=" + mPdop +
                ", mHdop=" + mHdop +
                ", mVdop=" + mVdop +
                ", mSrcString='" + mSrcString + '\'' +
                ", isValid=" + isValid +
                '}';
    }
}
