package com.skycaster.rfid_demo.activity;

import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.skycaster.inertial_navi_lib.GPGGABean;
import com.skycaster.rfid_demo.R;
import com.skycaster.rfid_demo.base.BaseInertialNaviActivity;
import com.skycaster.rfid_demo.utils.AlertDialogUtil;

import java.util.ArrayList;

public class InertialNaviActivity extends BaseInertialNaviActivity {
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mList=new ArrayList<>();
    private Handler mHandler;



    @Override
    protected int attachLayout() {
        return R.layout.activity_inertial_navi;
    }

    @Override
    protected void initViews() {
        mListView= (ListView) findViewById(R.id.activity_inertial_lstv);

    }

    @Override
    protected void initInertialData() {
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mList);
        mListView.setAdapter(mAdapter);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle("测试惯导模块");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mHandler=new Handler();
    }

    @Override
    protected void onGetData(byte[] data, int len) {
        updateConsole(new String(data,0,len));
    }

    @Override
    protected void onGPGGABeanGot(GPGGABean bean) {
        updateConsole(bean.toString());
    }

    @Override
    protected void initListeners() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inertial_navi_activity,menu);
        MenuItem item = menu.findItem(R.id.menu_toggle_transmission);
        if(isRunning.get()){
            item.setIcon(R.drawable.ic_pause_white_48dp);
        }else {
            item.setIcon(R.drawable.ic_play_arrow_white_48dp);
        }
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
            case R.id.menu_toggle_transmission:
                isRunning.set(!isRunning.get());
                if(isRunning.get()){
                    startExtracting();
                }else {
                    stopExtracting();
                }
                supportInvalidateOptionsMenu();
                break;
        }
        return true;
    }


    private void updateConsole(final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mList.add(msg);
                mAdapter.notifyDataSetChanged();
                mListView.smoothScrollToPosition(Integer.MAX_VALUE);
            }
        });
    }

    @Override
    protected void showHint(String message) {
        updateConsole(message);
    }
}
