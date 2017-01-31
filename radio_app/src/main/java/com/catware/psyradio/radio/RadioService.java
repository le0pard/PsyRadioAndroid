package com.catware.psyradio.radio;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;


import com.catware.psyradio.manager.SettingManager;
import com.catware.psyradio.util.IcyStreamMeta;
import com.catware.psyradio.activity.MainActivity;
import com.catware.psyradio.util.TimeUtils;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.Util;
import com.psyradio.psyradio.R;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by Alexandr on 03.09.2015.
 */
public class RadioService extends Service implements RadioPresenter, IcyStreamMeta.OnStreamMetaListner, PlaybackTimer.OnTimerFinishListner, PlaybackTimer.OnTimerListner, ExoPlayer.Listener {

    private static final String USER_AGGENT = "PsyRadio";
    private static final long RESTARTING_TIME = 3000;
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;
    private static final int MIN_BUFFER_MILLISECOND = 5000;
    private static final int MIN_REBUFFER_MILLISECOND = 5000;
    private static final int RENDER_COUNT = 1;
    public static final int RADIO_NOTIFICATION_ID = 1;

    public static final String ACTION_RADIO = "com.catware.psyradio.radio.RadioService";

    protected ExoPlayer exoPlayer;
    protected RadioView radioView;
    protected StreamStatus streamStatus = StreamStatus.DEFAULT;

    boolean isShowPoints = true;
    protected String CONNECTING_STATUS;
    protected String RESUMING_STATUS;
    protected String trackName;
    protected String urlStream;
    protected String currentTime = "";

    private int playImageId = R.drawable.ic_play_small;
    private int pauseImageId = R.drawable.ic_pause_small;

    protected PlaybackTimer playbackTimer;
    protected PlaybackTimer.OnTimerListner onTimerListner;
    protected NotificationCompat.Builder notificationBuilder;

    private MusicIntentReceiver musicIntentReceiver;


    //region lifecycle methods
    @Override
    public void onCreate() {
        super.onCreate();
        CONNECTING_STATUS = getString(R.string.connecting);
        RESUMING_STATUS = getString(R.string.resuming_with_points);
        registearHeadsetReciver();
        addPhoneStateListner();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {

                if (action.equalsIgnoreCase(ACTION_RADIO)) {
                    if (streamStatus.equals(StreamStatus.CONNECTING) || streamStatus.equals(StreamStatus.PLAY) || streamStatus.equals(StreamStatus.RESUMING)) {
                        pauseStream();
                    } else {
                        resumeStream();
                    }
                }

            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new RadioBinder();
    }
    //endregion


    //region exoPlayer listners methods
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playWhenReady && playbackState == ExoPlayer.STATE_READY) {
            if (radioView != null) {
                radioView.onStreamPrepared(streamStatus);
                streamStatus = StreamStatus.PLAY;
                // isStreamRestarting = false;
            }
            updateTrackInfoIfNeed();
        }
        if (playbackState == ExoPlayer.STATE_BUFFERING) {
            if (radioView != null) {
                trackName = null;
                // notifyTrackInfoChanged(getString(R.string.connecting), R.drawable.ic_pause);
            }
        }

    }

    @Override
    public void onPlayWhenReadyCommitted() {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

        if (radioView != null && !streamStatus.equals(StreamStatus.RESUMING)) {
            radioView.onStreamFail(getString(R.string.internet_connection_fail), streamStatus);
        }

        restartStream();
    }
    //endregion


    //region stream methods
    @Override
    public void startStream() {
        streamStatus = StreamStatus.CONNECTING;
        // isStreamStarted = true;
        if (radioView != null) {
            radioView.onStreamStart(streamStatus);
        }
        startForegroundWhitNotification();
        startPlayer();
    }


    @Override
    public void stopStream() {

        stopPlayer();
        streamStatus = StreamStatus.STOP;
        stopForeground(true);
        if (radioView != null) {
            radioView.onStreamStop("", streamStatus);
        }
    }

    @Override
    public void pauseStream() {
        streamStatus = StreamStatus.PAUSE;
        pausePlayer();
        stopForeground(false);
        updateNotification(getString(R.string.pause), "", playImageId);
        if (radioView != null) {
            radioView.onStreamStop(getString(R.string.pause), streamStatus);
        }
        stopTimer();
    }

    @Override
    public void resumeStream() {
        streamStatus = StreamStatus.RESUMING;
        if (radioView != null) {
            radioView.onStreamStart(streamStatus);
        }
        startPlayer();
        startForegroundWhitNotification();
        updateNotification(RESUMING_STATUS, currentTime, pauseImageId);

    }

    private void pausePlayer() {
        if (exoPlayer != null) {
            if (exoPlayer.isPlayWhenReadyCommitted()) {
                exoPlayer.stop();
            }
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private void stopPlayer() {
        if (exoPlayer != null) {
            if (exoPlayer.isPlayWhenReadyCommitted()) {
                exoPlayer.stop();
            }
            exoPlayer.release();
            exoPlayer = null;
        }


        if (playbackTimer != null) {
            if (playbackTimer.isTimerStarted()) {
                playbackTimer.stop();
            }
        }
    }

    private void restartStream() {

        if (streamStatus.equals(StreamStatus.STOP)) {
            return;
        }
        streamStatus = StreamStatus.RESUMING;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (streamStatus.equals(StreamStatus.STOP)) {
                    return;
                }
                startPlayer();
            }
        }, RESTARTING_TIME);
        if (radioView != null) {
            radioView.onInfoChanged(CONNECTING_STATUS, streamStatus);
        }
        updateNotification(CONNECTING_STATUS, currentTime, pauseImageId);

    }

    public void startPlayer() {
        urlStream = SettingManager.getInstance().getStreamUrl(this);
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }

        exoPlayer = ExoPlayer.Factory.newInstance(RENDER_COUNT, MIN_BUFFER_MILLISECOND, MIN_REBUFFER_MILLISECOND);
        exoPlayer.addListener(this);

        Uri radioUri = Uri.parse(urlStream);
        // Settings for exoPlayer
        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        String userAgent = Util.getUserAgent(this, USER_AGGENT);
        DataSource dataSource = new DefaultUriDataSource(this, null, userAgent);
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(radioUri, dataSource, allocator, BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT);
        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);
        // Prepare and start ExoPlayer
        exoPlayer.prepare(audioRenderer);
        exoPlayer.setPlayWhenReady(true);


    }

    //endregion


    //region timer methods
    public void startTimer(long timeInSeconds) {
        if (playbackTimer == null) {
            playbackTimer = PlaybackTimer.getInstance();
        }
        playbackTimer.setOnTimerFinishedListner(this);
        playbackTimer.setTimeInSeconds(timeInSeconds);
        playbackTimer.start();

        if (streamStatus.equals(StreamStatus.STOP) || streamStatus.equals(StreamStatus.DEFAULT) || streamStatus.equals(StreamStatus.PAUSE)) {
            startStream();
        }
    }

    public void stopTimer() {
        if (playbackTimer == null) {
            return;
        }
        playbackTimer.stop();
    }

    public boolean isTimerStarted() {
        if (playbackTimer == null) {
            return false;
        }
        return playbackTimer.isTimerStarted();
    }

    public void setOnTimerListner(PlaybackTimer.OnTimerListner onTimerListner) {
        this.onTimerListner = onTimerListner;
        if (playbackTimer == null) {
            playbackTimer = PlaybackTimer.getInstance();
        }
        playbackTimer.setOnTimerListner(this);

    }

    public long getTimerTime() {
        if (playbackTimer == null) {
            playbackTimer = PlaybackTimer.getInstance();
        }
        return playbackTimer.getTimeInSeconds();
    }

    @Override
    public void onTimerFinished() {
        stopStream();
    }


    @Override
    public void onUpdateTime(long timeInSeconds) {
        if (onTimerListner != null) {
            onTimerListner.onUpdateTime(timeInSeconds);
        }
        isShowPoints = !isShowPoints;
        currentTime = isShowPoints ?
                TimeUtils.getTimeWithPoints(timeInSeconds)
                : TimeUtils.getTimeWithoutPoints(timeInSeconds);
        if (streamStatus.equals(StreamStatus.CONNECTING) || streamStatus.equals(StreamStatus.PLAY) || streamStatus.equals(StreamStatus.RESUMING)) {
            updateNotification(null, currentTime, -1);
        }


    }

    @Override
    public void onTimerStarted(long timeInSeconds) {
        if (onTimerListner != null) {
            onTimerListner.onTimerStarted(timeInSeconds);
        }
        currentTime = TimeUtils.getTimeWithPoints(timeInSeconds);
        if (streamStatus.equals(StreamStatus.CONNECTING) || streamStatus.equals(StreamStatus.PLAY)) {
            updateNotification(null, currentTime, -1);
        }
    }

    @Override
    public void onTimerStoped(long timeInSeconds) {
        currentTime = "";
        if (onTimerListner != null) {
            onTimerListner.onTimerStoped(timeInSeconds);
        }
        if (streamStatus.equals(StreamStatus.CONNECTING) || streamStatus.equals(StreamStatus.PLAY)) {
            updateNotification(null, currentTime, -1);
        }
    }

    //endregion


    private void startForegroundWhitNotification() {
        Notification notification = buildNotification(CONNECTING_STATUS, "", pauseImageId);
        startForeground(RADIO_NOTIFICATION_ID, notification);
    }

    private void updateNotification(String text, String time, int imageId) {
        Notification notification = buildNotification(text, time, imageId);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(RADIO_NOTIFICATION_ID, notification);

    }


    private Notification buildNotification(@Nullable String text, @Nullable String time, int pauseImageId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        //region custom notifications view
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_view);
        remoteViews.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
        if (text != null) {
            remoteViews.setTextViewText(R.id.text, text);
        }
        remoteViews.setTextViewText(R.id.title, getString(R.string.app_name));
        if (time != null) {
            remoteViews.setTextViewText(R.id.time, time);
        }
        if (pauseImageId != -1) {
            remoteViews.setImageViewResource(R.id.pause_image_view, pauseImageId);
        }
        setOnStopNotificationClick(remoteViews);
        //endregion

        if (notificationBuilder == null) {
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(ContextCompat.getColor(this, R.color.colorTransparent))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher);
        }

        notificationBuilder.setContent(remoteViews);
        return notificationBuilder.build();
    }

    private void setOnStopNotificationClick(RemoteViews remoteViews) {
        Intent clickIntent = new Intent(this, RadioService.class);
        clickIntent.setAction(ACTION_RADIO);
        PendingIntent clickPending = PendingIntent.getService(this, 0, clickIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.pause_image_view, clickPending);
    }


    //region track info methods
    public void updateTrackInfoIfNeed() {
        Log.d("onInfoUpdated", "updateTrackInfoIfNeed " + streamStatus.toString());
        if (exoPlayer == null) {
            return;
        }

        if (streamStatus.equals(StreamStatus.STOP) || streamStatus.equals(StreamStatus.RESUMING)) {
            return;
        }
        urlStream = SettingManager.getInstance().getStreamUrl(this);
        URL url = null;
        try {
            url = new URL(urlStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url == null) {
            return;
        }

        IcyStreamMeta icyStreamMeta = IcyStreamMeta.newBuild()
                .setUrl(url)
                .setOnStreamMetaListner(this)
                .build();
        icyStreamMeta.refreshMeta();

    }

    @Override
    public void onInfoUpdated(String artist, String track) {
        Log.d("onInfoUpdated", streamStatus.toString());
        trackName = new StringBuilder().append(artist).append(" - ").append(track).toString();
        notifyTrackInfoChanged(trackName, pauseImageId);
    }

    @Override
    public void onInfoFail() {

        trackName = null;
        notifyTrackInfoChanged(streamStatus.equals(StreamStatus.CONNECTING) ? CONNECTING_STATUS : "", pauseImageId);
    }

    private void notifyTrackInfoChanged(String message, int imageId) {

        if (streamStatus.equals(StreamStatus.STOP) || streamStatus.equals(StreamStatus.RESUMING)) {
            return;
        }

        if (radioView != null) {
            Log.d("onInfoUpdated", "radioView" + streamStatus.toString());
            radioView.onInfoChanged(message, streamStatus);
        }

        updateNotification(message, currentTime, imageId);
    }
    //endregion


    //region state methods
    public boolean isStreamStarted() {
        return streamStatus.equals(StreamStatus.PLAY) || streamStatus.equals(StreamStatus.CONNECTING) || streamStatus.equals(StreamStatus.RESUMING);
    }

    public String getStateInfo() {
        if (exoPlayer == null) {
            return "";
        }
        if (!(streamStatus.equals(StreamStatus.PLAY) && exoPlayer.isPlayWhenReadyCommitted()) || !streamStatus.equals(StreamStatus.RESUMING)) {

            return CONNECTING_STATUS;
        } else if (exoPlayer.isPlayWhenReadyCommitted()) {
            if (trackName != null) {
                return trackName;
            }

            return CONNECTING_STATUS;
        }


        return "";

    }

    public StreamStatus getStreamStatus() {
        return streamStatus;
    }

    //endregion


    //region setters and getters
    public void setRadioView(RadioView radioView) {
        this.radioView = radioView;
    }
    //endregion


    private void addPhoneStateListner() {
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    stopPlayer();
                    Log.d("addPhoneStateListner", "CALL_STATE_RINGING");
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                    if (streamStatus.equals(StreamStatus.PLAY) || streamStatus.equals(StreamStatus.CONNECTING)) {
                        startStream();
                    }
                    Log.d("addPhoneStateListner", "CALL_STATE_IDLE");
                    //
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    Log.d("addPhoneStateListner", "CALL_STATE_OFFHOOK");
                    //A call is dialing, active or on hold
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    private void registearHeadsetReciver() {
        musicIntentReceiver = new MusicIntentReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(musicIntentReceiver, filter);
    }


    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        if (!streamStatus.equals(StreamStatus.STOP) && !streamStatus.equals(StreamStatus.DEFAULT) ) {
                            stopStream();
                        }
                        break;
                    case 1:
                        break;
                    default:
                }
            }
        }
    }

    public class RadioBinder extends Binder {
        public RadioService getPlayer() {
            return RadioService.this;
        }
    }

}
