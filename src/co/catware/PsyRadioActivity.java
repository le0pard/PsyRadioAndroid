package co.catware;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.widget.TextView;

public class PsyRadioActivity extends Activity {
	
	private MediaPlayer player;
	private TextView text;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        text = (TextView) findViewById(R.id.testStr);
        
        /* test */
        player = new RadioPlayer();
        try {
            player.setDataSource("http://stream.psyradio.com.ua:8000/256kbps");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

            public void onBufferingUpdate(MediaPlayer mp, int percent) {
            	text.setText("Buffering " + String.valueOf(percent));
            }
        });
        
        player.prepareAsync();
        
        player.setOnPreparedListener(new OnPreparedListener() {

            public void onPrepared(MediaPlayer mp) {
                player.start();
                text.setText("Play");
            }
        });
    }
}