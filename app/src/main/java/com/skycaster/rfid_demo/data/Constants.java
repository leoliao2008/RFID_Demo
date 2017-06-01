package com.skycaster.rfid_demo.data;

import android.Manifest;

/**
 * Created by 廖华凯 on 2017/5/23.
 */

public interface Constants {
    String[] SYSTEM_PERMISSIONS=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    String[] AVAILABLE_BAUD_RATES=new String[]{"9600","19200","38400","57600","115200"};
}
