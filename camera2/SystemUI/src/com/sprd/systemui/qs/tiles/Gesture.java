package com.sprd.systemui.qs.tiles;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.sprd.systemui.statusbar.policy.GestureController;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

public class Gesture extends QSTile<QSTile.BooleanState> {

    private final GestureController mController;

    GestureModelObserver gestureModelObserver;

    public Gesture(Host host) {
        super(host);
        mController = host.getGestureController();
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        if(listening && gestureModelObserver==null){
		gestureModelObserver =new GestureModelObserver(new Handler());
		gestureModelObserver.startObserving();
	}else{
	}
    }

    @Override
    protected void handleClick() {
        mController.setGestureMode(!mController.isGestureModeOn());
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
	state.label = mContext.getString(R.string.quick_settings_gesture_label);
	state.visible = true;
	final boolean GesturemodeEnabled =  mController.isGestureModeOn();
	if(GesturemodeEnabled){
		state.icon = ResourceIcon.get(R.drawable.ic_qs_gesture_1);
	}else{
		state.icon = ResourceIcon.get(R.drawable.ic_qs_gesture_2);
	}
    }

    private class GestureModelObserver extends ContentObserver {
		public GestureModelObserver(Handler handler) {
		    super(handler);
		}

		public void onChange(boolean selfChange) {
			Log.e("wangsheng","=========Gesture");
			refreshState();
		}

		public void startObserving() {
			    final ContentResolver cr = mContext.getContentResolver();
			    cr.registerContentObserver(
				    Settings.System.getUriFor(Settings.System.GESTURE_SWITCH), true, this,
				    UserHandle.USER_ALL);
		}
		public void stopobserving(){
			mContext.getContentResolver().unregisterContentObserver(gestureModelObserver);
		}
   	    }

}

