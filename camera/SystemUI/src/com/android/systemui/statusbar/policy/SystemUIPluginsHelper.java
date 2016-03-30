
package com.android.systemui.statusbar.policy;

import android.app.AddonManager;
import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManagerSprd;
import android.telephony.TelephonyManagerSprd.RadioCapbility;
import android.telephony.TelephonyManagerSprd.RadioFeatures;
import android.util.Log;

import com.android.systemui.R;

public class SystemUIPluginsHelper {
    static SystemUIPluginsHelper mInstance;

    public static final String TAG = "SystemUIPluginsHelper";

    public static int[] SIM_CARD_ID = TelephonyIcons.SIM_CARD_ID;
    public static final int[][] TELEPHONY_SIGNAL_STRENGTH_COLOR_ONE = TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH_COLOR_ONE;
    public static final int[][] TELEPHONY_SIGNAL_STRENGTH_COLOR_TWO = TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH_COLOR_TWO;

    public static final int ABSENT_SIM_COLOR = 0xFFFFFFFF;
    public static final int DEFAULT_SIM_COLOR = 0xFF00796b;
    public static final int[] COLORS = {
            0xFF18FFFF, 0xFFFFEB3B
    };

    public SystemUIPluginsHelper() {
    }

    public static SystemUIPluginsHelper getInstance() {
        if (mInstance != null)
            return mInstance;
        mInstance = (SystemUIPluginsHelper) AddonManager.getDefault()
                .getAddon(R.string.feature_display_for_operator, SystemUIPluginsHelper.class);
        return mInstance;
    }

    public String updateNetworkName(Context context, boolean showSpn, String spn, boolean showPlmn,
            String plmn, int phoneId) {
        return "";
    }

    public int getSubscriptionInfoColor(SubscriptionInfo info) {
        Log.d(TAG, "getSubscriptionInfoColor");
        return info == null ? ABSENT_SIM_COLOR : info.getIconTint();
    }

    public int[][] getColorfulSignalStrengthIcons(int phoneId) {
        return null;
    }

    public int getNoSimIconId() {
        return R.drawable.stat_sys_no_sim_sprd;
    }

    public int getNoServiceIconId() {
        return R.drawable.stat_sys_signal_null;
    }

    public int getSimCardIconId(int subId) {
        return 0;
    }

    public int getSimStandbyIconId() {
        return R.drawable.stat_sys_signal_standby_sprd;
    }

    /* SPRD: add for H+ icons for bug443776 @{ */
    public boolean hspaDataDistinguishable() {
        return true;
    }
    /* @} */

    public int getLteIconId() {
        return R.drawable.stat_sys_data_fully_connected_4g_sprd;
    }

    /* SPRD: add for reliance case @{ */
    public int getMobileGroupLayout() {
        return R.layout.mobile_signal_group;
    }

    public int getRoamIcon() {
        return R.drawable.stat_sys_data_connected_roam_sprd;
    }

    public int getLteIcon() {
        return R.drawable.stat_sys_data_fully_connected_lte_sprd;
    }

    public int getGIcon() {
        return R.drawable.stat_sys_data_fully_connected_g_sprd;
    }

    public int getEIcon() {
        return R.drawable.stat_sys_data_fully_connected_e_sprd;
    }

    public int getHIcon() {
        return R.drawable.stat_sys_data_fully_connected_h_sprd;
    }

    public int getHPIcon() {
        return R.drawable.stat_sys_data_fully_connected_hp_sprd;
    }

    public int getThreeGIcon() {
        return R.drawable.stat_sys_data_fully_connected_3g_sprd;
    }

    public int getFourGIcon() {
        return R.drawable.stat_sys_data_fully_connected_4g_sprd;
    }

    public int getOneXIcon() {
        return R.drawable.stat_sys_data_fully_connected_1x_sprd;
    }

    public int getFourGLte() {
        return R.drawable.stat_sys_data_fully_connected_4g_lte_sprd;
    }

    public int getDataInOutIcon() {
        return R.drawable.stat_sys_data_inout_sprd;
    }

    public int getDataInIcon() {
        return R.drawable.stat_sys_data_in_sprd;
    }

    public int getDataOutIcon() {
        return R.drawable.stat_sys_data_out_sprd;
    }

    public int getDataDefaultIcon() {
        return R.drawable.stat_sys_data_default_sprd;
    }

    public int getSignalZeroIcon() {
        return R.drawable.stat_sys_signal_0_sprd;
    }

    public int getSignalOneIcon() {
        return R.drawable.stat_sys_signal_1_sprd;
    }

    public int getSignalTwoIcon() {
        return R.drawable.stat_sys_signal_2_sprd;
    }

    public int getSignalThreeIcon() {
        return R.drawable.stat_sys_signal_3_sprd;
    }

    public int getSignalFourIcon() {
        return R.drawable.stat_sys_signal_4_sprd;
    }

    public int getFourGIconForVoice() {
        return 0;
    }

    public int getVoLTEIcon() {
        return R.drawable.stat_sys_volte;
    }

    public int getSignalVoLTEIcon() {
        return 0;
    }
    /* @} */

}
