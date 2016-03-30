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

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;

import android.util.Log;
//import com.mediatek.audioprofile.AudioProfileManager;
//import com.mediatek.audioprofile.AudioProfileManager.Scenario;
//import com.mediatek.common.audioprofile.AudioProfileListener;
import android.database.ContentObserver;

import java.util.ArrayList;
import java.util.List;
import android.graphics.drawable.Drawable;

public class AirShutterTitle extends QSTile<QSTile.BooleanState> {
    private static final String TAG = "AirShutterTitle";
    private static final boolean DBG = false;

    private static final boolean ENABLE_AIR_SHUTTER = com.huaqin.common.featureoption.FeatureOption.HQ_SENSOR_CONTROL;// HSS

    private static final int PROFILE_SWITCH_DIALOG_LONG_TIMEOUT = 4000;
    private static final int PROFILE_SWITCH_DIALOG_SHORT_TIMEOUT = 2000;
    private static final int SHOW_AIRSHUTTER_SWITCH_DIALOG = 9000;

    private boolean mListening;
    private boolean mUpdating = false;

    // hss HQ_SENSOR_CONTROL begin
    private Dialog mAirShutterDialog;
    private ImageView mAirShutterIcon;
    private ImageView mCameraIcon;
    private ImageView mMusicIcon;
    private ImageView mGalleryIcon;
    private ImageView mFmIcon;
    private List<String> mAirShuffle;

    private static final String camera_str = "camera";
    private static final String music_str = "music";
    private static final String gallery_str = "gallery";
    private static final String fm_str = "fmradio";
    // hss HQ_SENSOR_CONTROL end

    // private AudioProfileManager mProfileManager;
    // private AudioManager mAudioManager;
    // private Scenario mCurrentScenario;

    private int mAirShutterState = R.drawable.air_shuffle_all_on;

    public AirShutterTitle(Host host) {
        super(host);
        createAirShutterSwitchDialog();
        setAirShutterUpdates(true);
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
    }

    @Override
    protected void handleClick() {
        Message msg = mHandler.obtainMessage(SHOW_AIRSHUTTER_SWITCH_DIALOG);
        mHandler.sendMessage(msg);
    }

    @Override
    protected void handleLongClick() {
        /*
         * Intent intent = new Intent(); intent.setComponent(new ComponentName(
         * "com.android.settings",
         * "com.android.settings.Settings$AudioProfileSettingsActivity"));
         * mHost.startSettingsActivity(intent);
         */
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label = "AIR SHUFFLE";// mContext.getString(R.string.audio_profile);
        state.visible = true;
        state.icon = ResourceIcon.get(mAirShutterState);
    }

    /*
     * private void updateAirShutter(String key) { if (key == null) { return; }
     * if (DBG) { android.util.Log.i(TAG,
     * "updateAirShutter called, selected profile is: " + key); } if
     * (ENABLE_AIR_SHUTTER) { mProfileManager.setActiveProfile(key); } if (DBG)
     * { android.util.Log.d(TAG,
     * "updateAirShutter called, setActiveProfile is: " + key); } }
     */

    /*
     * private void showAirShutterSwitchDialog() {
     * createAirShutterSwitchDialog(); if (!mAirShutterSwitchDialog.isShowing())
     * { try { WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
     * } catch (RemoteException e) { } mAirShutterSwitchDialog.show();
     * dismissAirShutterSwitchDialog(PROFILE_SWITCH_DIALOG_LONG_TIMEOUT); } }
     */

    // HQ_SENSOR_CONTROLs create dialog for air shuffle start-----------hss
    private void showAirShutterSwitchDialog() {
        createAirShutterSwitchDialog();
        if (!mAirShutterDialog.isShowing()) {
            try {
                WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
            } catch (RemoteException e) {
            }
            mAirShutterDialog.show();
            dismissAirShuffleSwitchDialog(PROFILE_SWITCH_DIALOG_LONG_TIMEOUT);
        }
    }

    // HQ_SENSOR_CONTROL start---------------hss
    private void dismissAirShuffleSwitchDialog(int timeout) {
        removeAIrshuffleSwitchDialogCallbacks();
        if (mAirShutterDialog != null) {
            mHandler.postDelayed(mDismissAirShuffleSwitchDialogRunnable,
                    timeout);
        }
    }

    private Runnable mDismissAirShuffleSwitchDialogRunnable = new Runnable() {
        public void run() {
            if (mAirShutterDialog != null && mAirShutterDialog.isShowing()) {
                mAirShutterDialog.dismiss();
            }
            removeAIrshuffleSwitchDialogCallbacks();
        };
    };

    private void removeAIrshuffleSwitchDialogCallbacks() {
        mHandler.removeCallbacks(mDismissAirShuffleSwitchDialogRunnable);
    }

    public void setAirShutterUpdates(boolean update) {
        // HQ_SENSOR_CONTROL

        mAirShuffle = new ArrayList<String>();
        mAirShuffle.add(camera_str);
        mAirShuffle.add(music_str);
        mAirShuffle.add(gallery_str);
        mAirShuffle.add(fm_str);
        mUpdating = update;
        if (update) {
            resgenObservice();// HQ_SENSOR_CONTROL
            setQuickSettingIcon();
        }

        /*
         * if (update != mUpdating) { if (true) {//ENABLE_AUDIO_PROFILE
         * mProfileManager = (AudioProfileManager)
         * mContext.getSystemService(Context.AUDIO_PROFILE_SERVICE);
         * mProfileManager.listenAudioProfie(mAudioProfileListenr,
         * AudioProfileListener.LISTEN_PROFILE_CHANGE); } mProfileKeys = new
         * ArrayList<String>(); mProfileKeys =
         * mProfileManager.getPredefinedProfileKeys(); mUpdating = update; }
         * else { if (true) {//ENABLE_AUDIO_PROFILE
         * mProfileManager.listenAudioProfie(mAudioProfileListenr,
         * AudioProfileListener.STOP_LISTEN); } }
         */
    }

    // HQ_SENSOR_CONTROL end------------hss

    private void createAirShutterSwitchDialog() {
        if (mAirShutterDialog == null) {
            mAirShutterDialog = new Dialog(mContext);
            mAirShutterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mAirShutterDialog
                    .setContentView(R.layout.quick_settings_air_shuffle_switch_dialog);
            mAirShutterDialog.setCanceledOnTouchOutside(true);
            mAirShutterDialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL);
            mAirShutterDialog.getWindow().getAttributes().privateFlags |= WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;
            mAirShutterDialog.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            mCameraIcon = (ImageView) mAirShutterDialog
                    .findViewById(R.id.camera_icon);
            mMusicIcon = (ImageView) mAirShutterDialog
                    .findViewById(R.id.music_icon);
            mGalleryIcon = (ImageView) mAirShutterDialog
                    .findViewById(R.id.gallery_icon);
            mFmIcon = (ImageView) mAirShutterDialog.findViewById(R.id.fm_icon);

            View aircamera = (View) mAirShutterDialog
                    .findViewById(R.id.camera_control);
            aircamera.setOnClickListener(mAirShuffleSwitchListener);
            aircamera.setTag(camera_str);

            View airmusic = (View) mAirShutterDialog
                    .findViewById(R.id.music_control);
            airmusic.setOnClickListener(mAirShuffleSwitchListener);
            airmusic.setTag(music_str);

            View airgallery = (View) mAirShutterDialog
                    .findViewById(R.id.gallery_control);
            airgallery.setOnClickListener(mAirShuffleSwitchListener);
            airgallery.setTag(gallery_str);

            View airfm = (View) mAirShutterDialog.findViewById(R.id.fm_control);
            airfm.setOnClickListener(mAirShuffleSwitchListener);
            airfm.setTag(fm_str);

            loadEnabledAirShuffleResource();

        }
    }

    private View.OnClickListener mAirShuffleSwitchListener = new View.OnClickListener() {
        public void onClick(View v) {
            for (int i = 0; i < mAirShuffle.size(); i++) {
                if (v.getTag().equals(mAirShuffle.get(i))) {
                    if (DBG) {
                        android.util.Log.d(TAG,
                                "onClick called, mAirShuffle clicked is: "
                                        + mAirShuffle.get(i));
                    }
                    String key = mAirShuffle.get(i);
                    saveAirShuffle(key);
                    loadEnabledAirShuffleResource();
                    // add by jiangguohu for HQ01709428 2016.02.01 start
                    if (key.equals(camera_str)) {
                        android.util.Log.i("jiangguohu","getAirShuffleCamera() = "+getAirShuffleCamera());
                        Intent intent = new Intent();
                        intent.putExtra("airCamera", getAirShuffleCamera());
                        intent.setAction("android.intent.action.quick_setting_aircontrol_camera");
                        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                        mContext.sendBroadcast(intent);
                    }
                    // add by jiangguohu for HQ01709428 2016.02.01 end
                    // add by jiangguohu for HQ01712021 2016.02.01 start
                    if (key.equals(gallery_str)) {
                        android.util.Log.i("jiangguohu","getAirShuffleGallery() = "+getAirShuffleGallery());
                        Intent intent = new Intent();
                        intent.putExtra("airGallery", getAirShuffleGallery());
                        intent.setAction("android.intent.action.quick_setting_aircontrol_gallery");
                        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                        mContext.sendBroadcast(intent);
                    }
                    // add by jiangguohu for HQ01712021 2016.02.01 end
                    if (mAirShutterDialog != null) {
                        mAirShutterDialog.dismiss();
                    }
                    break;
                }
            }
        }
    };

    // private void updateProfileView(Scenario scenario) {
    private void updateProfileView() {
        // loadDisabledProfileResouceForAll();
        // loadEnabledProfileResource(scenario);
        // add by wangxiaoyu for air shutter 20140318(start)
        // HQ_SENSOR_CONTROL
        setQuickSettingIcon();
        // add by wangxiaoyu for air shutter 20140318(end)
    }

    // HQ_SENSOR_CONTROL
    public boolean getAirShuffleCamera() {
        if (false)// FeatureOption.HQ_AIRSHUFFLE_SENSOR_DEFAULT_CLOSE
        {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_CAMERA, 0) == 1; // sanlei
        } else if (false) {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_CAMERA, 0) == 1; // sanlei
        } else {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_CAMERA, 1) == 1; // sanlei
        }

    }

    public boolean getAirShuffleMusic() {

        if (false)// FeatureOption.HQ_AIRSHUFFLE_SENSOR_DEFAULT_CLOSE
        {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_MUSIC, 0) == 1;
        } else {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_MUSIC, 1) == 1;
        }

    }

    public boolean getAirShuffleGallery() {

        if (false)// FeatureOption.HQ_AIRSHUFFLE_SENSOR_DEFAULT_CLOSE
        {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_IMAGE, 0) == 1;
        } else {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_IMAGE, 1) == 1;
        }
    }

    public boolean getAirShuffleFM() {
        if (false)// FeatureOption.HQ_AIRSHUFFLE_SENSOR_DEFAULT_CLOSE
        {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_FMRADIO, 0) == 1;
        } else {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_FMRADIO, 1) == 1;
        }
    }

    private void saveAirShuffle(String key) {

        android.util.Log.d("jiangguohu", "saveAirShuffle ,clicked is: " + key);
        if (key.equals(camera_str)) {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_CAMERA,
                    getAirShuffleCamera() ? 0 : 1);
        } else if (key.equals(music_str)) {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_MUSIC,
                    getAirShuffleMusic() ? 0 : 1);
        } else if (key.equals(gallery_str)) {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_IMAGE,
                    getAirShuffleGallery() ? 0 : 1);
        } else if (key.equals(fm_str)) {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.HQ_ACCESSIBILITY_SENSOR_FMRADIO,
                    getAirShuffleFM() ? 0 : 1);
        }

    }

    private void loadEnabledAirShuffleResource() {

        android.util.Log.d(TAG, "camera is: " + getAirShuffleCamera());
        android.util.Log.d(TAG, "music is: " + getAirShuffleMusic());
        android.util.Log.d(TAG, "gallery is: " + getAirShuffleGallery());
        android.util.Log.d(TAG, "fm is: " + getAirShuffleFM());
        // first set icon for app icon
        setDialogIconShow();
        // second for mAirShutterIcon
        setQuickSettingIcon();

    }

    public void setDialogIconShow() {
        if (mCameraIcon != null && mMusicIcon != null && mGalleryIcon != null
                && mFmIcon != null) {
            mCameraIcon
                    .setImageResource(getAirShuffleCamera() ? R.drawable.air_camera_on
                            : R.drawable.air_camera_off);
            mMusicIcon
                    .setImageResource(getAirShuffleMusic() ? R.drawable.air_music_on
                            : R.drawable.air_music_off);
            mGalleryIcon
                    .setImageResource(getAirShuffleGallery() ? R.drawable.air_gallery_on
                            : R.drawable.air_gallery_off);
            mFmIcon.setImageResource(getAirShuffleFM() ? R.drawable.air_fm_on
                    : R.drawable.air_fm_off);
        }
    }

    public void setQuickSettingIcon() {
        /*
         * if(mAirShutterState == null){ return; }
         */
        if (getAirShuffleCamera() && !getAirShuffleMusic()
                && !getAirShuffleGallery() && !getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_camera_on);
            mAirShutterState = R.drawable.air_shuffle_camera_on;// hss
        } else if (!getAirShuffleCamera() && getAirShuffleMusic()
                && !getAirShuffleGallery() && !getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_music_on);
            mAirShutterState = R.drawable.air_shuffle_music_on;// hss
        } else if (!getAirShuffleCamera() && !getAirShuffleMusic()
                && getAirShuffleGallery() && !getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_gallery_on);
            mAirShutterState = R.drawable.air_shuffle_gallery_on;// hss
        } else if (!getAirShuffleCamera() && !getAirShuffleMusic()
                && !getAirShuffleGallery() && getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_fm_on);
            mAirShutterState = R.drawable.air_shuffle_fm_on;// hss
        } else if (!getAirShuffleCamera() && !getAirShuffleMusic()
                && !getAirShuffleGallery() && !getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_all_on);
            mAirShutterState = R.drawable.air_shuffle_all_on;// hss
        } else if (getAirShuffleCamera() && getAirShuffleMusic()
                && !getAirShuffleGallery() && !getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_camera_music_on);
            mAirShutterState = R.drawable.air_shuffle_camera_music_on;// hss
        } else if (getAirShuffleCamera() && !getAirShuffleMusic()
                && getAirShuffleGallery() && !getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_camera_gallery_on);
            mAirShutterState = R.drawable.air_shuffle_camera_gallery_on;// hss
        } else if (getAirShuffleCamera() && !getAirShuffleMusic()
                && !getAirShuffleGallery() && getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_camera_fm_on);
            mAirShutterState = R.drawable.air_shuffle_camera_fm_on;// hss
        } else if (!getAirShuffleCamera() && getAirShuffleMusic()
                && getAirShuffleGallery() && !getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_music_gallery_on);
            mAirShutterState = R.drawable.air_shuffle_music_gallery_on;// hss
        } else if (!getAirShuffleCamera() && getAirShuffleMusic()
                && !getAirShuffleGallery() && getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_music_fm_on);
            mAirShutterState = R.drawable.air_shuffle_music_fm_on;// hss
        } else if (!getAirShuffleCamera() && !getAirShuffleMusic()
                && getAirShuffleGallery() && getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_gallery_fm_on);
            mAirShutterState = R.drawable.air_shuffle_gallery_fm_on;// hss
        } else if (getAirShuffleCamera() && getAirShuffleMusic()
                && getAirShuffleGallery() && !getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_camera_music_gallery_on);
            mAirShutterState = R.drawable.air_shuffle_camera_music_gallery_on;// hss
        } else if (getAirShuffleCamera() && getAirShuffleMusic()
                && !getAirShuffleGallery() && getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_camera_music_fm_on);
            mAirShutterState = R.drawable.air_shuffle_camera_music_fm_on;// hss
        } else if (getAirShuffleCamera() && !getAirShuffleMusic()
                && getAirShuffleGallery() && getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_camera_gallery_fm_on);
            mAirShutterState = R.drawable.air_shuffle_camera_gallery_fm_on;// hss
        } else if (!getAirShuffleCamera() && getAirShuffleMusic()
                && getAirShuffleGallery() && getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_music_gallery_fm_on);
            mAirShutterState = R.drawable.air_shuffle_music_gallery_fm_on;// hss
        } else if (getAirShuffleCamera() && getAirShuffleMusic()
                && getAirShuffleGallery() && getAirShuffleFM()) {
            // mAirShutterIcon.setImageResource(R.drawable.air_shuffle_camera_music_gallery_fm_on);
            mAirShutterState = R.drawable.air_shuffle_camera_music_gallery_fm_on;// hss
        }
        refreshState();

    }

    public void resgenObservice() {// HQ_SENSOR_CONTROL

        mContext.getContentResolver()
                .registerContentObserver(
                        Settings.Secure
                                .getUriFor(Settings.Secure.HQ_ACCESSIBILITY_SENSOR_CAMERA),
                        true, mAirShuffleChangeObserver);// HQ_SENSOR_CONTROL
        android.util.Log.d("chenqiwei", "HQ_ACCESSIBILITY_SENSOR_CAMERA: ");
        mContext.getContentResolver()
                .registerContentObserver(
                        Settings.Secure
                                .getUriFor(Settings.Secure.HQ_ACCESSIBILITY_SENSOR_MUSIC),
                        true, mAirShuffleChangeObserver);// HQ_SENSOR_CONTROL

        mContext.getContentResolver()
                .registerContentObserver(
                        Settings.Secure
                                .getUriFor(Settings.Secure.HQ_ACCESSIBILITY_SENSOR_FMRADIO),
                        true, mAirShuffleChangeObserver);// HQ_SENSOR_CONTROL

        mContext.getContentResolver()
                .registerContentObserver(
                        Settings.Secure
                                .getUriFor(Settings.Secure.HQ_ACCESSIBILITY_SENSOR_IMAGE),
                        true, mAirShuffleChangeObserver);// HQ_SENSOR_CONTROL
    }

    private ContentObserver mAirShuffleChangeObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            loadEnabledAirShuffleResource();
            android.util.Log.d(TAG, "changge ");
        }
    };
    // HQ_SENSOR_CONTROL end

    // ////////////////////////////////ok
    // start////////////////////////////////////////
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SHOW_AIRSHUTTER_SWITCH_DIALOG:
                showAirShutterSwitchDialog();
                break;
            default:
                break;
            }
        }
    };
    // ////////////////////////////////ok
    // end////////////////////////////////////////
}
