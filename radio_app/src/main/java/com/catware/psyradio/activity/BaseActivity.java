package com.catware.psyradio.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.catware.psyradio.manager.SettingManager;
import com.catware.psyradio.radio.RadioService;
import com.psyradio.psyradio.R;

/**
 * Created by officemac on 08.09.16.
 */
public class BaseActivity extends AppCompatActivity implements ServiceConnection {

    protected SettingManager settingManager;
    protected RadioService radioService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingManager = SettingManager.getInstance();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d("UpdateTimer", "onServiceConnected");
        if (!(service instanceof RadioService.RadioBinder)) {
            throw new IllegalArgumentException();
        }
        if (radioService == null) {
            radioService = ((RadioService.RadioBinder) service).getPlayer();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    protected void bindPlayerService() {
        Intent playerIntent = new Intent(this, RadioService.class);
        startService(playerIntent);
        bindService(playerIntent, this, BIND_AUTO_CREATE);
    }
}
