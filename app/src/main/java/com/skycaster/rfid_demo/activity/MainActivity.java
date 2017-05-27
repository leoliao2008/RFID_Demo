package com.skycaster.rfid_demo.activity;

import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.skycaster.rfid_demo.R;
import com.skycaster.rfid_demo.base.RFIDActivity;
import com.skycaster.rfid_demo.utils.AlertDialogUtil;
import com.skycaster.rfid_lib.RFIDAckCallBack;

import java.util.ArrayList;

public class MainActivity extends RFIDActivity {
    private ListView mListView;
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> messages=new ArrayList<>();
    private ActionBar mActionBar;

    @Override
    protected int attachLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        mListView= (ListView) findViewById(R.id.main_console);

    }


    @Override
    protected void initRFIDData() {
        mArrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,messages);
        mListView.setAdapter(mArrayAdapter);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            initActionBar(actionBar);
        }
    }

    private void initActionBar(ActionBar actionBar) {
        mActionBar=actionBar;
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("JT2850 RFID模块测试");
    }


    @Override
    protected void initListeners() {
        onClick(R.id.main_btn_get_dev_version, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().requestVersionName();
            }
        });

        onClick(R.id.main_btn_detect_tag, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().detectTag();
            }
        });

    }

    @Override
    protected RFIDAckCallBack initRFIDAckCallBack() {
        return new RFIDAckCallBack() {
            @Override
            public void onVersionNameGet(String versionName) {
                updateConsole("设备版本号："+versionName);
            }

            @Override
            public void onTagDetected(boolean isSuccess,String antennaId, String tagId,String info) {
                updateConsole(info);
            }
        };
    }

    @Override
    protected void showHint(String message) {
        updateConsole(message);
    }

    private void updateConsole(String message) {
        if(mArrayAdapter!=null){
            messages.add(message);
            mArrayAdapter.notifyDataSetChanged();
            mListView.smoothScrollToPosition(Integer.MAX_VALUE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_set_serial_port:
                AlertDialogUtil.showSerialPortSettingView(this);
                break;
            default:
                break;
        }
        return true;
    }
}
