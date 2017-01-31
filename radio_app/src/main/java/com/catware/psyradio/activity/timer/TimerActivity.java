package com.catware.psyradio.activity.timer;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.transition.AutoTransition;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.catware.psyradio.activity.BaseActivity;
import com.catware.psyradio.radio.RadioService;
import com.catware.psyradio.radio.PlaybackTimer;
import com.catware.psyradio.view.TimerView;
import com.psyradio.psyradio.R;

import org.w3c.dom.Text;

/**
 * Created by officemac on 07.09.16.
 */
public class TimerActivity extends BaseActivity implements View.OnClickListener, TimerController, PlaybackTimer.OnTimerListner {

    final static String TIMER_SETTING_TAG = "settingTag";
    final static String TIMER_TAG = "timerTag";

    protected ImageButton backImageButton;
    protected TextView titleTextView;

    protected SettingTimerFragment settingTimerFragment;
    protected TimerFragment timerFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        titleTextView = (TextView) findViewById(R.id.at_title_text_view);
        titleTextView.setVisibility(View.GONE);
        backImageButton = (ImageButton) findViewById(R.id.at_back_image_button);
        backImageButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindPlayerService();
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindService(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    public void onClick(View v) {
        finish();
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }


    protected void replaceSettingFragment() {
        settingTimerFragment = SettingTimerFragment.getInstance();
        settingTimerFragment.setOnTimerPrepareListner(this);
        getSupportFragmentManager().beginTransaction()
                .disallowAddToBackStack()
                .replace(R.id.at_fragment_container, settingTimerFragment, TIMER_SETTING_TAG)
                .commit();

        titleTextView.setVisibility(View.VISIBLE);

    }


    protected void replaceTimerFragment() {
        timerFragment = TimerFragment.getInstance();
        timerFragment.setOnTimerPrepareListner(this);
        timerFragment.setTimeInSeconds(radioService.getTimerTime());

        getSupportFragmentManager()
                .beginTransaction()
                .disallowAddToBackStack()
                .replace(R.id.at_fragment_container, timerFragment, TIMER_TAG)
                .commit();

        titleTextView.setVisibility(View.GONE);

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        radioService.setOnTimerListner(this);
        if (radioService.isTimerStarted()) {
            replaceTimerFragment();

        } else {
            replaceSettingFragment();
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        super.onServiceDisconnected(name);
        Log.d("UpdateTimer", "onServiceDisconnected");
    }

    @Override
    public void startTimer(long timeInSeconds) {
        if (radioService == null) {
            return;
        }
        radioService.startTimer(timeInSeconds);

    }

    @Override
    public void stopTimer() {
        if (radioService == null) {
            return;
        }
        radioService.stopTimer();
    }

    @Override
    public void onUpdateTime(long timeInSeconds) {
        Log.d("UpdateTimer", timeInSeconds + " ");
        if (timerFragment != null) {
            timerFragment.onUpdateTime(timeInSeconds);
        }
    }

    @Override
    public void onTimerStarted(long timeInSeconds) {
         replaceTimerFragment();
    }

    @Override
    public void onTimerStoped(long timeInSeconds) {
        replaceSettingFragment();
    }
}
