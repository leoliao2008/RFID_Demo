package com.skycaster.rfid_lib;

/**
 * Created by 廖华凯 on 2017/5/27.
 */

public enum PackageType {
    TYPE_DEVICE_VERSION,
    TYPE_DETECT_TAG,
    TYPE_READ_TAG,
    TYPE_NOT_FOUND;


    public byte toByte(){
        byte b=0;
        switch (this){
            case TYPE_DEVICE_VERSION:
                b=0x6A&0xff;
                break;
            case TYPE_DETECT_TAG:
                b= (byte) (0x82&0xff);
                break;
            case TYPE_READ_TAG:
                b= (byte) (0x80&0xff);
            default:
                break;
        }
        return b;
    }

    public static PackageType getTypeByByte(byte b){
        PackageType[] types = PackageType.values();
        int len = types.length;
        for(int i=0;i<len;i++){
            if(types[i].toByte()==b){
                return types[i];
            }
        }
        return TYPE_NOT_FOUND;
    }
}
