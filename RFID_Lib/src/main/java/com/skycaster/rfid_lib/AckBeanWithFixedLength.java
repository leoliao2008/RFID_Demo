package com.skycaster.rfid_lib;

import android.util.Log;

/**
 * Created by 廖华凯 on 2017/5/27.
 */

public class AckBeanWithFixedLength {
    private String TAG;
    private byte[] data=new byte[6];
    private String statusDescription;

    public AckBeanWithFixedLength(byte[] data) {
        TAG=getClass().getSimpleName();
        System.arraycopy(data,0,this.data,0,6);
    }

    public PackageType getPackageType(){
        return PackageType.getTypeByByte(data[0]);
    }

    public byte getStatusCode(){
        return data[4];
    }

    public String getStatusDescription(){
        switch (getStatusCode()){
            case 0x00:
                return "命令成功完成";
            case 0x02:
                return "CRC校验错误";
            case 0x10:
                return "非法命令";
            case 0x01:
                return "其它错误";
            case 0x03:
                return "未知错误 状态码为0x03";
            case 0x04:
            case 0x05:
                return "标签读取失败";//貌似读取不到tag时会返回以上数据
            default:
                showLog("数据不符合协议，默认错误,状态码：0x"+String.format("%02X",getStatusCode()));
                return "数据不符合协议，默认错误。";
        }
    }

    private void showLog(String msg){
        Log.e(TAG,msg);
    }


}
