package com.micronet.alwaysonwifihotspot;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by brigham.diaz on 12/12/2016.
 *
 * Service monitors the state of the Wifi AP and re-enables the tethering if it's
 * ever turned off (disabled). The monitoring is performed using a receiver and handler. The
 * receiver immediately enables the AP if it's ever disabled. The handler monitors the WifiApState
 * and enables it in a disabled state. The handler is more of a fallback in case the receiver failed
 * to enable hotspot or it didn't receive change.
 */
public class AlwaysOnWiFiHotspotService extends Service {
    private final BroadcastReceiver wifiApStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // first make sure that action received is wifi ap
            if (WiFiApManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                // get Wi-Fi Hotspot state
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WiFiApManager.WIFI_AP_STATE_FAILED);
                Log.d(this.toString(), "wifiApState=" + state);
                if (state == WiFiApManager.WIFI_AP_STATE_DISABLED) {
                    // re-enable Wifi AP
                    boolean result = WiFiApManager.setWiFiApState(context, true);
                    Log.d(this.toString(), "setWiFiApState=" + result);
                }
            }
        }
    };

    /**
     * The handler should post to a Runnable when it's first called and afterwards, every 60 seconds.
     * If AP state is WIFI_AP_STATE_DISABLED or WIFI_AP_STATE_FAILED then
     *  attempt to re-enable hotspot.
     *  set post for 60 seconds after
     * If the AP state is WIFI_AP_STATE_ENABLED then
     *  do nothing
     *  set post for 60 seconds
     * If the AP state is disabling or enabling then
     *  do nothing
     *  set post for 10 seconds
     */
    private Handler wifiApHandler;
    private int wifiApvalue;
    private final long SIXTY_SECONDS = 60000;
    private final long TEN_SECONDS = 10000;
    private Context context;

    @Override
    public void onCreate() {
        // The service is being created
        IntentFilter intentFilter = new IntentFilter(WiFiApManager.WIFI_AP_STATE_CHANGED_ACTION);
        registerReceiver(wifiApStatusReceiver, intentFilter);
        // TODO: create handler and call post
        context=this;
        if (wifiApHandler == null) {
            wifiApHandler = new Handler(Looper.myLooper());
            wifiApHandler.post(wifiApCheck);
        }

    }

    final Runnable wifiApCheck = new Runnable() {
        @Override
        public void run() {
            wifiApvalue= WiFiApManager.getWifiApState(context);

            try {
                switch (wifiApvalue){
                    case WiFiApManager.WIFI_AP_STATE_DISABLING: //WiFi AP is currently disabling
                        //Doing Nothing and Setting post to 10s
                        wifiApHandler.postDelayed(this,TEN_SECONDS);
                    case WiFiApManager.WIFI_AP_STATE_DISABLED: //WiFi AP is currently disabled
                        //Re-enable the WiFi Hotspot state
                         WiFiApManager.setWiFiApState(context, true);
                           /* WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                            wifi.setWifiEnabled(true);*/ //To disable Wi-Fi
                        //Seting post to 60s
                        wifiApHandler.postDelayed(this,SIXTY_SECONDS);
                        break;
                    case WiFiApManager.WIFI_AP_STATE_ENABLING: //Wifi AP is currently enabling
                        //Do nothing Setting post to 10s
                        wifiApHandler.postDelayed(this,TEN_SECONDS);
                        break;
                    case WiFiApManager.WIFI_AP_STATE_ENABLED:// WiFi AP is currently enabled
                        // Do nothing
                        wifiApHandler.postDelayed(this,SIXTY_SECONDS);
                        break ;
                    case WiFiApManager.WIFI_AP_STATE_FAILED://WiFi Ap failed
                        // Re enable the WiFi Hotspot state
                        WiFiApManager.setWiFiApState(context, true);
                        wifiApHandler.postDelayed(this,SIXTY_SECONDS);
                        break;
                    default:
                        break;
                }
            }
            catch( Exception e) {
                Log.d(TAG, "run: bh");
            }
            wifiApHandler.postDelayed(this, 60000);
        }
    };
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(wifiApStatusReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return null;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return false;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }
}
