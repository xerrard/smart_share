package com.huaqin.wirelessfiletransfer.listen._02_base;

import java.util.Calendar;

import com.huaqin.wirelessfiletransfer.R;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public abstract class NoDoubleClickListener implements OnClickListener {

    public Context context;
    public int min_clik_delay_time;
    
    public NoDoubleClickListener(Context context,int min_clik_delay_time) {
        super(); 
        this.context = context;
        this.min_clik_delay_time = min_clik_delay_time;
    }

    public static final int MIN_CLICK_DELAY_TIME = 10000;
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onNoDoubleClick(v);
        }
        else { 
            onDoubleClick(v);
            
        }  
    }

    public abstract void onNoDoubleClick(View v);
    public void onDoubleClick(View v){
        Toast.makeText(context,
                context.getText(R.string.click_search_limit),
                Toast.LENGTH_LONG).show();
    }
}