
package com.android.systemui.statusbar;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * SPRD:
 */
public class FloatAppButton extends ImageButton implements OnClickListener {
    private static final String TAG = "FloatAppButton";

    private ComponentName mApp;
    private Context mContext;
    FloatPanelView mFloatPanelView;

    public FloatAppButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOnClickListener(this);
        setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        mFloatPanelView.hide();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(mApp);
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "start activity fail for " + mApp);
        }

    }

    public void setApp(ComponentName component) {
        mApp = component;
        Drawable icon = getAppIcon(component);
        setImageDrawable(icon);
        if (icon == null) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }

    }

    private Drawable getAppIcon(ComponentName component) {
        PackageManager pm = mContext.getPackageManager();
        Drawable icon = null;
        try {
            icon = pm.getActivityIcon(component);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getAppIcon fail for " + component);
        }
        return icon;
    }

    public void setFloatPanelView(FloatPanelView floatPanelView) {
        mFloatPanelView = floatPanelView;
    }

}
