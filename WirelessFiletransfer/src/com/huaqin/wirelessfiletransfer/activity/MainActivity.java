package com.huaqin.wirelessfiletransfer.activity;

import org.xerrard.util.WifiUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.huaqin.wirelessfiletransfer.MainApplication;
import com.huaqin.wirelessfiletransfer.R;
import com.huaqin.wirelessfiletransfer.service.MainService;

public class MainActivity extends Activity implements OnClickListener {
    private TextView mWFServiceState;
    private Button mSetupBtn;
    private Intent intent;
    protected MainApplication app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MainApplication) getApplication();
        setContentView(R.layout.activity_main);
        initRes();
    }



 
    
    
    
    private void initRes() {
        mWFServiceState = (TextView) findViewById(R.id.wfservice_status);
        mSetupBtn = (Button) findViewById(R.id.setupservicebtn);
        mSetupBtn.setOnClickListener(this);
        intent = new Intent(MainActivity.this, MainService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (app.mServiceSetup) {
            mWFServiceState.setText(getString(R.string.service_running));
            mSetupBtn.setText(R.string.stopservice);
        }
        else {
            mWFServiceState.setText(getString(R.string.service_not_running));
            mSetupBtn.setText(R.string.startservice);
        }

    }

    @Override
    public void onClick(View v) {
        if (v instanceof Button) {
            if (v.equals(mSetupBtn)) {
                if (app.mServiceSetup) {
                    stopService(intent);
                }
                else {
                    if (!WifiUtil.isWifiEnabled(this)) {
                        WifiUtil.enableWifi(this); // 确保wifi打开
                    }
                    startService(intent);
                }
                finish();
            }
        }

    }
}
