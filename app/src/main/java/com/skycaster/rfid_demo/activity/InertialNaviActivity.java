package com.skycaster.rfid_demo.activity;

import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    protected void initListeners() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inertial_navi_activity,menu);
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
        }
        return true;
    }

    @Override
    protected void onGetSerialPortData(byte[] data, int len) {
        updateConsole(new String(data,len));
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
