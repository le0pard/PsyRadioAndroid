package co.catware.live;

import android.util.Log;
import co.catware.PsyRadioActivity;
import co.catware.live.LiveShowState.*;

public class LiveShowPresenter implements ILiveShowVisitor {

	private PsyRadioActivity activity;
	private boolean isActive;

	public LiveShowPresenter(PsyRadioActivity activity) {
		this.activity = activity;
	}

	public void onIdle(Idle state) {
		beInactiveState();
	}

	public void onWaiting(Waiting state) {
		beActiveState(state, 2, true, true);
	}

	public void onConnecting(Connecting state) {
		beActiveState(state, 1, false, true);
	}

	public void onPlaying(Playing state) {
		beActiveState(state, 3, false, true);
	}

	public void onStopping(Stopping state) {
		beActiveState(state, 4, false, false);
	}

	public void switchPlaybackState(LiveShowState state) {
		if (isActive) {
			state.stopPlayback();
		} else {
			state.startPlayback();
		}
	}

	private void beActiveState(LiveShowState state, int labelStringId,
			boolean isHelpTextVisible, boolean buttonEnabled) {
		isActive = true;
		activity.setStatusLabel(labelStringId);
		activity.showHelpText(isHelpTextVisible);
		activity.setButtonState(0, buttonEnabled);
	}

	private void beInactiveState() {
		isActive = false;
		activity.setStatusLabel(0);
		activity.showHelpText(false);
		activity.setButtonState(1, true);
	}

	@Override
	public void onUpdateSoundTitle(String title) {
		Log.i("title", title);
	}

}

