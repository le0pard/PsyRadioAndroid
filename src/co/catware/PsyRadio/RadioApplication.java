package co.catware.PsyRadio;

import android.app.Application;
import android.media.MediaPlayer;

public class RadioApplication extends Application {
	private MediaPlayer mediaPlayer;

	@Override
	public void onCreate() {
		super.onCreate();		
		mediaPlayer = new MediaPlayer();
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		mediaPlayer.release();
	}
	
	public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}
	
	public void setMediaPlayer(MediaPlayer instance) { 
		mediaPlayer = instance;
	}
}

