package com.micronet.alwaysonwifihotspot;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

/**
 * Created by brigham.diaz on 12/12/2016.
 *
 */
public class WiFiApManager {
    /* Wifi AP values and intent string from obc_android SDK */
    public static final int WIFI_AP_STATE_DISABLING = 10; // Wi-Fi AP is currently being disabled. The state will change to WIFI_AP_STATE_DISABLED if it finishes successfully.
    public static final int WIFI_AP_STATE_DISABLED = 11; // Wi-Fi AP is disabled.
    public static final int WIFI_AP_STATE_ENABLING = 12; // Wi-Fi AP is currently being enabled. The state will change to WIFI_AP_STATE_ENABLED if it finishes successfully.
    public static final int WIFI_AP_STATE_ENABLED = 13; // Wi-Fi AP is enabled.
    public static final int WIFI_AP_STATE_FAILED = 14;  // Wi-Fi AP is in a failed state. This state will occur when an error occurs during enabling or disabling

    // Broadcast intent action indicating that Wi-Fi AP has been enabled, disabled, enabling, disabling, or failed.
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";


    public static boolean isWiFiApOn(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);

        try {
            // access isWifiApEnabled method by reflection
            Method isWifiApEnabledMethod = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            isWifiApEnabledMethod.setAccessible(true);
            return (Boolean) isWifiApEnabledMethod.invoke(wifiManager);
        } catch (Throwable throwable) {
        }

        return false;
    }


    /**
     * Start or stop the AccessPoint using the saved configuration.
     * Uses reflection to access System API
     *
     * @param context Activity context
     * @param enabled true to enable and false to disable
     * @return {@code true} if the operation succeeds, {@code false} otherwise
     */
    public static boolean setWiFiApState(Context context, boolean enabled) {
        /**
         * Method overview
         * 1. Get WifiManager
         * 2. Get Wifi AP state
         * 3. Compare enabled argument with AP state.
         *      - return if states match. No need to enable Wifi AP if it's already running, could cause interruption?
         * 4. If enabling AP, then first disable WiFi radio using WifiManager
         * 5. Get the current Wifi AP configuration
         * 6. Get a handle to setWifiApEnabled
         * 6. Pass enabled and Wifi AP configuration as argument values to setWifiApEnabled for WifiManager
         */
        Boolean result = false;

        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            // 1. TODO: first check Wifi AP state and do not repeat action of current state,
            // i.e. do not turn on AP if it's already enabled and do not turn off AP if it's already disabled
            // Will need to use reflection to call isWifiApEnabled or getWifiApState and check against WIFI_AP_STATE_XXXXX
            // SystemAPI: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/wifi/java/android/net/wifi/WifiManager.java

            // 2. TODO: turn off WiFi before enabling hotspot

            // using reflection to get method access for getWifiApConfiguration and setWifiApEnabled
            Method getWifiApMethod = wifiManager.getClass().getDeclaredMethod("getWifiApConfiguration");
            getWifiApMethod.setAccessible(true);
            WifiConfiguration wifiApConfig = (WifiConfiguration) getWifiApMethod.invoke(wifiManager);
            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            // pass in the ObjectReceiver, WifiConfiguration, boolean
            result = (Boolean) setWifiApMethod.invoke(wifiManager, wifiApConfig, enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
