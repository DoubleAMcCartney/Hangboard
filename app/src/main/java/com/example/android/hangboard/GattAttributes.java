/*
 * This class includes GATT attributes required for connection to the HAG Board.
 */

package com.example.android.hangboard;

import java.util.HashMap;

public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HAG_CURRENT = "53E70003-D192-47A0-8F06-35EC90C73A3A";
    public static String HAG_DESIRED = "53E70002-D192-47A0-8F06-35EC90C73A3A";
    public static String HAG_MOVE = "53E70004-D192-47A0-8F06-35EC90C73A3A";
    public static String HAG_SERVICE = "53E70001-D192-47A0-8F06-35EC90C73A3A";
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