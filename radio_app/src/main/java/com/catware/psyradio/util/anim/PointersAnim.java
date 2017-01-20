package com.catware.psyradio.util.anim;

import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by officemac on 19.09.16.
 */
public class PointersAnim {


    Timer timer;
    TimerTask timerTask;
    //для того, чтобы сохранить текст по центру и избежать скачков при анимации
    String[] pointersArray = new String[]{".<font color=#FFFFFF>..</font>", "..<font color=#FFFFFF>.</font>", "..."};
    int position = 0;
    public Long delay = 1000l;

    public void stratPointersAnimation(final TextView textView, final String text, @Nullable Long delay) {
        if (delay != null) {
            this.delay = delay;
        }
        if (textView == null || text == null) {
            return;
        }

        if (timer == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder()
                                    .append(text)
                                    .append(pointersArray[position]);
                            Log.d("updateStatus", stringBuilder.toString() +" "+position);
                            textView.setText(Html.fromHtml(stringBuilder.toString()));
                            updatePosition();
                        }
                    });


                }
            };
            timer.schedule(timerTask, 0, 1000);
        }
    }

    public void stopPointerAnimation() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

    }

    private void updatePosition() {
        if (position == pointersArray.length - 1) {
            position = 0;
        } else {
            position++;
        }

    }


}
