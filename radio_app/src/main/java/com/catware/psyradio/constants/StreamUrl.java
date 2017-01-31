package com.catware.psyradio.constants;

import java.io.Serializable;

/**
 * Created by officemac on 14.09.16.
 */
public enum StreamUrl implements Serializable {

    STREAM_URL_64("http://stream.psyradio.com.ua:8000/64kbps"),
    STREAM_URL_128("http://stream.psyradio.com.ua:8000/128kbps"),
    STREAM_URL_256("http://stream.psyradio.com.ua:8000/256kbps");

    String url;

    StreamUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public boolean equals(StreamUrl streamUrl) {
        return toString().equalsIgnoreCase(streamUrl.toString());
    }

    public boolean equalsUrl(StreamUrl streamUrl) {
        return url.equalsIgnoreCase(streamUrl.getUrl());
    }

}
