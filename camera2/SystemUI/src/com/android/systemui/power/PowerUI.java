/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.systemui.power;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import android.view.WindowManager;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

public class PowerUI extends SystemUI {
    static final String TAG = "PowerUI";
    static final boolean DEBUG = true || Log.isLoggable(TAG, Log.DEBUG);

    private final Handler mHandler = new Handler();
    private final Receiver mReceiver = new Receiver();

    private PowerManager mPowerManager;
    private WarningsUI mWarnings;
    private int mBatteryLevel = 100;
    private int mBatteryStatus = BatteryManager.BATTERY_STATUS_UNKNOWN;
    private int mPlugType = 0;
    private int mInvalidCharger = 0;

    private int mBatteryHealth = BatteryManager.BATTERY_HEALTH_UNKNOWN;
    private boolean mTempLowWarningFlag = false;
    private boolean mTempHighWarningFlag = false;

    private int mLowBatteryAlertCloseLevel;
    private final int[] mLowBatteryReminderLevels = new int[2];

    private long mScreenOffTime = -1;
    private boolean mShowNotification = false;
    AlertDialog mTempLowDialog;
    AlertDialog mTempHighDialog;
    AlertDialog mVoltageHighDialog;
    private boolean mPlaySoundAndDialogForTemp = true;
    private static final String SOUND_PATH_FOR_BATTERY = "/system/media/audio/notifications/Tethys.ogg";

    public void start() {
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mScreenOffTime = mPowerManager.isScreenOn() ? -1 : SystemClock.elapsedRealtime();
        mWarnings = new PowerNotificationWarnings(mContext, getComponent(PhoneStatusBar.class));

        ContentObserver obs = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                updateBatteryWarningLevels();
            }
        };
        final ContentResolver resolver = mContext.getContentResolver();
        resolver.registerContentObserver(Settings.Global.getUriFor(
                Settings.Global.LOW_POWER_MODE_TRIGGER_LEVEL),
                false, obs, UserHandle.USER_ALL);
        updateBatteryWarningLevels();
        mReceiver.init();
    }

    private void setSaverMode(boolean mode) {
        mWarnings.showSaverMode(mode);
    }

    void updateBatteryWarningLevels() {
        int critLevel = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_criticalBatteryWarningLevel);

        final ContentResolver resolver = mContext.getContentResolver();
        int defWarnLevel = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lowBatteryWarningLevel);
        int warnLevel = Settings.Global.getInt(resolver,
                Settings.Global.LOW_POWER_MODE_TRIGGER_LEVEL, defWarnLevel);
        if (warnLevel == 0) {
            warnLevel = defWarnLevel;
        }
        if (warnLevel < critLevel) {
            warnLevel = critLevel;
        }

        mLowBatteryReminderLevels[0] = warnLevel;
        mLowBatteryReminderLevels[1] = critLevel;
        mLowBatteryAlertCloseLevel = mLowBatteryReminderLevels[0]
                + mContext.getResources().getInteger(
                        com.android.internal.R.integer.config_lowBatteryCloseWarningBump);
    }

    /**
     * Buckets the battery level.
     *
     * The code in this function is a little weird because I couldn't comprehend
     * the bucket going up when the battery level was going down. --joeo
     *
     * 1 means that the battery is "ok"
     * 0 means that the battery is between "ok" and what we should warn about.
     * less than 0 means that the battery is low
     */
    private int findBatteryLevelBucket(int level) {
        if (level >= mLowBatteryAlertCloseLevel) {
            return 1;
        }
        if (level > mLowBatteryReminderLevels[0]) {
            return 0;
        }
        final int N = mLowBatteryReminderLevels.length;
        for (int i=N-1; i>=0; i--) {
            if (level <= mLowBatteryReminderLevels[i]) {
                return -1-i;
            }
        }
        throw new RuntimeException("not possible!");
    }

    private final class Receiver extends BroadcastReceiver {

        public void init() {
            // Register for Intent broadcasts for...
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_USER_SWITCHED);
            filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGING);
            filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
            mContext.registerReceiver(this, filter, null, mHandler);
            updateSaverMode();
        }

        private void updateSaverMode() {
            setSaverMode(mPowerManager.isPowerSaveMode());
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                final int oldBatteryLevel = mBatteryLevel;
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                final int oldBatteryStatus = mBatteryStatus;
                mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
                final int oldPlugType = mPlugType;
                mPlugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 1);
                final int oldInvalidCharger = mInvalidCharger;
                mInvalidCharger = intent.getIntExtra(BatteryManager.EXTRA_INVALID_CHARGER, 0);

                final int oldBatteryHealth = mBatteryHealth;
                mBatteryHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);

                final boolean plugged = mPlugType != 0;
                final boolean oldPlugged = oldPlugType != 0;

                int oldBucket = findBatteryLevelBucket(oldBatteryLevel);
                int bucket = findBatteryLevelBucket(mBatteryLevel);

                if (DEBUG) {
                    Slog.d(TAG, "buckets   ....." + mLowBatteryAlertCloseLevel
                            + " .. " + mLowBatteryReminderLevels[0]
                            + " .. " + mLowBatteryReminderLevels[1]);
                    Slog.d(TAG, "level          " + oldBatteryLevel + " --> " + mBatteryLevel);
                    Slog.d(TAG, "status         " + oldBatteryStatus + " --> " + mBatteryStatus);
                    Slog.d(TAG, "plugType       " + oldPlugType + " --> " + mPlugType);
                    Slog.d(TAG, "invalidCharger " + oldInvalidCharger + " --> " + mInvalidCharger);
                    Slog.d(TAG, "bucket         " + oldBucket + " --> " + bucket);
                    Slog.d(TAG, "plugged        " + oldPlugged + " --> " + plugged);
                    Slog.d(TAG, "health        " + oldBatteryHealth + " --> " + mBatteryHealth);
                }

                mWarnings.update(mBatteryLevel, bucket, mScreenOffTime);
                if (oldInvalidCharger == 0 && mInvalidCharger != 0) {
                    Slog.d(TAG, "showing invalid charger warning");
                    mWarnings.showInvalidChargerWarning();
                    return;
                } else if (oldInvalidCharger != 0 && mInvalidCharger == 0) {
                    mWarnings.dismissInvalidChargerWarning();
                } else if (mWarnings.isInvalidChargerWarningShowing()) {
                    // if invalid charger is showing, don't show low battery
                    return;
                }

                if (plugged) {
                    if( oldBatteryHealth != mBatteryHealth && mBatteryHealth != BatteryManager.BATTERY_HEALTH_UNKNOWN) {
                        if(mBatteryHealth == BatteryManager.BATTERY_HEALTH_COLD) {
                            if(!com.huaqin.common.featureoption.FeatureOption.HQ_SPRD_MMX_REMOVE_LOWTEP_Dialog){
                                showTempLowWarning();
                            }
                        } else if(oldBatteryHealth == BatteryManager.BATTERY_HEALTH_COLD) {
                            if(!com.huaqin.common.featureoption.FeatureOption.HQ_SPRD_MMX_REMOVE_LOWTEP_Dialog){
                                dismissingLowWarning();
                            }
                        }

                        if(mBatteryHealth == BatteryManager.BATTERY_HEALTH_OVERHEAT) {
                            showTempHighWarning();
                        } else if(oldBatteryHealth == BatteryManager.BATTERY_HEALTH_OVERHEAT) {
                            dismissingHighWarning();
                        }
                    }
                } else {
                    if(oldPlugged) {
                        if(!com.huaqin.common.featureoption.FeatureOption.HQ_SPRD_MMX_REMOVE_LOWTEP_Dialog){
                            dismissingLowWarning();
                         }
                        dismissingHighWarning();
                    }
                }

                if (!plugged
                        && (bucket < oldBucket || oldPlugged)
                        && mBatteryStatus != BatteryManager.BATTERY_STATUS_UNKNOWN
                        && bucket < 0) {
                    // only play SFX when the dialog comes up or the bucket changes
                    final boolean playSound = bucket != oldBucket || oldPlugged;
                    mWarnings.showLowBatteryWarning(playSound);
                } else if (plugged || (bucket > oldBucket && bucket > 0)) {
                    mWarnings.dismissLowBatteryWarning();
                } else {
                    mWarnings.updateLowBatteryWarning();
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenOffTime = SystemClock.elapsedRealtime();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenOffTime = -1;
            } else if (Intent.ACTION_USER_SWITCHED.equals(action)) {
                mWarnings.userSwitched();
            } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(action)) {
                updateSaverMode();
            } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGING.equals(action)) {
                setSaverMode(intent.getBooleanExtra(PowerManager.EXTRA_POWER_SAVE_MODE, false));
            } else {
                Slog.w(TAG, "unknown intent: " + intent);
            }
        }
    };

    private void showTempLowWarning() {
        if (mShowNotification) {
            mWarnings.showTempOverLowWarning();
        } else {
            if (mPlaySoundAndDialogForTemp) {
                showTempOverLowWarningDialog();
                playBatteryTempWarningSound();
                mPlaySoundAndDialogForTemp = false;
            }
        }
    }

    private void dismissingLowWarning() {
        if (mShowNotification) {
            mWarnings.dismissTempOverLowWarning();
        } else {
            dismissTempOverLowWarningDialog();
            mPlaySoundAndDialogForTemp = true;
        }
    }

    private void showTempHighWarning() {
        if (mShowNotification) {
            mWarnings.showTempOverHighWarning();
        } else {
            if (mPlaySoundAndDialogForTemp) {
                showTempOverHighWarningDialog();
                playBatteryTempWarningSound();
                mPlaySoundAndDialogForTemp = false;
            }
        }
    }

    private void dismissingHighWarning() {
        if (mShowNotification) {
            mWarnings.dismissTempOverHighWarning();
        } else {
            dismissTempOverHighWarningDialog();
            mPlaySoundAndDialogForTemp = true;
        }
    }

    private void showTempOverLowWarningDialog() {
        Slog.d(TAG, "showing temperature overLow dialog");

        AlertDialog.Builder b = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
        b.setTitle(R.string.temperature_overlow_title);
        b.setCancelable(true);
        b.setMessage(R.string.temperature_overlow_text);
        b.setIconAttribute(android.R.attr.alertDialogIcon);
        b.setPositiveButton(android.R.string.ok, null);

        AlertDialog d = b.create();
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                mTempLowDialog = null;
            }
        });

        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        d.show();
        mTempLowDialog = d;
    }

    private void dismissTempOverLowWarningDialog() {
        if (mTempLowDialog != null) {
            Slog.d(TAG, "dismiss temperature overLow dialog");
            mTempLowDialog.dismiss();
        }
    }

    private void showTempOverHighWarningDialog() {
        Slog.d(TAG, "showing temperature overHigh dialog");

        AlertDialog.Builder b = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
        b.setTitle(R.string.temperature_overhigh_title);
        b.setCancelable(true);
        b.setMessage(R.string.temperature_overhigh_text);
        b.setIconAttribute(android.R.attr.alertDialogIcon);
        b.setPositiveButton(android.R.string.ok, null);

        AlertDialog d = b.create();
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                mTempHighDialog = null;
            }
        });

        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        d.show();
        mTempHighDialog = d;
    }

    private void dismissTempOverHighWarningDialog() {
        if (mTempHighDialog != null) {
            Slog.d(TAG, "dismiss temperature overHigh dialog");
            mTempHighDialog.dismiss();
        }
    }

    private void playBatteryTempWarningSound() {
        final ContentResolver cr = mContext.getContentResolver();
        final int silenceAfter = Settings.Global.getInt(cr,
                Settings.Global.LOW_BATTERY_SOUND_TIMEOUT, 0);
        final long offTime = SystemClock.elapsedRealtime() - mScreenOffTime;
        if (silenceAfter > 0 && mScreenOffTime > 0 && offTime > silenceAfter) {
            Slog.i(TAG, "screen off too long (" + offTime + "ms, limit "
                    + silenceAfter
                    + "ms): not waking up the user with low battery sound");
            return;
        }

        if (DEBUG) {
            Slog.d(TAG, "playing battery over sound. pick-a-doop!"); // WOMP-WOMP is deprecated
        }

        if (Settings.Global.getInt(cr, Settings.Global.POWER_SOUNDS_ENABLED, 1) == 1) {
            final String soundPath = SOUND_PATH_FOR_BATTERY;
            if (soundPath != null) {
                final Uri soundUri = Uri.parse("file://" + soundPath);
                if (soundUri != null) {
                    final Ringtone sfx = RingtoneManager.getRingtone(mContext,
                            soundUri);
                    if (sfx != null) {
                        sfx.setStreamType(AudioManager.STREAM_SYSTEM);
                        sfx.play();
                    }
                }
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("mLowBatteryAlertCloseLevel=");
        pw.println(mLowBatteryAlertCloseLevel);
        pw.print("mLowBatteryReminderLevels=");
        pw.println(Arrays.toString(mLowBatteryReminderLevels));
        pw.print("mBatteryLevel=");
        pw.println(Integer.toString(mBatteryLevel));
        pw.print("mBatteryStatus=");
        pw.println(Integer.toString(mBatteryStatus));
        pw.print("mPlugType=");
        pw.println(Integer.toString(mPlugType));
        pw.print("mInvalidCharger=");
        pw.println(Integer.toString(mInvalidCharger));
        pw.print("mScreenOffTime=");
        pw.print(mScreenOffTime);
        if (mScreenOffTime >= 0) {
            pw.print(" (");
            pw.print(SystemClock.elapsedRealtime() - mScreenOffTime);
            pw.print(" ago)");
        }
        pw.println();
        pw.print("soundTimeout=");
        pw.println(Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.LOW_BATTERY_SOUND_TIMEOUT, 0));
        pw.print("bucket: ");
        pw.println(Integer.toString(findBatteryLevelBucket(mBatteryLevel)));
        mWarnings.dump(pw);
    }

    public interface WarningsUI {
        void update(int batteryLevel, int bucket, long screenOffTime);
        void showSaverMode(boolean mode);
        void dismissLowBatteryWarning();
        void showLowBatteryWarning(boolean playSound);
        void dismissInvalidChargerWarning();
        void showInvalidChargerWarning();
        void updateLowBatteryWarning();
        boolean isInvalidChargerWarningShowing();
        void dump(PrintWriter pw);
        void userSwitched();

        void dismissTempOverLowWarning();
        void dismissTempOverHighWarning();
        void showTempOverLowWarning();
        void showTempOverHighWarning();
    }
}

