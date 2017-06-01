package com.skycaster.rfid_lib;

/**
 * Created by 廖华凯 on 2017/5/27.
 */

public enum PackageType {
    TYPE_DEVICE_VERSION,
    TYPE_DETECT_TAG,
    TYPE_READ_TAG_DATA,
    TYPE_WRITE_TAG_DATA,
    TYPE_LOCK_TAG,
    TYPE_UNLOCK_TAG,
    TYPE_KILL_TAG,
    TYPE_RESET_TAG,
    TYPE_RESET_DEVICE,
    TYPE_READING_STOP,
    TYPE_READING_RESTART,
    TYPE_CONTROL_RELAY,
    TYPE_SET_BAUD_RATE,
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
            case TYPE_READ_TAG_DATA:
                b= (byte) (0x80&0xff);
                break;
            case TYPE_WRITE_TAG_DATA:
                b= (byte) (0x81&0xff);
                break;
            case TYPE_LOCK_TAG:
                b= (byte) (0xA5&0xff);
                break;
            case TYPE_UNLOCK_TAG:
                b= (byte) (0xA6&0xff);
                break;
            case TYPE_KILL_TAG:
                b= (byte) (0x86&0xff);
                break;
            case TYPE_RESET_TAG:
                b= (byte) (0x99&0xff);
                break;
            case TYPE_RESET_DEVICE:
                b=0x65;
                break;
            case TYPE_READING_STOP:
                b= (byte) (0xA8&0xff);
                break;
            case TYPE_READING_RESTART:
                b= (byte) (0xFC&0xff);
                break;
            case TYPE_CONTROL_RELAY:
                b= (byte) (0xB1&0xff);
                break;
            case TYPE_SET_BAUD_RATE:
                b= (byte) (0xA9&0xff);
                break;
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
