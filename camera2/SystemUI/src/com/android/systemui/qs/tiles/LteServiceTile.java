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

package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManagerSprd;
import android.telephony.TelephonyManagerSprd.RadioFeatures;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.TelephonyIntents;
import com.android.systemui.R;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTileView;

/** Quick settings tile: Lte service **/
public class LteServiceTile extends QSTile<QSTile.BooleanState> {

    private final GlobalSetting mLteEnableSetting;
    private TelephonyManagerSprd mTelephonyManager;
    private boolean mListening;
    private boolean mLteEnabled;
    private boolean mLteAvailable;
    private QSTileView mQSTileView;
    private static final int PREFERRED_NETWORK_MODE_4G_3G_2G = 0;
    private static final int PREFERRED_NETWORK_MODE_4G_ONLY=2;

    public LteServiceTile(Host host) {
        super(host);
        mTelephonyManager = (TelephonyManagerSprd) TelephonyManager.from(mContext);

        mLteEnableSetting = new GlobalSetting(mContext,mHandler,Global.PREFERRED_NETWORK_MODE_TYPE) {
            @Override
            protected void handleValueChanged(int value) {
                Log.d(TAG, "handleValueChanged: value = " + value);
                handleRefreshState(value);
            }
        };

    }

    public QSTileView createTileView(Context context) {
        mQSTileView = new QSTileView(context);
        return mQSTileView;
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    /** SPRD: during the call,user can't switch 4G service. @{ */
    private boolean isInCall() {
        SubscriptionManager subManager = SubscriptionManager.from(mContext);
        int activeSubId[] = subManager.getActiveSubscriptionIdList();
        for (int subId : activeSubId) {
            int state = mTelephonyManager.getCallState(subId);
            if (state != TelephonyManager.CALL_STATE_IDLE) {
                return true;
            }
        }
        return false;
    }
    /** @} */

    @Override
    public void handleClick() {
        if (mTelephonyManager.isAirplaneModeOn()) {
            mHost.collapsePanels();
            Toast.makeText(mContext, R.string.lte_service_error_airplane, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        /** SPRD: during the call,user can't switch 4G service. @{ */
        if (isInCall()) {
            mHost.collapsePanels();
            Toast.makeText(mContext, R.string.lte_service_error_incall, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        /** @} */

        Log.d(TAG, "handleClick: mLteAvailable = " + mLteAvailable);

        if (!mLteAvailable) {
            return;
        } else {
            setLteEnabled();
        }
    }

    private void setLteEnabled() {
        Log.d(TAG, "setLteEnabled: " + !mState.value);
        mLteEnabled = !mState.value;
        mTelephonyManager.setLteEnabled(mLteEnabled);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {

        if (!mTelephonyManager.isAirplaneModeOn()) {
             updateLteEnabledState();
        }

        state.value = mLteEnabled;
        state.visible = true;
        state.label = mContext.getString(R.string.quick_settings_lte_service_label);

        int primaryCard = mTelephonyManager.getPrimaryCard();
        boolean isPrimaryCardUsim = mTelephonyManager.hasUsimCard(primaryCard);
        boolean isPrimaryCardReady = mTelephonyManager.isSimStandby(primaryCard) &&
                mTelephonyManager.getSimState(primaryCard) == TelephonyManager.SIM_STATE_READY;
        mLteAvailable = isPrimaryCardUsim && isPrimaryCardReady;

        Log.d(TAG, "handleUpdateState: mLteEnabled = " + mLteEnabled +
                " isPrimaryCardUsim = " + isPrimaryCardUsim +
                " isPrimaryCardReady = " + isPrimaryCardReady);

        if (mLteEnabled && mLteAvailable
                && !mTelephonyManager.isAirplaneModeOn()) {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_4g_on_sprd);
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_lte_on);
        } else {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_4g_off_sprd);
            state.contentDescription =  mContext.getString(
                    R.string.accessibility_quick_settings_lte_off);
        }
    }

    public void updateLteEnabledState() {
        final int primaryCard = mTelephonyManager.getPrimaryCard();
        boolean lteEnabled = false;
        Log.d(TAG, "updateLteEnabledState: primaryCard = " + primaryCard);

        if (SubscriptionManager.isValidPhoneId(primaryCard)) {
            /* SPRD: query LTE status from AP instead of from BP with AT commands,
             * as the latter method may cause ANR for multiple blocked thread.
             * See bug:438813{@ */
            lteEnabled = getLteEnable(primaryCard);
            Log.d(TAG, "updateLteEnabledState: lteEnabled = " + lteEnabled + ", mLteEnabled = " + mLteEnabled);

            if (mLteEnabled != lteEnabled) {
                mState.value = mLteEnabled = lteEnabled;
                if (mQSTileView != null) {
                    mQSTileView.onStateChanged(mState);
                }
            }
            /* @} */
        }
    }

    /** SPRD: Set NetworkMode for testMode. @{ */
    private boolean getLteEnable(int phoneId) {
        int mode = Settings.Global.getInt(mContext.getContentResolver(),
                    Settings.Global.PREFERRED_NETWORK_MODE_TYPE,-1);
        if (mode == PREFERRED_NETWORK_MODE_4G_3G_2G
                || mode == PREFERRED_NETWORK_MODE_4G_ONLY) {
            return true;
        }
        return false;
    }
    /** @} */

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_lte_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_lte_changed_off);
        }
    }

    public void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        if (listening) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
            mContext.registerReceiver(mReceiver, filter);
        } else {
            mContext.unregisterReceiver(mReceiver);
        }
        mLteEnableSetting.setListening(listening);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())
                    || Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                refreshState();
            }
        }
    };
}
