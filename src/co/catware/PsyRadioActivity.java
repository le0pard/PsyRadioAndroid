package co.catware;

import co.catware.live.LiveShowPresenter;
import co.catware.live.LiveShowService;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class PsyRadioActivity extends Activity implements OnClickListener, SeekBar.OnSeekBarChangeListener {
	
	protected LiveShowService service;

	protected BroadcastReceiver onPlaybackState = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			updateVisualState();
		}
	};
	private ServiceConnection onService = new ServiceConnection() {
		public void onServiceDisconnected(ComponentName name) {
		}

		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = ((LiveShowService.LocalBinder) binder).getService();
			updateVisualState();
		}
	};
	private String[] statusLabels;
	private LiveShowPresenter visitor;
	private Animation rotate_animation;
	private SeekBar volumeSeekBar;
	private AudioManager audioManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        rotate_animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        
		visitor = new LiveShowPresenter(this);
		statusLabels = getResources().getStringArray(
				R.array.live_show_status_labels);
		
		View mainButton = findViewById(R.id.live_action_shitcher);
		mainButton.setOnClickListener(this);
		
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	    int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		volumeSeekBar = (SeekBar) findViewById(R.id.seekbar_volume);
		volumeSeekBar.setOnSeekBarChangeListener(this);
		volumeSeekBar.setMax(maxVolume);
		volumeSeekBar.setProgress(curVolume);
		
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent i = new Intent(this, LiveShowService.class);
		startService(i);
		bindService(i, onService, 0);
		registerReceiver(onPlaybackState, new IntentFilter(
				LiveShowService.PLAYBACK_STATE_CHANGED));
	}

	@Override
	protected void onStop() {
		unregisterReceiver(onPlaybackState);
		unbindService(onService);
		service = null;
		super.onStop();
	}

	public void onButtonPressed(View v) {
		visitor.switchPlaybackState(service.getCurrentState());
	}

	protected void updateVisualState() {
		if (service != null){
			service.acceptVisitor(visitor);
			if (service.streamMetaTitle != null){
				setStreamingTitle(service.streamMetaTitle);
			} else {
				setStreamingTitle("");
			}
		}
	}

	public LiveShowService getService() {
		return service;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
     if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) { 
           int index = volumeSeekBar.getProgress(); 
           volumeSeekBar.setProgress(index + 1); 
           return true; 
     } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
           int index = volumeSeekBar.getProgress(); 
           volumeSeekBar.setProgress(index - 1); 
           return true; 
     }
     return super.onKeyDown(keyCode, event); 
    }
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.live_action_shitcher:
				this.onButtonPressed(v);
				break;
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}
	
	

	public void setButtonState(int labelId, boolean enabled, int state) {
		//Button button = (Button) findViewById(R.id.live_show_action_button);
		//button.setText(buttonLabels[labelId]);
		//button.setEnabled(enabled);
		switch (state){
			case 1:
				ImageView spinner = (ImageView) findViewById(R.id.live_action_button);
				spinner.startAnimation(rotate_animation);
				break;
			default:
				rotate_animation.cancel();
		}
	}

	public void setStatusLabel(int labelId) {
		TextView view = (TextView) findViewById(R.id.playback_state_label);
		view.setText(statusLabels[labelId]);
	}

	public void setStreamingTitle(String text) {
		TextView timerLabel = (TextView) findViewById(R.id.live_title_label);
		timerLabel.setText(text);
	}

	public void showHelpText(boolean visible) {
		View view = findViewById(R.id.live_show_hint);
		int visibility = (visible) ? View.VISIBLE : View.INVISIBLE;
		view.setVisibility(visibility);
	}
}