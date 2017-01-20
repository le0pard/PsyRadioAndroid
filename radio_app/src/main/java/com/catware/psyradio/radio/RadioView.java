package com.catware.psyradio.radio;

/**
 * Created by officemac on 06.09.16.
 */
public interface RadioView {

    void onStreamStart(StreamStatus streamStatus);

    void onStreamPrepared(StreamStatus streamStatus);

    void onStreamStop(String message,StreamStatus streamStatus);

    void onInfoChanged(String message,StreamStatus streamStatus);

    void onStreamFail(String message,StreamStatus streamStatus);
}
