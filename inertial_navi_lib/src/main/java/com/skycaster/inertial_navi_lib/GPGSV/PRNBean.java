package com.skycaster.inertial_navi_lib.GPGSV;

/**
 * PRN 码（伪随机噪声码）（01 - 32）
 */

public class PRNBean {
    private String mPrnCode;//PRN 码（伪随机噪声码）（01 - 32）（前导位数不足则补0）
    private float mElevation;//卫星仰角（00 - 90）度（前导位数不足则补0）
    private float mAzimuth;//卫星方位角（00 - 359）度（前导位数不足则补0）
    private float mSnr;//信噪比（00－99）dbHz

    public String getPrnCode() {
        return mPrnCode;
    }

    public void setPrnCode(String prnCode) {
        mPrnCode = prnCode;
    }

    public float getElevation() {
        return mElevation;
    }

    public void setElevation(float elevation) {
        mElevation = elevation;
    }

    public float getAzimuth() {
        return mAzimuth;
    }

    public void setAzimuth(float azimuth) {
        mAzimuth = azimuth;
    }

    public float getSnr() {
        return mSnr;
    }

    public void setSnr(float snr) {
        mSnr = snr;
    }

    @Override
    public String toString() {
        return "PRNBean{" +
                "mPrnCode='" + mPrnCode + '\'' +
                ", mElevation=" + mElevation +
                ", mAzimuth=" + mAzimuth +
                ", mSnr=" + mSnr +
                '}';
    }
}
