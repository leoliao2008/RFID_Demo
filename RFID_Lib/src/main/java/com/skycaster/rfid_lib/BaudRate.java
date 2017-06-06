package com.skycaster.rfid_lib;

/**
 * Created by 廖华凯 on 2017/6/2.
 */

public enum BaudRate {
    BD9600,BD19200,BD38400,BD57600,BD115200;

    public byte toByte(){
        byte b;
        switch (this){
            case BD9600:
                b=0x00;
                break;
            case BD19200:
                b=0x01;
                break;
            case BD38400:
                b=0x02;
                break;
            case BD57600:
                b=0x03;
                break;
            case BD115200:
                b=0x04;
                break;
            default:
                b=0x00;
                break;
        }
        return b;
    }
}
