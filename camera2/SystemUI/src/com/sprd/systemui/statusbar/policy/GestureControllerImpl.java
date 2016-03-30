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

package com.sprd.systemui.statusbar.policy;

import android.util.Log;
import android.content.Context;
import android.provider.Settings;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.WindowManager;
import com.android.systemui.R;
import android.view.Window;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import android.os.SystemProperties;
import android.content.ContentResolver;
import android.database.ContentObserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.ArrayList;



/** Platform implementation of the cast controller. **/
public class GestureControllerImpl implements GestureController{
    private static final String TAG = "wangsheng";
    private final Context mContext;
    private AlertDialog mWarnVistorMode;
    private PhoneStatusBar mPb;
    private static final String GESTURE_MODE_CHANGED = "android.intent.action.gesture_mode_changed";
    public GestureControllerImpl(Context context,PhoneStatusBar pb) {
        mContext = context;
        mPb=pb;
        Log.d(TAG, "new GestureControllerImpl()");
    }
	
    @Override
    public boolean isGestureModeOn() {

      boolean isChecked = Settings.System.getInt(mContext.getContentResolver(), Settings.System.GESTURE_SWITCH,0) == 0;
      if(isChecked){
	  return false;	
      }else{
            return true;
      }
   }


    @Override
    public void setGestureMode(boolean state) {

	Settings.System.putInt(mContext.getContentResolver(),Settings.System.GESTURE_SWITCH,state ? 1 : 0);
	Intent intent = new Intent(GESTURE_MODE_CHANGED);
	mContext.sendBroadcast(intent);
       }
    }



