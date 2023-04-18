/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.lize_app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    public static HashMap<String, String> attributes = new HashMap();
    public static ArrayList<String> subscribed_UUIDs = new ArrayList<>();
    public static ArrayList<String> input_UUIDs = new ArrayList<>();

    static {

        // ================================================================================
        // attributes

        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");

        // C3 & C6
        attributes.put("0000fff3-0000-1000-8000-00805f9b34fb", "C3");
        attributes.put("0000fff6-0000-1000-8000-00805f9b34fb", "C6");

        // ================================================================================
        // subscribed
        subscribed_UUIDs.add("0000fff6-0000-1000-8000-00805f9b34fb");

        // ================================================================================
        // input
        input_UUIDs.add("0000fff3-0000-1000-8000-00805f9b34fb");

    }

    public static String lookup(UUID uuid, String defaultName) {
        return lookup(uuid.toString(), defaultName);
    }
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

    public static boolean isIncluded(int type, String UUID) {
        ArrayList<String> list;
        switch(type){
            case 0:
                list = subscribed_UUIDs;
                break;
            case 1:
                list = input_UUIDs;
                break;
            default:
                list = new ArrayList<>();
        }
        for (String u:list) {
            if(u.equals(UUID)) {
                return true;
            }
        }
        return false;
    }
    public static boolean checkSubscribed(String UUID) {
        return isIncluded(0, UUID);
    }
    public static boolean checkInput(String UUID) {
        return isIncluded(1, UUID);
    }
}
