package com.skycaster.rfid_demo.utils;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 廖华凯 on 2017/5/23.
 */

public class RFIDUtil {

    public static boolean checkIfDllInstalled(Context context){
        boolean isInstalled=false;
        try {
            InputStream inputStream = context.getAssets().open("Disdll.dll");
            File file = context.getFileStreamPath("Disdll.dll");
            if(file!=null){
                if(file.length()==inputStream.available()){
                    isInstalled=true;
                }
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isInstalled;
    }

    public static boolean installDll(Context context){
        boolean isSuccess=false;
        File file = context.getFileStreamPath("Disdll.dll");
        boolean isDeleteSuccess=true;
        if(file.exists()){
            isDeleteSuccess=file.delete();
        }
        if(isDeleteSuccess){
            try {
                FileOutputStream outputStream = context.openFileOutput("Disdll.dll", Context.MODE_PRIVATE);
                InputStream inputStream = context.getAssets().open("Disdll.dll");
                byte[] temp=new byte[1024];
                int len=-1;
                while ((len=inputStream.read(temp))!=-1){
                    outputStream.write(temp,0,len);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            isSuccess=checkIfDllInstalled(context);
        }
        return isSuccess;
    }
}
