package com.catware.psyradio.view;

import android.content.Context;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.catware.psyradio.manager.SettingManager;
import com.catware.psyradio.util.TimeUtils;
import com.psyradio.psyradio.R;

import java.util.IllegalFormatException;

/**
 * Created by officemac on 07.09.16.
 */
public class TimerView extends LinearLayout implements View.OnClickListener, View.OnLongClickListener {

    static final int MAX_VALUE = 9;
    static final int MIN_VALUE = 0;

    ImageButton clearImageButton;

    SettingManager settingManager;

    TextView[] timesTextViews = new TextView[3];
    int[] timesComponentArray = new int[3];
    long timeInSecconds = 0;


    public TimerView(Context context) {
        super(context);
        inintKeyboardLayout(context, null);
    }

    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inintKeyboardLayout(context, attrs);
    }

    public TimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inintKeyboardLayout(context, attrs);
    }


    private void inintKeyboardLayout(Context context, AttributeSet attrs) {
        settingManager = SettingManager.getInstance();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_timer_layout, this);
        clearImageButton = (ImageButton) findViewById(R.id.tl_clear_image_button);
        clearImageButton.setOnClickListener(this);
        clearImageButton.setOnLongClickListener(this);

        timesTextViews[0] =(TextView) findViewById(R.id.tl_minuts_text_view);
        timesTextViews[1] = (TextView) findViewById(R.id.tl_minuts_tenth_text_view);//minutes tenth
        timesTextViews[2] = (TextView) findViewById(R.id.tl_hours_text_view);//hours

       // textViews[0] = minutTextView;
      //  textViews[1] = minutTenthTextView;
      //  textViews[2] = hourTextView;
        setTimerSettings();
    }


    public void setValue(int value) {
        if (value > MAX_VALUE || value < MIN_VALUE) {
            return;
        }

        shiftEnterValueArray();
        timesComponentArray[0] = value;
        updateViewsTimes();
        updateTime();
    }

    public void deleteValue() {
        if (timeInSecconds == 0) {
            return;
        }
        shiftDeleteValueArray();
        timesComponentArray[timesComponentArray.length - 1] = 0;
        updateViewsTimes();
        updateTime();
    }

    public void clearAllValue() {
        for (int i = 0; i < timesTextViews.length; i++) {
            timesComponentArray[i] = 0;
            timesTextViews[i].setText(String.valueOf(0));
        }
        updateTime();

    }

    private void shiftEnterValueArray() {
        for (int i = timesComponentArray.length - 1; i > 0; i--) {
            timesComponentArray[i] = timesComponentArray[i - 1];
        }
    }

    private void shiftDeleteValueArray() {
        for (int i = 0; i < timesComponentArray.length - 1; i++) {
            timesComponentArray[i] = timesComponentArray[i + 1];
        }
    }

    private void updateViewsTimes() {
        for (int i = 0; i < timesComponentArray.length; i++) {
            timesTextViews[i].setText(String.valueOf(timesComponentArray[i]));
        }
    }

    private void updateTimesFromViews() {
        for (int i = 0; i < timesComponentArray.length; i++) {
            timesComponentArray[i] = Integer.valueOf(timesTextViews[i].getText().toString());
        }
    }


    private void updateTime() {
        int hours = Integer.valueOf( timesTextViews[2].getText().toString());
        String minutsString =  timesTextViews[1].getText().toString() +  timesTextViews[0].getText().toString();
        int minuts = Integer.valueOf(minutsString);
        timeInSecconds = hours * 60 * 60 + minuts * 60;
        settingManager.saveUserTime(getContext(), timeInSecconds);
    }

    public long getTimeInSecconds() {
        return timeInSecconds;
    }

    private void setTimerSettings() {
        this.timeInSecconds = settingManager.getUserTime(getContext());
        int hours = TimeUtils.getHours(timeInSecconds);
        timesTextViews[2].setText(String.valueOf(hours));
        int minutes = TimeUtils.getMinutes(timeInSecconds);
        if (minutes > 9) {

            String minuteString = String.valueOf(minutes);
            if (minuteString.length() == 2) {
                timesTextViews[1].setText(minuteString.substring(0, 1));
                timesTextViews[0].setText(minuteString.substring(1));
            } else {
                timesTextViews[1].setText(String.valueOf(0));
                timesTextViews[0].setText(String.valueOf(0));
            }
        } else {
            timesTextViews[1].setText(String.valueOf(0));
            timesTextViews[0].setText(String.valueOf(minutes));
        }
        updateTimesFromViews();

    }


    @Override
    public void onClick(View v) {
        deleteValue();
    }

    @Override
    public boolean onLongClick(View v) {
        if (timeInSecconds != 0) {
            Vibrator vibe = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(50);
            clearAllValue();
            return true;
        }
        return false;
    }

}
