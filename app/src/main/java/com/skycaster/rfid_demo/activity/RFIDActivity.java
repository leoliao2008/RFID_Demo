package com.skycaster.rfid_demo.activity;

import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.skycaster.rfid_demo.R;
import com.skycaster.rfid_demo.base.BaseRFIDActivity;
import com.skycaster.rfid_demo.utils.AlertDialogUtil;
import com.skycaster.rfid_lib.RFIDAckCallBack;

import java.util.ArrayList;
import java.util.Random;

public class RFIDActivity extends BaseRFIDActivity {
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

        onClick(R.id.main_btn_read_tag_data, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().readTagData();
            }
        });

        onClick(R.id.main_btn_write_tag_data, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRandomBytes();
                getRFIDRequestSender().writeTagData(randomBytes[0],randomBytes[1]);
            }
        });

        onClick(R.id.main_btn_lock_tag, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().lockTag();
            }
        });

        onClick(R.id.main_btn_unlock_tag, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().unlockTag();
            }
        });

        onClick(R.id.main_btn_kill_tag, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().killTag();
            }
        });

        onClick(R.id.main_btn_reset_tag, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().resetTag();
            }
        });

        onClick(R.id.main_btn_reset_device, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().resetDevice();
            }
        });

        onClick(R.id.main_btn_stop_reading_tag, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().stopReadingTag();
            }
        });

        onClick(R.id.main_btn_restart_reading_tag, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().restartReadingTag();
            }
        });

        onClick(R.id.main_btn_activate_relay_on, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().activateDelay(true);
            }
        });

        onClick(R.id.main_btn_activate_relay_off, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().activateDelay(false);
            }
        });

        onClick(R.id.main_btn_set_bd_rate, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRFIDRequestSender().setBaudRate(RFIDActivity.this,String.valueOf(getSerialPortBdRate()));
            }
        });
    }

    private Random mRandom=new Random();
    private byte[] randomBytes=new byte[2];

    private void generateRandomBytes(){
        mRandom.nextBytes(randomBytes);
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

            @Override
            public void onTagDataRead(boolean isSuccess, byte data01, byte data02, String info) {
                updateConsole(info);
            }

            @Override
            public void onTagDataWritten(boolean isSuccess, String info) {
                updateConsole("写入数据："+info);
            }

            @Override
            public void onTagLock(boolean isSuccess, String info) {
                updateConsole("锁定写入区域："+info);
            }

            @Override
            public void onTagUnlock(boolean isSuccess, String info) {
                updateConsole("解锁写入区域："+info);
            }

            @Override
            public void onKillTag(boolean isSuccess, String info) {
                updateConsole(info);
            }

            @Override
            public void onResetTag(boolean isSuccess, String info) {
                updateConsole(info);

            }

            @Override
            public void onResetDevice(boolean isSuccess, String info) {
                updateConsole(info);

            }

            @Override
            public void onStopReading(boolean isSuccess, String info) {
                updateConsole(info);

            }

            @Override
            public void onRestartReading(boolean isSuccess, String info) {
                updateConsole(info);

            }

            @Override
            public void onControlRelay(boolean isSuccess, String info) {
                updateConsole(info);

            }

            @Override
            public void onSetBaudRate(boolean isSuccess, String info) {
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
            case R.id.menu_test_inertial_navi_module:

                break;
            default:
                break;
        }
        return true;
    }
}
