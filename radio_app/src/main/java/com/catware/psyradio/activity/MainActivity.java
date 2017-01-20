package com.catware.psyradio.activity;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.catware.psyradio.activity.setting.SettingActivity;
import com.catware.psyradio.activity.timer.TimerActivity;
import com.catware.psyradio.radio.PlaybackTimer;
import com.catware.psyradio.radio.RadioService;
import com.catware.psyradio.radio.RadioView;
import com.catware.psyradio.radio.StreamStatus;
import com.catware.psyradio.util.TimeUtils;
import com.catware.psyradio.util.anim.PointersAnim;
import com.psyradio.psyradio.R;

public class MainActivity extends BaseActivity implements View.OnClickListener, RadioView, PlaybackTimer.OnTimerListner {

    //ui
    ImageButton playImageButton;
    ImageButton timerImageButton;
    ImageButton settingsImageButton;
    TextView statusTextView;
    TextView timeTextView;

    boolean isShowPoints = true;

    private PointersAnim pointersAnim;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playImageButton = (ImageButton) findViewById(R.id.am_play_image_button);
        timerImageButton = (ImageButton) findViewById(R.id.am_timer_image_button);
        settingsImageButton = (ImageButton) findViewById(R.id.am_settings_image_button);
        statusTextView = (TextView) findViewById(R.id.am_status_text_view);
        timeTextView = (TextView) findViewById(R.id.am_time_text_view);
        pointersAnim = new PointersAnim();

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        timerImageButton.setOnClickListener(this);
        settingsImageButton.setOnClickListener(this);
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
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);

        radioService.setOnTimerListner(this);
        radioService.setRadioView(this);
        playImageButton.setOnClickListener(this);
        updatePlayButton(radioService.isStreamStarted());
        updateStatusTextView(radioService.getStateInfo(), radioService.getStreamStatus());
        updateTimeTextView();
        radioService.updateTrackInfoIfNeed();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        super.onServiceDisconnected(name);
        Log.d("TimeLife", "onServiceDisconnected");
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == playImageButton.getId()) {
            if (radioService == null) {
                return;
            }
            if (radioService.isStreamStarted()) {
                radioService.stopStream();
            } else {
                radioService.startStream();
            }
        }
        if (view.getId() == timerImageButton.getId()) {
            onTimerClick();
        }


        if (view.getId() == settingsImageButton.getId()) {
            onSettingClick();
        }

    }

    //region onclicks methods
    public void onTimerClick() {
        Intent intent = new Intent(this, TimerActivity.class);
        startActivity(intent);


    }

    public void onSettingClick() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);

    }
    //endregion


    //region update ui methods
    protected void updatePlayButton(boolean isPlayerStarted) {
        playImageButton.setImageResource(isPlayerStarted ? R.drawable.ic_pause : R.drawable.ic_play);

    }

    protected void updateStatusTextView(String message, StreamStatus streamStatus) {
        Log.d("updateStatus", streamStatus.toString());
        if (streamStatus.equals(StreamStatus.CONNECTING) || streamStatus.equals(StreamStatus.RESUMING)) {
            pointersAnim.stratPointersAnimation(statusTextView, getString(R.string.connecting), null);
        } else {
            pointersAnim.stopPointerAnimation();
            statusTextView.setText(message);
        }


    }

    protected void updateTimeTextView() {
        timeTextView.setVisibility(radioService.getTimerTime() > 0 ? View.VISIBLE : View.INVISIBLE);
        timeTextView.setText(TimeUtils.getTimeWithPoints(radioService.getTimerTime()));

    }


    //endregion

    //region Payer view methods
    @Override
    public void onStreamStart(StreamStatus streamStatus) {
        updatePlayButton(true);
        updateStatusTextView(getString(R.string.connecting_with_points), streamStatus);
    }

    @Override
    public void onStreamPrepared(StreamStatus streamStatus) {

    }

    @Override
    public void onStreamStop(String message, StreamStatus streamStatus) {
        updateStatusTextView(message, streamStatus);
        updatePlayButton(false);
    }

    @Override
    public void onInfoChanged(String message, StreamStatus streamStatus) {
        updateStatusTextView(message, streamStatus);
    }

    @Override
    public void onStreamFail(String message, StreamStatus streamStatus) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUpdateTime(long timeInSeconds) {
        timeTextView.setText(isShowPoints ?
                TimeUtils.getTimeWithPoints(timeInSeconds)
                : TimeUtils.getTimeWithoutPoints(timeInSeconds));
        isShowPoints = !isShowPoints;
    }

    @Override
    public void onTimerStarted(long timeInSeconds) {
        timeTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTimerStoped(long timeInSeconds) {
        timeTextView.setVisibility(View.INVISIBLE);
    }
    //endregion
}
