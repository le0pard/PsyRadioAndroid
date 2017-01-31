package com.catware.psyradio.activity.timer;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.AutoTransition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.catware.psyradio.fragment.BaseFragment;
import com.catware.psyradio.view.KeyboardView;
import com.catware.psyradio.view.TimerView;
import com.psyradio.psyradio.R;

/**
 * Created by officemac on 08.09.16.
 */
public class SettingTimerFragment extends BaseFragment implements KeyboardView.OnKeyboardClickListner, View.OnClickListener {


    KeyboardView keyboardView;
    TimerView timerView;
    ImageButton startImageButton;

    TimerController timerController;

    public static SettingTimerFragment getInstance() {
        SettingTimerFragment settingTimerFragment = new SettingTimerFragment();
        return settingTimerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_timer, container, false);
        keyboardView = (KeyboardView) view.findViewById(R.id.st_keyboard_view);
        timerView = (TimerView) view.findViewById(R.id.st_timer_view);
        startImageButton = (ImageButton) view.findViewById(R.id.st_start_image_button);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startImageButton.setOnClickListener(this);
        keyboardView.setOnKeyboardClickListner(this);
    }

    @Override
    public void onKeyClick(View view, int number) {
        timerView.setValue(number);
    }


    @Override
    public void onClick(View v) {
        if (timerView.getTimeInSecconds() == 0) {
            return;
        }
        if (timerController != null) {
            timerController.startTimer(timerView.getTimeInSecconds());
        }
    }

    public void setOnTimerPrepareListner(TimerController timerController) {
        this.timerController = timerController;
    }
}
