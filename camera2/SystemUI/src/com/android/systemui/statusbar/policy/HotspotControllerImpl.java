/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

public class HotspotControllerImpl implements HotspotController {

    private static final String TAG = "HotspotController";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    // Keep these in sync with Settings TetherService.java
    public static final String EXTRA_ADD_TETHER_TYPE = "extraAddTetherType";
    public static final String EXTRA_SET_ALARM = "extraSetAlarm";
    public static final String EXTRA_RUN_PROVISION = "extraRunProvision";
    public static final String EXTRA_ENABLE_WIFI_TETHER = "extraEnableWifiTether";
    // Keep this in sync with Settings TetherSettings.java
    public static final int WIFI_TETHERING = 0;

    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
    private final Receiver mReceiver = new Receiver();
    private final Context mContext;
    private final WifiManager mWifiManager;
    private final ConnectivityManager mConnectivityManager;
    private boolean supportBtWifiSoftApCoexist = true;
    private BluetoothAdapter mBluetoothAdapter;
    //SPRD:fixbug434711 make the statusbar close when click hotspot.
    private PhoneStatusBar mBar;

    public HotspotControllerImpl(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (SystemProperties.get("ro.btwifisoftap.coexist", "true").equals(
                "false")) {
            if (mBluetoothAdapter == null) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }
            supportBtWifiSoftApCoexist = false;
        }
    }

    public void addCallback(Callback callback) {
        if (callback == null || mCallbacks.contains(callback)) return;
        if (DEBUG) Log.d(TAG, "addCallback " + callback);
        mCallbacks.add(callback);
        mReceiver.setListening(!mCallbacks.isEmpty());
    }

    public void removeCallback(Callback callback) {
        if (callback == null) return;
        if (DEBUG) Log.d(TAG, "removeCallback " + callback);
        mCallbacks.remove(callback);
        mReceiver.setListening(!mCallbacks.isEmpty());
    }

    @Override
    public boolean isHotspotEnabled() {
        return mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED;
    }

    @Override
    public boolean isHotspotSupported() {
        final boolean isSecondaryUser = ActivityManager.getCurrentUser() != UserHandle.USER_OWNER;
        return !isSecondaryUser && mConnectivityManager.isTetheringSupported();
    }

    @Override
    public boolean isProvisioningNeeded() {
        // Keep in sync with other usage of config_mobile_hotspot_provision_app.
        // TetherSettings#isProvisioningNeeded and
        // ConnectivityManager#enforceTetherChangePermission
        String[] provisionApp = mContext.getResources().getStringArray(
                com.android.internal.R.array.config_mobile_hotspot_provision_app);
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)
                || provisionApp == null) {
            return false;
        }
        return (provisionApp.length == 2);
    }

    @Override
    public void setHotspotEnabled(boolean enabled) {
        final ContentResolver cr = mContext.getContentResolver();
        // Call provisioning app which is called when enabling Tethering from Settings
        if (enabled) {
            if (isProvisioningNeeded()) {
                String tetherEnable = mContext.getResources().getString(
                        com.android.internal.R.string.config_wifi_tether_enable);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_ADD_TETHER_TYPE, WIFI_TETHERING);
                intent.putExtra(EXTRA_SET_ALARM, true);
                intent.putExtra(EXTRA_RUN_PROVISION, true);
                intent.putExtra(EXTRA_ENABLE_WIFI_TETHER, true);
                intent.setComponent(ComponentName.unflattenFromString(tetherEnable));
                mContext.startServiceAsUser(intent, UserHandle.CURRENT);
            } else {
                // This needs to be kept up to date with Settings (WifiApEnabler.setSoftapEnabled)
                // in case it is turned on in settings and off in qs (or vice versa).
                // Disable Wifi if enabling tethering.
                if (!supportBtWifiSoftApCoexist) {
                    int btState = mBluetoothAdapter.getState();
                    if(mBar!=null&&enabled && ((btState == BluetoothAdapter.STATE_ON) || (btState == BluetoothAdapter.STATE_TURNING_ON))) {
                        //SPRD:fixbug434711 make the statusbar close when click hotspot.
                        mBar.animateCollapseQuickSettings();
                        Toast.makeText(mContext, com.android.systemui.R.string.softap_bt_cannot_coexist, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Settings.Global.putInt(cr, Settings.Global.SOFTAP_ENABLING_OR_ENABLED, 1);
                }

                /* SPRD: Modify Bug 451875 show tip for wifi hotspot by mobile data disabled @{ */
                if (mBar!=null && enabled && !mConnectivityManager.getMobileDataEnabled()) {
                    mBar.animateCollapseQuickSettings();
                    showAlertForMobileDataNeedEnabled();
                }
                /* @} */
                int wifiState = mWifiManager.getWifiState();
                if ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
                        (wifiState == WifiManager.WIFI_STATE_ENABLED)) {
                    mWifiManager.setWifiEnabled(false);
                    Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
                }
                mWifiManager.setWifiApEnabled(null, true);
            }
        } else {
            mWifiManager.setWifiApEnabled(null, false);
            if (Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE, 0) == 1) {
                mWifiManager.setWifiEnabled(true);
                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
            }
        }
    }

    private void fireCallback(boolean isEnabled) {
        for (Callback callback : mCallbacks) {
            callback.onHotspotChanged(isEnabled);
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        public void setListening(boolean listening) {
            if (listening && !mRegistered) {
                if (DEBUG) Log.d(TAG, "Registering receiver");
                final IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
                mContext.registerReceiver(this, filter);
                mRegistered = true;
            } else if (!listening && mRegistered) {
                if (DEBUG) Log.d(TAG, "Unregistering receiver");
                mContext.unregisterReceiver(this);
                mRegistered = false;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive " + intent.getAction());
            fireCallback(isHotspotEnabled());
        }
    }

    /* SPRD:fixbug434711 make the statusbar close when click hotspot @{ */
    public void setbar(PhoneStatusBar bar) {
        // TODO Auto-generated method stub
        mBar=bar;
    }
    /* @} */

    /* SPRD: Modify Bug 451875 show tip for wifi hotspot by mobile data disabled @{ */
    private void showAlertForMobileDataNeedEnabled() {
        Toast.makeText(mContext, com.android.systemui.R.string.softap_need_mobile_data_enabled, Toast.LENGTH_LONG)
                .show();
    }
    /* @} */
}
