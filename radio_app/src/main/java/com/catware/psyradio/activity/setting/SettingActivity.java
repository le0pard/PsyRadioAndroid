package com.catware.psyradio.activity.setting;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.catware.psyradio.activity.BaseActivity;
import com.catware.psyradio.constants.StreamUrl;
import com.psyradio.psyradio.R;

/**
 * Created by officemac on 14.09.16.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {


    RadioButton lowStreamButton;
    RadioButton mediumStreamButton;
    RadioButton hightStreamButton;

    ImageButton backImageButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        backImageButton = (ImageButton) findViewById(R.id.as_back_image_button);
        lowStreamButton = (RadioButton) findViewById(R.id.as_low_stream_radio_button);
        mediumStreamButton = (RadioButton) findViewById(R.id.as_medium_stream_radio_button);
        hightStreamButton = (RadioButton) findViewById(R.id.as_hight_stream_radio_button);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        updateStramButton();
        lowStreamButton.setOnClickListener(this);
        mediumStreamButton.setOnClickListener(this);
        hightStreamButton.setOnClickListener(this);
        backImageButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindPlayerService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    @Override
    public void onClick(View buttonView) {

        if (buttonView instanceof RadioButton) {
            updateStreamUrl((RadioButton) buttonView);
        }

        if (buttonView.getId() == backImageButton.getId()) {
            finish();
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        }

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
    }

    protected void updateStreamUrl(RadioButton buttonView) {

        if (buttonView.getId() == lowStreamButton.getId()) {
            settingManager.saveStreamUrl(this, StreamUrl.STREAM_URL_64.getUrl());
            selectLowSettings();
        }
        if (buttonView.getId() == mediumStreamButton.getId()) {
            settingManager.saveStreamUrl(this, StreamUrl.STREAM_URL_128.getUrl());
            selectMediumSettings();
        }
        if (buttonView.getId() == hightStreamButton.getId()) {
            settingManager.saveStreamUrl(this, StreamUrl.STREAM_URL_256.getUrl());
            selectHightSettings();
        }

        if (radioService != null && radioService.isStreamStarted()) {
            radioService.startStream();
        }

    }

    protected void updateStramButton() {
        String currentStream = settingManager.getStreamUrl(this);
        if (currentStream.equalsIgnoreCase(StreamUrl.STREAM_URL_64.getUrl())) {
            selectLowSettings();
        } else if (currentStream.equalsIgnoreCase(StreamUrl.STREAM_URL_128.getUrl())) {
            selectMediumSettings();
        } else if (currentStream.equalsIgnoreCase(StreamUrl.STREAM_URL_256.getUrl())) {
            selectHightSettings();
        }
    }

    protected void selectLowSettings() {
        lowStreamButton.setChecked(true);
        mediumStreamButton.setChecked(false);
        hightStreamButton.setChecked(false);
    }

    protected void selectMediumSettings() {
        lowStreamButton.setChecked(false);
        mediumStreamButton.setChecked(true);
        hightStreamButton.setChecked(false);
    }

    protected void selectHightSettings() {
        lowStreamButton.setChecked(false);
        mediumStreamButton.setChecked(false);
        hightStreamButton.setChecked(true);
    }


}
