package com.skycaster.rfid_lib;

/**
 * Created by 廖华凯 on 2017/5/26.
 */

public abstract class RFIDAckCallBack {
    public abstract void onVersionNameGet(String versionName);

    /**
     * @param  isSuccess 是否操作成功
     * @param antennaId 天线号
     * @param tagId 标签Id
     */
    public abstract void onTagDetected(boolean isSuccess,String antennaId, String tagId,String info);
}
