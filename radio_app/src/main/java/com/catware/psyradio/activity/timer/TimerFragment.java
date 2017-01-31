package com.catware.psyradio.activity.timer;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.AutoTransition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.catware.psyradio.fragment.BaseFragment;
import com.catware.psyradio.util.TimeUtils;
import com.psyradio.psyradio.R;

/**
 * Created by officemac on 08.09.16.
 */
public class TimerFragment extends BaseFragment implements View.OnClickListener {


    ImageButton stopImageButton;
    TextView timeTextView;
    TimerController timerController;
    long timeInSeconds = 0;
    boolean isTwoPointVisible;


    public static TimerFragment getInstance() {
        TimerFragment timerFragment = new TimerFragment();
        return timerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        stopImageButton = (ImageButton) view.findViewById(R.id.ft_stop_image_button);
        timeTextView = (TextView) view.findViewById(R.id.ft_time_text_view);
        stopImageButton.setOnClickListener(this);
        timeTextView.setText(TimeUtils.getTimeWithPoints(timeInSeconds));
        isTwoPointVisible = false;

    }


    public void setOnTimerPrepareListner(TimerController timerController) {
        this.timerController = timerController;
    }

    public void setTimeInSeconds(long timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    @Override
    public void onClick(View v) {
        if (timerController != null) {
            timerController.stopTimer();
        }
    }


    public void onUpdateTime(long timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
        timeTextView.setText(isTwoPointVisible ?
                TimeUtils.getTimeWithPoints(timeInSeconds) :
                TimeUtils.getTimeWithoutPoints(timeInSeconds));
        isTwoPointVisible = !isTwoPointVisible;
    }


}
