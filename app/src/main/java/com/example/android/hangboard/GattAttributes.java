/*
 * This class includes GATT attributes required for connection to the HAG Board.
 */

package com.example.android.hangboard;

import java.util.HashMap;

public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HAG_CURRENT = "00009fd2-0000-1000-8000-00805f9b34fb";
    public static String HAG_DESIRED = "00009fd1-0000-1000-8000-00805f9b34fb";
    public static String HAG_MOVE = "00009fd3-0000-1000-8000-00805f9b34fb";
    public static String HAG_SERVICE = "00003a3a-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Services.
        attributes.put(HAG_SERVICE, "HAG Board Service");
        // Characteristics.
        attributes.put(HAG_CURRENT, "HAG Board Current Measurements");
        attributes.put(HAG_DESIRED, "HAG Board Desired Measurements");
        attributes.put(HAG_MOVE, "HAG Board Moving Status");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}