package com.example.stm32update;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stm32update.Ymodem.YModem;
import com.example.stm32update.Ymodem.YModemListener;
import com.example.stm32update.adapter.OTAFileListAdapter;
import com.example.stm32update.model.OTAFileModel;
import com.example.stm32update.service.BluetoothService;
import com.example.stm32update.utils.config.BroadCast;
import com.example.stm32update.utils.config.Constants;
import com.example.stm32update.utils.config.GattAttributes;
import com.example.stm32update.widgets.TextProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class OTAAcivity extends AppCompatActivity implements View.OnClickListener{

    /**
     * 静态的非变量成员
     */
    private static final String TAG = "OTAActivity";
    public static final int mApplicationUpgrade = 101;
    public static final int mApplicationAndStackCombined = 201;
    public static final int mApplicationAndStackSeparate = 301;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private TextView tvfilanme = null;
    private Button sendBtn =null;
    public BluetoothService bluetoothService;//接收蓝牙广播
    public static BluetoothGattCharacteristic mSendOTACharacteristic;
    //获取得到的设备地址和设备名称
    private String deviceAddre;
    private String deviceName;
    private YModem yModem;
    private boolean sendData;
    //加载文件相关
    private OTAFileListAdapter mFirmwareAdapter;
    private ListView mFileListView;
    private int mFilesCount;
    public static Boolean mApplicationInBackground = false;
    private final ArrayList<OTAFileModel> mArrayListFiles = new ArrayList<>();
    private final ArrayList<String> mArrayListPaths = new ArrayList<>();
    private final ArrayList<String> mArrayListFileNames = new ArrayList<>();

    TextView blename = null;
    TextView bleadress = null;
    TextView updateinfo = null;
    public String updatafilepath = null;
    String updatafilename = null;//bin文件路径
    TextProgressBar mUpgradeBar =null;//bin文件名
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otaacivity);
        initView();
        initData();
        /*
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isSuccess = bluetoothService.writeCharacteristic(OTAAcivity.mSendOTACharacteristic,"0x05");
            }
        });*/
        /**
         * File Selection click event
         */

        //Log.e(TAG, "进入ota");//---------------------测试成功
    }
    private void initView(){

        sendBtn = findViewById(R.id.sendbtn);
        //updateinfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        blename = findViewById(R.id.blename);
        bleadress =findViewById(R.id.bleadress);
        updateinfo = findViewById(R.id.updateinfo);
        updateinfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        updateinfo.setScrollbarFadingEnabled(false);

    }
    private void initData(){
        deviceAddre = getIntent().getStringExtra(OTAAcivity.EXTRAS_DEVICE_ADDRESS);
        deviceName = getIntent().getStringExtra(OTAAcivity.EXTRAS_DEVICE_NAME);
        blename.setText(deviceName);
        bleadress.setText(deviceAddre);
        mFilesCount = mApplicationAndStackCombined ;
        sendBtn.setOnClickListener(this);

        //创建service
        Intent gattServiceIntent = new Intent(this, BluetoothService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    /**
     * 发送服务广播
     * @return
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCast.ACTION_GATT_CONNECTED);//连接通知
        intentFilter.addAction(BroadCast.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BroadCast.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BroadCast.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BroadCast.ACTION_OTA_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        return intentFilter;
    }
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver(){//Receive function "BluetoothGattCallback" 's Radio
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();
            // 如果已经连接
            if (BroadCast.ACTION_GATT_CONNECTED.equals(action)) {

                //Log.e(TAG, "连接成功!");//---------------------测试成功
            } else if (BroadCast.ACTION_GATT_DISCONNECTED.equals(action)) {
                // 如果没有连接
                /**
                 * 当蓝牙连接出现断开的时候那么需要把该界面finish()掉
                 * 我这里是清除了所有的界面 除了MainActivity
                 */
            } else if (BroadCast.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {//ACTION_GATT_SERVICES_DISCOVERED
                // 发现服务
                Log.e(TAG, "发现服务!");
                displayGattServices(bluetoothService.getSupportedGattServices());

            }

            //ota的 接收信息
            else if (BroadCast.ACTION_DATA_AVAILABLE.equals(action)) {
                if(extras.containsKey(BroadCast.ACTION_OTA_DATA)) {
                   // byte[] otadata = intent.getStringExtra(BroadCast.ACTION_OTA_DATA);
                    String otadata = intent.getStringExtra(BroadCast.ACTION_OTA_DATA);
                    updateinfo.append(otadata.toString()+"\r\n");
                    //if (otadata.equalsIgnoreCase("C"))
                       // onDataReceivedFromBLE(strToByteArray(otadata));//这句出错bug

                    ///////////////////////////////////////////////////////////////////
                }else{
                    sendData = false;
                }
            }
        }
    };
    /*
    *       (ergodic)Find the Bluetoothcharacteristic
    * 遍历BluetoothCharacteristic
    */
   // BluetoothGatt----BluetoothGattService---
    private void displayGattServices(List<BluetoothGattService> gattServices) {

        for(BluetoothGattService gattService : gattServices){
            //List<BluetoothGattCharacteristic> gattCharacteristicss = gattService.getCharacteristics();
            String uuid = gattService.getUuid().toString();
            if (uuid.equals(GattAttributes.GATT_SEVICE)){
                List<BluetoothGattCharacteristic> gattCharacteristicss = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristicss) {
                    String uuidchara = gattCharacteristic.getUuid().toString();
                    if(uuidchara.equalsIgnoreCase(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG)){
                        //Log.e(TAG,"GattCharacteristic:READ:"+gattCharacteristic.getUuid().toString());
                        //bluetoothService.enableNotification(UUID.fromString(GattAttributes.GATT_SEVICE),UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                        prepareBroadcastDataRead(gattCharacteristic);
                        prepareBroadcastDataIndicate(gattCharacteristic);
                    }
                    if (uuidchara.equalsIgnoreCase(GattAttributes.BW_PROJECT_OTA_DATA)){
                        mSendOTACharacteristic = gattCharacteristic;
                        //Log.e(TAG,"GattCharacteristic:Write:"+gattCharacteristic.getUuid().toString());

                    }
                }
            }


        }
    }
    void prepareBroadcastDataRead(BluetoothGattCharacteristic gattCharacteristic) {
        if ((gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

            bluetoothService.readCharacteristic(gattCharacteristic);
        }

    }

    void prepareBroadcastDataIndicate(
            BluetoothGattCharacteristic gattCharacteristic) {
        if ((gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            bluetoothService.setCharacteristicIndication(gattCharacteristic, true);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

    }
    /**
     * 蓝牙连接
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {//连接到系统服务
            bluetoothService = ((BluetoothService.LocalBinder) service).getService();
            if (!bluetoothService.initialize()) {
                //Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            if(deviceAddre!=null) {
                bluetoothService.connect(deviceAddre, OTAAcivity.this);

            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //yModem.stop();
        unregisterReceiver(mGattUpdateReceiver);
        //断开服务连接
        unbindService(mServiceConnection);
        //断开蓝牙服务连接
        if(bluetoothService!=null){
            bluetoothService.disconnect();
        }
    }
    public void onDataReceivedFromBLE(byte[] data) {
        yModem.onReceiveData(data);
        sendData = true;
    }
    public static byte[] strToByteArray(String str) {
        if (str == null) {
            return null;
        }
        byte[] byteArray = str.getBytes();
        return byteArray;
    }
    /**
     * Method to search phone/directory for the .bin files
     * 对手机搜索/目录的方法.bin文件
     * @param dir
     */
    private void searchRequiredFile(File dir) {
        if (dir.exists()) {
            String filePattern = "bin";
            File[] allFilesList = dir.listFiles();
            for (int pos = 0; pos < allFilesList.length; pos++) {
                File analyseFile = allFilesList[pos];
                if (analyseFile != null) {
                    if (analyseFile.isDirectory()) {
                        searchRequiredFile(analyseFile);
                    } else {
                        Uri selectedUri = Uri.fromFile(analyseFile);
                        String fileExtension
                                = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
                        if (fileExtension.equalsIgnoreCase(filePattern)) {
                            OTAFileModel fileModel = new OTAFileModel(analyseFile.getName(),
                                    analyseFile.getAbsolutePath(), false, analyseFile.getParent());
                           mArrayListFiles.add(fileModel);
                           mFirmwareAdapter.addFiles(mArrayListFiles);
                           mFirmwareAdapter.notifyDataSetChanged();
                        }
                    }

                }
            }
        } else {
            Toast.makeText(this, "Directory does not exist", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * File Selection click event
     */


    @Override
    public void onClick(View view) {
       if (view.getId()==R.id.sendbtn){
           boolean isSuccess = bluetoothService.SendPassWord(OTAAcivity.mSendOTACharacteristic,"xiaowenlg".getBytes());//0x01为选择更新的标志
            /*if(isSuccess){
                startTransmission();
            }*/
        }
    }
}
