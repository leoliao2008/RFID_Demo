package com.skycaster.rfid_demo.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.skycaster.rfid_demo.R;
import com.skycaster.rfid_demo.base.RFIDActivity;
import com.skycaster.rfid_demo.data.Constants;

import project.SerialPort.SerialPortFinder;

/**
 * Created by 廖华凯 on 2017/5/23.
 */

public class AlertDialogUtil {
    private static AlertDialog alertDialog;
    private static String serialPortPath;
    private static int serialPortRate;

    public static void showAlertDialog(Activity context, String msg, @Nullable final Runnable onConfirm, @Nullable final Runnable onCancel){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.gentle_warning)).setMessage(msg);
        builder.setPositiveButton(context.getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(onConfirm!=null){
                    onConfirm.run();
                }
                alertDialog.dismiss();
            }
        });
        if(onCancel!=null){
            builder.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onCancel.run();
                    alertDialog.dismiss();
                }
            });
        }
        alertDialog=builder.create();
        alertDialog.show();
    }

    public static void showSerialPortSettingView(final RFIDActivity context) {
        //init view
        View rootView=context.getLayoutInflater().inflate(R.layout.set_serial_port_layout,null);
        Spinner spn_paths= (Spinner) rootView.findViewById(R.id.set_sp_layout_sp_path_options);
        Spinner spn_baudRates= (Spinner) rootView.findViewById(R.id.set_sp_layout_bd_rate_options);
        Button btn_confirm= (Button) rootView.findViewById(R.id.set_sp_layout_btn_confirm);
        Button btn_cancel= (Button) rootView.findViewById(R.id.set_sp_layout_btn_cancel);
        //init data
        //init data--path spinner
        SerialPortFinder portFinder=new SerialPortFinder();
        final String[] paths = portFinder.getAllDevicesPath();
        ArrayAdapter<String> pathsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, paths);
        pathsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_paths.setAdapter(pathsAdapter);
        serialPortPath = context.getSerialPortPath();
        for(int i=0;i<paths.length;i++){
            if(serialPortPath.equals(paths[i])){
                spn_paths.setSelection(i);
                break;
            }
        }
        spn_paths.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serialPortPath=paths[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //init data--bd rate spinner
        final String[] baudRates = Constants.AVAILABLE_BAUD_RATES;
        ArrayAdapter<String> bdRateAdapter=new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,baudRates);
        bdRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_baudRates.setAdapter(bdRateAdapter);
        serialPortRate=context.getSerialPortBdRate();
        for(int i=0;i<baudRates.length;i++){
            if(serialPortRate==Integer.valueOf(baudRates[i])){
                spn_baudRates.setSelection(i);
                break;
            }
        }
        spn_baudRates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serialPortRate=Integer.valueOf(baudRates[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //init listener
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.openSerialPort(serialPortPath,serialPortRate);
                alertDialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        //init dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        alertDialog=builder.setTitle("设置串口").setView(rootView).create();
        alertDialog.show();
    }
}
