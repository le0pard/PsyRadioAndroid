package com.catware.psyradio.radio;

import android.os.Handler;
import android.os.Looper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by officemac on 08.09.16.
 */
public class PlaybackTimer {

    public static final long DEAFAULT_INTERVAL = 1000;

    protected long timeInSeconds;

    protected long interval = DEAFAULT_INTERVAL;
    //listners
    protected OnTimerListner onTimerListner;
    protected OnTimerFinishListner onTimerFinishedListner;

    protected Timer timer;
    protected TimerTask timerTask;
    protected Handler handler;
    private static PlaybackTimer playbackTimer = null;

    private boolean isTimerStarted;

    private PlaybackTimer() {
    }

    public static PlaybackTimer getInstance() {
        if (playbackTimer == null) {
            playbackTimer = new PlaybackTimer();
        }
        return playbackTimer;
    }


    //region timers methods
    public void start() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (onTimerListner != null) {
            onTimerListner.onTimerStarted(timeInSeconds);
        }
        isTimerStarted = true;
        timer = new Timer();

        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (timeInSeconds <= 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (onTimerFinishedListner != null) {
                                onTimerFinishedListner.onTimerFinished();
                            }
                        }
                    });

                    stop();
                    return;
                }

                timeInSeconds = timeInSeconds - (interval / 1000);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (onTimerListner != null) {
                            onTimerListner.onUpdateTime(timeInSeconds);
                        }

                    }
                });


            }
        };
        timer.schedule(timerTask, 0, interval);


    }

    public void stop() {
        if (timer != null) {
            isTimerStarted = false;
            timer.cancel();
            timer = null;
            timeInSeconds = 0;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (onTimerListner != null) {
                        onTimerListner.onTimerStoped(timeInSeconds);
                    }
                }
            });

        }

    }
    //endregion


    //region setters and getters

    public boolean isTimerStarted() {
        return isTimerStarted;
    }

    public void setOnTimerFinishedListner(OnTimerFinishListner onTimerFinishedListner) {
        this.onTimerFinishedListner = onTimerFinishedListner;
    }

    public void setOnTimerListner(OnTimerListner onTimerListner) {
        this.onTimerListner = onTimerListner;
    }

    public long getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(long timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }
    //endregion


    //region listners
    public interface OnTimerListner {
        void onUpdateTime(long timeInSeconds);

        void onTimerStarted(long timeInSeconds);

        void onTimerStoped(long timeInSeconds);

    }

    public interface OnTimerFinishListner {

        void onTimerFinished();
    }
    //endregion

}
