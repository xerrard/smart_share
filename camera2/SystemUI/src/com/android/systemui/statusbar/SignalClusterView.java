/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.systemui.statusbar;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManagerSprd;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.SystemUIPluginsHelper;

import java.util.ArrayList;
import java.util.List;

// Intimately tied to the design of res/layout/signal_cluster_view.xml
public class SignalClusterView
        extends LinearLayout
        implements NetworkControllerImpl.SignalCluster,
        SecurityController.SecurityControllerCallback {

    static final String TAG = "SignalClusterView";
    static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    NetworkControllerImpl mNC;
    SecurityController mSC;

    private boolean mNoSimsVisible = false;
    private boolean mVpnVisible = false;
    private boolean mWifiVisible = false;
    private int mWifiStrengthId = 0;
    private boolean mIsAirplaneMode = false;
    private int mAirplaneIconId = 0;
    private int mAirplaneContentDescription;
    private String mWifiDescription;
    private ArrayList<PhoneState> mPhoneStates = new ArrayList<PhoneState>();

    ViewGroup mWifiGroup;
    ImageView mVpn, mWifi, mAirplane, mNoSims;
    View mWifiAirplaneSpacer;
    View mWifiSignalSpacer;
    LinearLayout mMobileSignalGroup;
    /* SPRD: bug450508 modify for volte icon @{ */
    ImageView mVolte;
    private int mVolteIconId = 0;
    /* @} */
    /* SPRD: Add HD audio icon in cucc for bug 509605. @{ */
    ImageView mHdVoice;
    private int mHdVoiceIconId = 0;
    /* @} */

    // SPRD: add for reliance case
    private boolean isRelianceBoard = "reliance".equals(SystemProperties.get(
            "ro.operator.volte", ""));
    private int mWideTypeIconStartPadding;
    private int mSecondaryTelephonyPadding;
    private int mEndPadding;
    private int mEndPaddingNothingVisible;

    ImageView mWifiOut, mWifiIn;
    private boolean isWifiIn, isWifiOut;

    public SignalClusterView(Context context) {
        this(context, null);
    }

    public SignalClusterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalClusterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setNetworkController(NetworkControllerImpl nc) {
        if (DEBUG) Log.d(TAG, "NetworkController=" + nc);
        mNC = nc;
    }

    public void setSecurityController(SecurityController sc) {
        if (DEBUG) Log.d(TAG, "SecurityController=" + sc);
        mSC = sc;
        mSC.addCallback(this);
        mVpnVisible = mSC.isVpnEnabled();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mWideTypeIconStartPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.wide_type_icon_start_padding);
        mSecondaryTelephonyPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.secondary_telephony_padding);
        mEndPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.signal_cluster_battery_padding);
        mEndPaddingNothingVisible = getContext().getResources().getDimensionPixelSize(
                R.dimen.no_signal_cluster_battery_padding);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mVpn            = (ImageView) findViewById(R.id.vpn);
        mWifiGroup      = (ViewGroup) findViewById(R.id.wifi_combo);
        mWifi           = (ImageView) findViewById(R.id.wifi_signal);
        mWifiOut = (ImageView) findViewById(R.id.wifi_out);
        mWifiIn = (ImageView) findViewById(R.id.wifi_in);
        mAirplane       = (ImageView) findViewById(R.id.airplane);
        mNoSims         = (ImageView) findViewById(R.id.no_sims);
        mWifiAirplaneSpacer =         findViewById(R.id.wifi_airplane_spacer);
        mWifiSignalSpacer =           findViewById(R.id.wifi_signal_spacer);
        mMobileSignalGroup = (LinearLayout) findViewById(R.id.mobile_signal_group);

        /* SPRD: bug450508 modify for volte icon @{ */
        mVolte          = (ImageView) findViewById(R.id.volte);
        /* @} */
        /* SPRD: Add HD audio icon in cucc for bug 509605. @{ */
        mHdVoice        = (ImageView) findViewById(R.id.hd_voice);
        /* @} */
        for (PhoneState state : mPhoneStates) {
            mMobileSignalGroup.addView(state.mMobileGroup);
        }

        apply();
    }

    @Override
    protected void onDetachedFromWindow() {
        mVpn            = null;
        mWifiGroup      = null;
        mWifi           = null;
        mWifiOut = null;
        mWifiIn = null;
        mAirplane       = null;
        mMobileSignalGroup.removeAllViews();
        mMobileSignalGroup = null;
        /* SPRD: bug450508 modify for volte icon @{ */
        mVolte          = null;
        /* @} */
        // SPRD: Add HD audio icon in cucc for bug 509605.
        mHdVoice        = null;
        super.onDetachedFromWindow();
    }

    // From SecurityController.
    @Override
    public void onStateChanged() {
        post(new Runnable() {
            @Override
            public void run() {
                mVpnVisible = mSC.isVpnEnabled();
                apply();
            }
        });
    }

    @Override
    public void setWifiIndicators(boolean visible, int strengthIcon, String contentDescription) {
        mWifiVisible = visible;
        mWifiStrengthId = strengthIcon;
        mWifiDescription = contentDescription;
        isWifiIn = false;
        isWifiOut = false;

        apply();
    }

    @Override
    public void setWifiIndicators(boolean visible, int strengthIcon, boolean activityIn, boolean activityOut, String contentDescription) {
        mWifiVisible = visible;
        mWifiStrengthId = strengthIcon;
        mWifiDescription = contentDescription;
        isWifiIn = activityIn;
        isWifiOut = activityOut;

        apply();
    }

    /* SPRD: Modify for bug 434412 @{ */
    @Override
    public void setMobileDataIndicators(boolean visible, int strengthIcon, int typeIcon,
            int roamIcon, String contentDescription, String typeContentDescription,
            boolean isTypeIconWide, int subId, int mobileDataInId, int mobileDataOutId) {
        // SPRD: modify for bug454715
        int phoneId = SubscriptionManager.getPhoneId(subId);
        // SPRD: modify for bug462864
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            PhoneState state = getOrInflateState(phoneId, subId);
            state.mMobileVisible = visible;
            state.mMobileStrengthId = strengthIcon;
            state.mMobileTypeId = typeIcon;
            state.mMobileRoamId = roamIcon;
            state.mMobileDescription = contentDescription;
            state.mMobileTypeDescription = typeContentDescription;
            state.mIsMobileTypeIconWide = isTypeIconWide;
//        state.mMobileDataInId = mobileDataInId;
//        state.mMobileDataOutId = mobileDataOutId;
            state.mMobileDataInOutId = mobileDataInId;
            apply();
        }
    }

    /* SPRD: Reliance UI spec 1.7. See bug #503821. @{ */
    public void setMobileDataIndicators(boolean visible, int strengthIcon, int typeIcon,
            int roamIcon, int imsregIcon, boolean isFourG, String contentDescription,
            String typeContentDescription,
            boolean isTypeIconWide, int subId, int mobileDataInId, int mobileDataOutId) {
        // SPRD: modify for bug454715
        int phoneId = SubscriptionManager.getPhoneId(subId);
        // SPRD: modify for bug462864
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            PhoneState state = getOrInflateState(phoneId, subId);
            state.mMobileVisible = visible;
            state.mMobileStrengthId = strengthIcon;
            state.mMobileTypeId = typeIcon;
            state.mMobileRoamId = roamIcon;
            state.mMobileImsregId = imsregIcon;
            state.isFourGLTE = isFourG;
            state.mMobileDescription = contentDescription;
            state.mMobileTypeDescription = typeContentDescription;
            state.mIsMobileTypeIconWide = isTypeIconWide;
//        state.mMobileDataInId = mobileDataInId;
//        state.mMobileDataOutId = mobileDataOutId;
            state.mMobileDataInOutId = mobileDataInId;
            apply();
        }
    }
    /* @} */
    /* @} */

    @Override
    public void setNoSims(boolean show) {
        mNoSimsVisible = show;
    }

    /* SPRD: Create PhoneState for slot which has no sim @{ */
    @Override
    public void setSubs(List<SubscriptionInfo> subs) {
        /* SPRD: add for Bug 464626 for volte@{ */
        TelephonyManagerSprd tm = (TelephonyManagerSprd) TelephonyManager.from(mContext);
        boolean isValidSim [] = new boolean[tm.getPhoneCount()];
         /* @} */
        /* SPRD: add for Bug 452989 @{ */
        int validSimCount = 0;
        int activeSubSize = subs.size();
        boolean isHotSwapSupported = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_hotswapCapable);
        ArrayList<PhoneState> phoneStates = new ArrayList<PhoneState>(mPhoneStates);
        for(PhoneState state : mPhoneStates) {
            Log.d(TAG,"subId = " + state.mSubId);
            if(SubscriptionManager.isValidSubscriptionId(state.mSubId)) {
                /* SPRD: add for Bug 464626 for volte @{ */
                isValidSim[state.mPhoneId] = true;
                  /* @} */
                validSimCount ++;
            }
        }
        Log.d(TAG,"validSimCount = " + validSimCount + ", subs.size = " + activeSubSize);
        if(isHotSwapSupported && validSimCount > 1 && activeSubSize ==0){
            Log.e(TAG,"invalid state, do nothing and return");
            return;
        }
       boolean simCountChange = !(validSimCount == activeSubSize);
        /* @} */
        // Clear out all old subIds.
        mPhoneStates.clear();
        if (mMobileSignalGroup != null) {
            mMobileSignalGroup.removeAllViews();
        }

        final int n = tm.getPhoneCount(); //TelephonyManager.from(mContext).getPhoneCount();
        for (int i = 0; i < n; i++) {
            SubscriptionInfo subInfo = findRecordByPhoneId(subs, i);
            int subId = subInfo != null ? subInfo.getSubscriptionId()
                    : SubscriptionManager.INVALID_SUBSCRIPTION_ID - i;
            /* SPRD: add for Bug 452989 @{ */
            PhoneState lastState = null ;
            if(SubscriptionManager.isValidSubscriptionId(subId)) {
                int phoneId = SubscriptionManager.getPhoneId(subId);
                for(PhoneState state : phoneStates) {
                    // SPRD: modify for bug454715
                    if(state.mPhoneId == phoneId) {
                        lastState = state;
                        lastState.mSubId = subId;
                        break;
                    }
                }
            }
            /* SPRD: add for Bug 464626 for volte@{ */
            else {
                if (TelephonyManagerSprd.getVolteEnabled()
                        && validSimCount > activeSubSize
                        && i == tm.getPrimaryCard() && isValidSim[i]
                        && mVolte != null) {
                    Log.d(TAG,
                            "Primary card has invalid subId, will set volte dismiss");
                    mVolte.setVisibility(View.GONE);
                }
            }
            /* @} */
            if(simCountChange && lastState != null) {
                if (mMobileSignalGroup != null) {
                    mMobileSignalGroup.addView(lastState.mMobileGroup);
                }
                mPhoneStates.add(lastState);
            } else {
            /* @} */
            // SPRD: modify for bug454715
            inflatePhoneState(i, subId);
            }
        }
    }

    private SubscriptionInfo findRecordByPhoneId(List<SubscriptionInfo> subs, int phoneId) {
        if (subs != null) {
            final int length = subs.size();
            for (int i = 0; i < length; ++i) {
                final SubscriptionInfo sir = subs.get(i);
                if (sir.getSimSlotIndex() == phoneId) {
                    return sir;
                }
            }
        }
        return null;
    }
    /* @} */

    // SPRD: modify for bug454715
    private PhoneState getOrInflateState(int phoneId, int subId) {
        for (PhoneState state : mPhoneStates) {
            if (state.mPhoneId == phoneId) {
                state.mSubId = subId;
                return state;
            }
        }
        return inflatePhoneState(phoneId, subId);
    }

    private PhoneState inflatePhoneState(int phoneId, int subId) {
        // SPRD: modify for bug454715
        PhoneState state = new PhoneState(phoneId, subId, mContext);
        if (mMobileSignalGroup != null) {
            mMobileSignalGroup.addView(state.mMobileGroup);
        }
        mPhoneStates.add(state);
        return state;
    }

    @Override
    public void setIsAirplaneMode(boolean is, int airplaneIconId, int contentDescription) {
        mIsAirplaneMode = is;
        mAirplaneIconId = airplaneIconId;
        mAirplaneContentDescription = contentDescription;

        apply();
    }

    /* SPRD: bug450508 modify for volte icon @{ */
    @Override
    public void setVolteIcon(boolean enabled) {
        if("cmcc".equals(SystemProperties.get("ro.operator"))){
            mVolteIconId = enabled ? R.drawable.stat_sys_volte_cmcc : 0;
        }else{
            // SPRD: add for reliance case
            mVolteIconId = enabled ? SystemUIPluginsHelper.getInstance().getVoLTEIcon() : 0;
        }
        apply();
    }
    /* @} */

    /* SPRD: Add HD audio icon in cucc for bug 509605. @{ */
    @Override
    public void setHdVoiceIcon(boolean enabled) {
        mHdVoiceIconId = enabled ? SystemUIPluginsHelper.getInstance().getHdVoiceIconId() : 0;
        apply();
    }
    /* @} */

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // Standard group layout onPopulateAccessibilityEvent() implementations
        // ignore content description, so populate manually
        if (mWifiVisible && mWifiGroup != null && mWifiGroup.getContentDescription() != null)
            event.getText().add(mWifiGroup.getContentDescription());
        for (PhoneState state : mPhoneStates) {
            state.populateAccessibilityEvent(event);
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);

        if (mWifi != null) {
            mWifi.setImageDrawable(null);
        }

        for (PhoneState state : mPhoneStates) {
            if (state.mMobile != null) {
                state.mMobile.setImageDrawable(null);
            }
            if (state.mMobileType != null) {
                state.mMobileType.setImageDrawable(null);
            }
            /* SPRD: add for bug 434412 @{ */
            if (state.mMobileRoam != null) {
                state.mMobileRoam.setImageDrawable(null);
            }
            /* @} */
            /* SPRD: Reliance UI spec 1.7. See bug #503821. @{ */
            if (state.mMobileImsreg != null) {
                state.mMobileImsreg.setImageDrawable(null);
            }
            /* @} */
        }

        if(mAirplane != null) {
            mAirplane.setImageDrawable(null);
        }
        /* SPRD: bug450508 modify for volte icon @{ */
        if(mVolte != null) {
            mVolte.setImageDrawable(null);
        }
        /* @} */

        /* SPRD: Add HD audio icon in cucc for bug 509605. @{ */
        if (mHdVoice != null) {
            mHdVoice.setImageDrawable(null);
        }
        /* @} */
        apply();
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    // Run after each indicator change.
    private void apply() {
        if (mWifiGroup == null) return;

        mVpn.setVisibility(mVpnVisible ? View.VISIBLE : View.GONE);
        if (DEBUG) Log.d(TAG, String.format("vpn: %s", mVpnVisible ? "VISIBLE" : "GONE"));
        if (mWifiVisible) {
            mWifi.setImageResource(mWifiStrengthId);
            mWifiIn.setVisibility(isWifiIn ? View.VISIBLE : View.INVISIBLE);
            mWifiOut.setVisibility(isWifiOut ? View.VISIBLE : View.INVISIBLE);
            mWifiGroup.setContentDescription(mWifiDescription);
            mWifiGroup.setVisibility(View.VISIBLE);
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }

        if (DEBUG) Log.d(TAG,
                String.format("wifi: %s sig=%d",
                    (mWifiVisible ? "VISIBLE" : "GONE"),
                    mWifiStrengthId));

        boolean anyMobileVisible = false;
        int firstMobileTypeId = 0;
        for (PhoneState state : mPhoneStates) {
            if (state.apply(anyMobileVisible)) {
                if (!anyMobileVisible) {
                    firstMobileTypeId = state.mMobileTypeId;
                    anyMobileVisible = true;
                }
            }
        }

        if (mIsAirplaneMode) {
            mAirplane.setImageResource(mAirplaneIconId);
            mAirplane.setContentDescription(mAirplaneContentDescription != 0 ?
                    mContext.getString(mAirplaneContentDescription) : null);
            mAirplane.setVisibility(View.VISIBLE);
        } else {
            mAirplane.setVisibility(View.GONE);
        }

        /* SPRD: bug450508 modify for volte icon @{ */
        // SPRD: Set VoLte icon invisible in airplane mode for bug 530738.
        if (mVolteIconId > 0 && !mIsAirplaneMode) {
            mVolte.setImageResource(mVolteIconId);
            mVolte.setVisibility(View.VISIBLE);
        } else {
            mVolte.setVisibility(View.GONE);
        }
        /* @} */

        /* SPRD: bug450508 modify for volte icon @{ */
        if (mHdVoiceIconId > 0) {
            mHdVoice.setImageResource(mHdVoiceIconId);
            mHdVoice.setVisibility(View.VISIBLE);
        } else {
            mHdVoice.setVisibility(View.GONE);
        }
        /* @} */

        if (mIsAirplaneMode && mWifiVisible) {
            mWifiAirplaneSpacer.setVisibility(View.VISIBLE);
        } else {
            mWifiAirplaneSpacer.setVisibility(View.GONE);
        }

        if (((anyMobileVisible && firstMobileTypeId != 0) || mNoSimsVisible) && mWifiVisible) {
            mWifiSignalSpacer.setVisibility(View.VISIBLE);
        } else {
            mWifiSignalSpacer.setVisibility(View.GONE);
        }

        // SPRD: this no sim is useless
        mNoSims.setVisibility(false && mNoSimsVisible ? View.VISIBLE : View.GONE);

        boolean anythingVisible = mNoSimsVisible || mWifiVisible || mIsAirplaneMode
                || anyMobileVisible || mVpnVisible;
        setPaddingRelative(0, 0, anythingVisible ? mEndPadding : mEndPaddingNothingVisible, 0);
    }

    /* SPRD: modify for bug454715 @{ */
    @Override
    public void updateMobileIconIfSimExist(int phoneId) {
        for (PhoneState state : mPhoneStates) {
            if (state.mPhoneId == phoneId) {
                state.updateMobileIconIfSimExist();
                break;
            }
        }
    }
    /* @} */

    /* SPRD: modify for bug460599 @{ */
    @Override
    public void isNeedSetStandbyIcon(int phoneId) {
        for (PhoneState state: mPhoneStates) {
            if (state.mPhoneId == phoneId) {
                state.isNeedSetStandbyIcon();
                break;
            }
        }
    }
    /* @} */

    @Override
    public void isNeedSetColorfulIcon(int phoneId) {
        for (PhoneState state: mPhoneStates) {
            if (state.mPhoneId == phoneId) {
                state.isNeedSetColorfulIcon();
                break;
            }
        }
    }

    private class PhoneState {
        // SPRD: modify for bug454715
        private final int mPhoneId;
        private int mSubId;
        //modified by zhangduanhua , 20160223
        private boolean mMobileVisible = com.huaqin.common.featureoption.FeatureOption.HQ_LAVA_SHOW_NO_SIM?true:false;//modified by yaoyajun
        // SPRD: add no sim icon for each slot
        private int mMobileStrengthId = SystemUIPluginsHelper.getInstance().getNoSimIconId();
        // SPRD: add for reliance case
        private int mViewGroupLayout = SystemUIPluginsHelper.getInstance().getMobileGroupLayout();
        // SPRD: modify for bug 434412
        // SPRD: add for reliance case
        private int mMobileTypeId = 0, mMobileDataInId = 0, mMobileRoamId = 0, mMobileImsregId = 0,
                mMobileDataOutId = 0, mMobileDataInOutId = 0, mMobileCardId = 0;
        private boolean mIsMobileTypeIconWide;
        // SPRD: Reliance UI spec 1.7. See bug #503821.
        private boolean isFourGLTE;
        private String mMobileDescription, mMobileTypeDescription;

        private ViewGroup mMobileGroup;
        // SPRD: modify for bug 434412
        // SPRD: Reliance UI spec 1.7. See bug #503821.
        private ImageView mMobile, mMobileType, mMobileRoam, mMobileImsreg, mMobileDataIn, mMobileDataOut, mMobileDataInOut, mMobileCard;
        private boolean mColorfulMobileSignal;
        private SubscriptionManager mSubManager;
        private TelephonyManagerSprd mTelephonyManager;
        private int mCurrentColor = -1;
        private int mLastColor = -1;


        // SPRD: modify for bug454715
        public PhoneState(int phoneId, int subId, Context context) {
            Log.d(TAG, "Create PhoneState subId = " + subId);
            ViewGroup root = (ViewGroup) LayoutInflater.from(context)
                    .inflate(mViewGroupLayout, null);
            setViews(root);
            mPhoneId = phoneId;
            mSubId = subId;
            // SPRD: add sim card icon for each slot
            mMobileCardId = SystemUIPluginsHelper.getInstance().getSimCardIconId(subId);
            mColorfulMobileSignal = getResources().getBoolean(R.bool.enable_signal_strenth_color);
            mSubManager = SubscriptionManager.from(mContext);
            mTelephonyManager = (TelephonyManagerSprd) TelephonyManager.from(mContext);
        }

        public void updateMobileIconIfSimExist() {
            if(mMobileStrengthId == SystemUIPluginsHelper.getInstance().getNoSimIconId()) {
                mMobileStrengthId = SystemUIPluginsHelper.getInstance().getNoServiceIconId();
                apply(false);
            }
        }

        public void isNeedSetStandbyIcon() {
            if (!mTelephonyManager.isSimStandby(mPhoneId)) {
                mMobileStrengthId = SystemUIPluginsHelper.getInstance().getSimStandbyIconId();
                mMobile.setColorFilter(null);
                mLastColor = -1;
                apply(false);
            }
        }

        private void updateStandbyIcon() {
            if (mTelephonyManager.isSimStandby(mPhoneId) && mMobileStrengthId == SystemUIPluginsHelper.getInstance().getSimStandbyIconId()) {
                mMobileStrengthId = SystemUIPluginsHelper.getInstance().getNoServiceIconId();
                apply(false);
            }
        }

        public void isNeedSetColorfulIcon() {
            if (mColorfulMobileSignal) {
                SubscriptionInfo subInfo = mSubManager.getActiveSubscriptionInfo(mSubId);
                mCurrentColor = SystemUIPluginsHelper.getInstance().getSubscriptionInfoColor(subInfo);
                if (mTelephonyManager.isSimStandby(mPhoneId)) {
                    if (mCurrentColor != mLastColor) {
                        if (mCurrentColor == SystemUIPluginsHelper.DEFAULT_SIM_COLOR) {
                            mMobile.setColorFilter(SystemUIPluginsHelper.ABSENT_SIM_COLOR);
                            mMobileDataInOut.setColorFilter(SystemUIPluginsHelper.ABSENT_SIM_COLOR);
                        } else {
                            mMobile.setColorFilter(mCurrentColor);
                            mMobileDataInOut.setColorFilter(mCurrentColor);
                        }
                        mLastColor = mCurrentColor;
                    }
                }
            }
            apply(false);
            /* @} */
        }

        public void setViews(ViewGroup root) {
            mMobileGroup    = root;
            mMobile         = (ImageView) root.findViewById(R.id.mobile_signal);
            mMobileType     = (ImageView) root.findViewById(R.id.mobile_type);
            // SPRD: add for bug 434412
            mMobileRoam     = (ImageView) root.findViewById(R.id.mobile_roam_type);
            // SPRD: Reliance UI spec 1.7. See bug #503821.
            mMobileImsreg    = (ImageView) root.findViewById(R.id.mobile_imsreg);
//            mMobileDataIn   = (ImageView) root.findViewById(R.id.mobile_data_in);
//            mMobileDataOut  = (ImageView) root.findViewById(R.id.mobile_data_out);
            mMobileDataInOut   = (ImageView) root.findViewById(R.id.mobile_data_in_out);
            mMobileCard     = (ImageView) root.findViewById(R.id.mobile_card);
        }

        public boolean apply(boolean isSecondaryIcon) {
            if (mMobileVisible && !mIsAirplaneMode) {
                updateStandbyIcon();
                mMobile.setImageResource(mMobileStrengthId);
                mMobileType.setImageResource(mMobileTypeId);
                // SPRD: add for bug 434412
                /* SPRD: Reliance UI spec 1.7. See bug #503821. @{ */
                if (isRelianceBoard && mMobileImsregId != 0 && mMobileRoamId == 0) {
                    mMobileRoam.setImageResource(mMobileImsregId);
                } else if (isRelianceBoard && mMobileRoamId == 0 && !isFourGLTE
                        && mMobileTypeId != 0
                        && mMobileDataInOutId == 0) {
                    mMobileRoam.setImageResource(mMobileTypeId);
                } else {
                    mMobileRoam.setImageResource(mMobileRoamId);
                }
                /* @} */
                // SPRD: add for bug 445792
                mMobileCard.setImageResource(mMobileCardId);
//                mMobileDataIn.setImageResource(mMobileDataInId);
//                mMobileDataOut.setImageResource(mMobileDataOutId);
                mMobileDataInOut.setImageResource(mMobileDataInOutId);
                /* SPRD: Add for Bug459030. @{ */
                mMobileGroup.setContentDescription(mMobileTypeDescription
                        + " " + mMobileDescription);
                mMobileGroup.setVisibility(View.VISIBLE);
            } else {
                mMobileGroup.setVisibility(View.GONE);
            }

            // When this isn't next to wifi, give it some extra padding between the signals.
            mMobileGroup.setPaddingRelative(isSecondaryIcon ? mSecondaryTelephonyPadding : 0,
                    0, 0, 0);
            mMobile.setPaddingRelative(mIsMobileTypeIconWide ? mWideTypeIconStartPadding : 0,
                    0, 0, 0);

            if (DEBUG) Log.d(TAG, String.format("mobile: %s sig=%d typ=%d",
                        (mMobileVisible ? "VISIBLE" : "GONE"), mMobileStrengthId, mMobileTypeId));

            /* SPRD: Reliance UI spec 1.7. See bug #503821. @{ */
            if (isRelianceBoard && mMobileRoamId == 0 && mMobileImsregId != 0) {
                mMobileType.setVisibility(mMobileTypeId != 0 ? View.VISIBLE : View.GONE);
                mMobileRoam.setVisibility(View.VISIBLE);
            } else if (isRelianceBoard && mMobileRoamId == 0 && !isFourGLTE && mMobileTypeId != 0
                    && mMobileDataInOutId == 0) {
                mMobileType.setVisibility(View.GONE);
                mMobileRoam.setVisibility(View.VISIBLE);
            } else {
                mMobileType.setVisibility(mMobileTypeId != 0 ? View.VISIBLE : View.GONE);
                // SPRD: add for bug 434412
                mMobileRoam.setVisibility(mMobileRoamId != 0 ? View.VISIBLE : View.GONE);
            }
            /* @} */
            mMobileCard.setVisibility(mMobileCardId != 0 ? View.VISIBLE : View.GONE);

            return mMobileVisible;
        }

        public void populateAccessibilityEvent(AccessibilityEvent event) {
            if (mMobileVisible && mMobileGroup != null
                    && mMobileGroup.getContentDescription() != null) {
                event.getText().add(mMobileGroup.getContentDescription());
            }
        }
    }
}

