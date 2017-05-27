package com.skycaster.rfid_lib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by 廖华凯 on 2017/5/26.
 */

public class RequestBean {
    private byte[] data;

    public RequestBean(@NonNull PackageType type, @Nullable byte[] param) {
        int len;
        if(param!=null){
            len=param.length;
        }else {
            len=0;
        }
        data=new byte[5+len];
        data[0]= (byte) (0xa0&0xff);
        data[1]= (byte) ((3+len)&0xff);
        data[2]=type.toByte();
        data[3]=0;
        if(param!=null){
            System.arraycopy(param,0,data,4,len);
        }
        data[data.length-1]=generateVerifyCode(data,data.length);
    }

    public RequestBean(PackageType type){
        this(type,null);
    }


    private byte generateVerifyCode(byte[] request,int len){
        byte b=0x00;
        len--;
        for(int i=0;i<len;i++){
            b+=request[i];
        }
        b= (byte) ((~b+1)&0xff);
        return b;
    }

    public byte[] toBytes(){
        return data;
    }

    public int len(){
        return data.length;
    }
}
