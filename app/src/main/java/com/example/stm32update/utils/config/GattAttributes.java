package com.example.stm32update.utils.config;

public class GattAttributes {
    //温度类型
    public static final String GATT_SEVICE = "0003cdd0-0000-1000-8000-00805f9b0131";//sevive
    //usual
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "0003cdd1-0000-1000-8000-00805f9b0131";//读
    //通过OTA发送数据到设备 更新设备的功能
    public static final String BW_PROJECT_OTA_DATA = "0003cdd2-0000-1000-8000-00805f9b0131";//写

    //WH-BLE 102的另外两个服务
    public static final String GENERIC_ACCESS_SERVICE = "00001800-0000-1000-8000-00805f9b34fb";
    public static final String GENERIC_ATTRIBUTE_SERVICE = "00001801-0000-1000-8000-00805f9b34fb";
}
