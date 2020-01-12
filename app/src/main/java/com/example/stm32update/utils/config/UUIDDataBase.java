package com.example.stm32update.utils.config;

import java.util.UUID;

public class UUIDDataBase {
    public final static UUID UUID_OTA_UPDATE_CHARACTERISTIC = UUID
            .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
}
