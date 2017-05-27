package com.skycaster.rfid_lib;

/**
 * Created by 廖华凯 on 2017/5/27.
 */

public class AckBeanWithFixedLength {
    private byte[] data=new byte[6];
    private String statusDescription;

    public AckBeanWithFixedLength(byte[] data) {
        System.arraycopy(data,0,this.data,0,6);
    }

    public PackageType getPackageType(){
        return PackageType.getTypeByByte(data[0]);
    }

    public String getStatusDescription(){
        switch (data[4]){
            case 0x00:
                return "命令成功完成";
            case 0x02:
                return "CRC校验错误";
            case 0x10:
                return "非法命令";
            case 0x01:
                return "其它错误";
            case 0x04:
            case 0x05:
                return "标签读取失败";//貌似读取不到tag时会返回这两个数据
            default:
                return "数据不符合协议，默认错误。";
        }
    }


}
