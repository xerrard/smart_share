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
 * limitations under the License
 */

package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.List;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.FlashlightController;
import android.telephony.TelephonyManagerSprd;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;

/** Quick settings tile: Control flashlight **/
public class FlashlightTile extends QSTile<QSTile.BooleanState> implements
        FlashlightController.FlashlightListener {

    /** Grace period for which we consider the flashlight
     * still available because it was recently on. */
    private static final long RECENTLY_ON_DURATION_MILLIS = 500;

    private final AnimationIcon mEnable
            = new AnimationIcon(R.drawable.ic_signal_flashlight_enable_animation);
    private final AnimationIcon mDisable
            = new AnimationIcon(R.drawable.ic_signal_flashlight_disable_animation);
    private final FlashlightController mFlashlightController;
    private long mWasLastOn;
    /* SPRD: bugfix 427428 Power off flishlight when coming a call. @{*/
    private TelephonyManager mTelephonyManager;
    private PhoneStateListener[] mPhoneStateListener;
    private SubscriptionManager  mSubscriptionManager;
    private boolean mTempState = false;
    /* @} */
    private TelephonyManagerSprd mTelephonyManagerSprd;
    private final String FLASH_LIGHT_ON ="com.huaqin.flashlight.on";
    private final String FLASH_LIGHT_OFF ="com.huaqin.flashlight.off";
    private final String CAMERA_TURN_FLASHLIGHT_OFF = "com.huaqin.camera.turn.flashlight.off";
    private final String HQ_FLASHLIGHT_SHOW = "HQ_FLASHLIGHT_SHOW";

    public FlashlightTile(Host host) {
        super(host);
        mFlashlightController = host.getFlashlightController();
        mFlashlightController.addListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(FLASH_LIGHT_ON);
        filter.addAction(FLASH_LIGHT_OFF);
        filter.addAction(CAMERA_TURN_FLASHLIGHT_OFF);
        filter.addAction(HQ_FLASHLIGHT_SHOW);
        mContext.registerReceiver(mReceiver, filter);
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        //SPRD: bugfix 427428 Power off flishlight when coming a call. 
        unregisterPhoneStateListener();
        mFlashlightController.removeListener(this);
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
    }

    @Override
    protected void handleUserSwitch(int newUserId) {
    }

    @Override
    protected void handleClick() {
        if (ActivityManager.isUserAMonkey()) {
            return;
        }
        /* SPRD: bugfix 427428 Power off flishlight when coming a call. @{*/
        if(mPhoneStateListener == null){
            Log.d(TAG,"FlashlightTile handleClick registerPhoneStateListener.");
            registerPhoneStateListener();
        }
        /* @} */
        boolean newState = !mState.value;
        if(newState)
        {
            setFlashlightOnBroadcast();
        }
        else
        {
            setFlashlightOffBroadcast();
        }
        mFlashlightController.setFlashlight(newState);
        refreshState(newState ? UserBoolean.USER_TRUE : UserBoolean.USER_FALSE);
    }
    
    private BroadcastReceiver  mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
                if(action.equals(FLASH_LIGHT_ON)){
                    boolean newState = true;
                    mFlashlightController.setFlashlight(newState);
                    refreshState(newState ? UserBoolean.USER_TRUE : UserBoolean.USER_FALSE);
                }else if(action.equals(FLASH_LIGHT_OFF) || action.equals(CAMERA_TURN_FLASHLIGHT_OFF)){
                    boolean newState = false;
                    mFlashlightController.setFlashlight(newState);
                    refreshState(newState ? UserBoolean.USER_TRUE : UserBoolean.USER_FALSE);
                }else if(action.equals(HQ_FLASHLIGHT_SHOW)){
                    if(mState.value){
                        setFlashlightOnBroadcast();
                    }
                }
            }
        };

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        /* SPRD: bugfix 427428 Power off flishlight when coming a call. @{*/
        if(mTempState == true){
            state.value = false;
            mTempState = false;
        }
        /* @} */
        if (state.value) {
            mWasLastOn = SystemClock.uptimeMillis();
        }

        if (arg instanceof UserBoolean) {
            state.value = ((UserBoolean) arg).value;
        }

        if (!state.value && mWasLastOn != 0) {
            if (SystemClock.uptimeMillis() > mWasLastOn + RECENTLY_ON_DURATION_MILLIS) {
                mWasLastOn = 0;
            } else {
                mHandler.removeCallbacks(mRecentlyOnTimeout);
                mHandler.postAtTime(mRecentlyOnTimeout, mWasLastOn + RECENTLY_ON_DURATION_MILLIS);
            }
        }

        // Always show the tile when the flashlight is or was recently on. This is needed because
        // the camera is not available while it is being used for the flashlight.
        state.visible = mWasLastOn != 0 || mFlashlightController.isAvailable();
        state.label = mHost.getContext().getString(R.string.quick_settings_flashlight_label);
        final AnimationIcon icon = state.value ? mEnable : mDisable;
        icon.setAllowAnimation(arg instanceof UserBoolean && ((UserBoolean) arg).userInitiated);
        state.icon = icon;
        int onOrOffId = state.value
                ? R.string.accessibility_quick_settings_flashlight_on
                : R.string.accessibility_quick_settings_flashlight_off;
        state.contentDescription = mContext.getString(onOrOffId);
    }
    
    public void setFlashlightOnBroadcast()
    {
        Intent intent = new Intent();
        intent.setAction("com.huaqin.quicksetting.flashlight.on");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mContext.sendBroadcast(intent);
    }
    
    public void setFlashlightOffBroadcast()
    {
        Intent intent = new Intent();
        intent.setAction("com.huaqin.quicksetting.flashlight.off");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mContext.sendBroadcast(intent);
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_off);
        }
    }

    @Override
    public void onFlashlightOff() {
        refreshState(UserBoolean.BACKGROUND_FALSE);
    }

    @Override
    public void onFlashlightError() {
        refreshState(UserBoolean.BACKGROUND_FALSE);
    }

    @Override
    public void onFlashlightAvailabilityChanged(boolean available) {
        refreshState();
    }

    private Runnable mRecentlyOnTimeout = new Runnable() {
        @Override
        public void run() {
            refreshState();
        }
    };
    /* SPRD: bugfix 427428 Power off flishlight when coming a call. @{*/
    private void registerPhoneStateListener() {
        mTelephonyManager = TelephonyManager.from(mContext);
        mSubscriptionManager = SubscriptionManager.from(mContext);
        // SPRD: bugfix 436636 When setting incoming calls, the flashlight's state.
        mTelephonyManagerSprd = (TelephonyManagerSprd) TelephonyManager.from(mContext);
        List<SubscriptionInfo> subscriptions = mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subscriptions == null){
            Log.d(TAG, "subscriptions is null ");
            return;
        }
        final int num = subscriptions.size();
        if (num <= 0)
            return;
        mPhoneStateListener = new PhoneStateListener[num];
        for (int i = 0; i < num; i++) {
            mPhoneStateListener[i] = new MobilePhoneStateListener(subscriptions.get(i)
                    .getSubscriptionId());
            mTelephonyManager.listen(mPhoneStateListener[i], PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void unregisterPhoneStateListener() {
        if (mPhoneStateListener != null) {
            Log.d(TAG,"FlashlightTile unregisterPhoneStateListener mPhoneStateListener = " + mPhoneStateListener);
            for (int i = 0; i < mPhoneStateListener.length; i++) {
                mTelephonyManager.listen(mPhoneStateListener[i], 0);
                mPhoneStateListener[i] = null;
            }
        }
    }
    class MobilePhoneStateListener extends PhoneStateListener {
        public MobilePhoneStateListener(int subId) {
            super(subId);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            /* SPRD: bugfix 436636 When setting incoming calls, the flashlight's state. @{ */
            if (state == TelephonyManager.CALL_STATE_RINGING && mTelephonyManagerSprd.isVideoCall(state)) {
                Log.d(TAG, "onCallStateChanged : closeFlashlight");
                mTempState = true;
                mFlashlightController.setFlashlight(false);
                refreshState(false);
            }
            /* @} */
        }
    }
    /* @} */
}
