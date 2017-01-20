package com.catware.psyradio.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.psyradio.psyradio.R;

import java.util.IllegalFormatException;

/**
 * Created by officemac on 07.09.16.
 */
public class KeyboardView extends LinearLayout implements View.OnClickListener {

    static final int ROW_COUNT = 4;
    static final int KEY_COUNT = 10;

    TextView oneTextView;
    TextView twoTextView;
    TextView threeTextView;
    TextView fourTextView;
    TextView fiveTextView;
    TextView sixTextView;
    TextView sevenTextView;
    TextView eightTextView;
    TextView nineTextView;
    TextView zeroTextView;

    TextView[] keyboardViewArray = new TextView[KEY_COUNT];

    OnKeyboardClickListner onKeyboardClickListner;

    public KeyboardView(Context context) {
        super(context);
        Log.d("KeyboardView", "KeyboardView(Context context)");
        inintKeyboardLayout(context, null);
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("KeyboardView", "KeyboardView(Context context, AttributeSet attrs)");
        inintKeyboardLayout(context, attrs);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d("KeyboardView", "KeyboardView(Context context, AttributeSet attrs, int defStyleAttr)");
        inintKeyboardLayout(context, attrs);
    }

    public void setOnKeyboardClickListner(OnKeyboardClickListner onKeyboardClickListner) {
        this.onKeyboardClickListner = onKeyboardClickListner;
    }

    private void inintKeyboardLayout(Context context, AttributeSet attrs) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_keyboard_layout, this);
        oneTextView = (TextView) findViewById(R.id.one_text_view);
        twoTextView = (TextView) findViewById(R.id.two_text_view);
        threeTextView = (TextView) findViewById(R.id.three_text_view);
        fourTextView = (TextView) findViewById(R.id.four_text_view);
        fiveTextView = (TextView) findViewById(R.id.five_text_view);
        sixTextView = (TextView) findViewById(R.id.six_text_view);
        sevenTextView = (TextView) findViewById(R.id.seven_text_view);
        eightTextView = (TextView) findViewById(R.id.eight_text_view);
        nineTextView = (TextView) findViewById(R.id.nine_text_view);
        zeroTextView = (TextView) findViewById(R.id.zero_text_view);

        fillKeyArrays();
        setClickListners();
        updateKeyViewSize();

    }

    private void setClickListners() {
        for (int index = 0; index < keyboardViewArray.length; index++) {
            keyboardViewArray[index].setOnClickListener(this);
        }
    }

    private void fillKeyArrays() {
        keyboardViewArray[0] = oneTextView;
        keyboardViewArray[1] = twoTextView;
        keyboardViewArray[2] = threeTextView;
        keyboardViewArray[3] = fourTextView;
        keyboardViewArray[4] = fiveTextView;
        keyboardViewArray[5] = sixTextView;
        keyboardViewArray[6] = sevenTextView;
        keyboardViewArray[7] = eightTextView;
        keyboardViewArray[8] = nineTextView;
        keyboardViewArray[9] = zeroTextView;
    }

    public void updateKeyViewSize() {


        post(new Runnable() {
            @Override
            public void run() {

                int value = getWidth() > getHeight() ? getHeight() : getWidth();
                value = value / ROW_COUNT;

                for (int index = 0; index < keyboardViewArray.length; index++) {
                    keyboardViewArray[index].getLayoutParams().height = value;
                    keyboardViewArray[index].getLayoutParams().width = value;
                    keyboardViewArray[index].setWidth(value);
                    keyboardViewArray[index].setHeight(value);
                }



            }
        });


    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView && onKeyboardClickListner != null) {
            try {
                int number = Integer.parseInt(((TextView) v).getText().toString());
                onKeyboardClickListner.onKeyClick(v, number);

            } catch (IllegalFormatException e) {

            }

        }
    }

    public interface OnKeyboardClickListner {
        void onKeyClick(View view, int number);
    }
}
