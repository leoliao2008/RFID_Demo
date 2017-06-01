package com.skycaster.rfid_lib;

/**
 * Created by 廖华凯 on 2017/5/26.
 */

public abstract class RFIDAckCallBack {
    /**
     * 获取设备系统版本号的回调
     * @param versionName 系统版本号
     */
    public abstract void onVersionNameGet(String versionName);

    /**
     * 识别标签的回调
     * @param isSuccess 是否操作成功
     * @param antennaId 天线号
     * @param tagId 标签Id
     * @param info 辅助信息
     */
    public abstract void onTagDetected(boolean isSuccess,String antennaId, String tagId,String info);

    /**
     * 获取标签里面长度为两个字节的写入数据的回调。写入数据由两个16进制形式的byte组成，根据使用环境自定义解析协议。
     * @param isSuccess 是否操作成功
     * @param data01 写入数据的首位
     * @param data02 写入数据的次位
     * @param info 辅助信息
     */
    public abstract void onTagDataRead(boolean isSuccess,byte data01, byte data02,String info);


    /**
     * 往标签中写入长度为两个字节的数据后的回调
     * @param isSuccess 是否操作成功
     * @param info 辅助信息
     */
    public abstract void onTagDataWritten(boolean isSuccess, String info);

    /**
     * 锁住标签数据区域的回调
     * @param isSuccess 是否操作成功
     * @param info 辅助信息
     */
    public abstract void onTagLock(boolean isSuccess, String info);

    /**
     * 解锁标签数据区域的回调
     * @param isSuccess 是否操作成功
     * @param info 辅助信息
     */
    public abstract void onTagUnlock(boolean isSuccess, String info);

    public abstract void onKillTag(boolean isSuccess, String info);

    public abstract void onResetTag(boolean isSuccess, String info);

    public abstract void onResetDevice(boolean isSuccess, String info);

    public abstract void onStopReading(boolean isSuccess, String info);

    public abstract void onRestartReading(boolean isSuccess, String info);

    public abstract void onControlRelay(boolean isSuccess, String info);

    public abstract void onSetBaudRate(boolean isSuccess, String info);
}
