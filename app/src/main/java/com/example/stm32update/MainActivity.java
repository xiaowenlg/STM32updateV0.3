package com.example.stm32update;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.stm32update.adapter.LeDeviceListAdapter;
import com.example.stm32update.widgets.CustomsDialog;

import butterknife.Bind;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    @Bind(R.id.mDeviceList)
    ListView mDeviceList;
    private SwipeRefreshLayout mRefresh;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    //扫描时间为5秒
    private static final int SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_BT = 1;
    private CustomsDialog mDialog;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void initView() {
        mHandler = new Handler();
        mDeviceList = findViewById(R.id.mDeviceList);
        mRefresh = findViewById(R.id.mRefresh);
        mRefresh.setOnRefreshListener(this);
        mRefresh.setColorSchemeResources(R.color.colorNav);
        //点击事件
        mDeviceList.setOnItemClickListener(onItemClickListener);
    }
    /**
     * listview点击项目
     */
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            BluetoothDevice device = mLeDeviceListAdapter
                    .getDevice(position);
            if (device == null) {
                return;
            }
            //showDialog(device);//jump function
        }
    };
    @Override
    public void onRefresh() {

    }
}
