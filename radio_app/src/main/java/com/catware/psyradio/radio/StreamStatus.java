package com.catware.psyradio.radio;

/**
 * Created by officemac on 19.09.16.
 */
public enum StreamStatus {

    DEFAULT(0),
    CONNECTING(1),
    RESUMING(2),
    PLAY(3),
    PAUSE(4),
    STOP(5);

    int code;

    StreamStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean equals(StreamStatus streamStatus) {
        return code == streamStatus.getCode();
    }
}
