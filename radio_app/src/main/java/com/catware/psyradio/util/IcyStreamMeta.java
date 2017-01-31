package com.catware.psyradio.util;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IcyStreamMeta implements Runnable {
    protected URL streamUrl;
    private Map<String, String> metadata;
    private OnStreamMetaListner onStreamMetaListner;


    public String getArtist() throws IOException {
        if (metadata == null) {
            return "";
        }

        if (!metadata.containsKey("StreamTitle"))
            return "";

        String streamTitle = metadata.get("StreamTitle");
        String title = streamTitle.substring(0, streamTitle.indexOf("-"));
        return title.trim();
    }

    /**
     * Get streamTitle
     *
     * @return String
     * @throws IOException
     */
    protected String getStreamTitle() throws IOException {
        if (metadata == null) {
            return "";
        }
        if (!metadata.containsKey("StreamTitle"))
            return "";

        return metadata.get("StreamTitle");
    }

    /**
     * Get title using stream's title
     *
     * @return String
     * @throws IOException
     */
    protected String getTitle() throws IOException {
        if (metadata == null) {
            return "";
        }

        if (!metadata.containsKey("StreamTitle"))
            return "";

        String streamTitle = metadata.get("StreamTitle");
        String artist = streamTitle.substring(streamTitle.indexOf("-") + 1);
        return artist.trim();
    }

    public Map<String, String> getMetadata() {
        if (metadata == null) {
            refreshMeta();
        }
        return metadata;
    }

    public void refreshMeta() {
        new Thread(this).start();
    }

    synchronized private boolean retreiveMetadata() throws IOException {
        URLConnection con = streamUrl.openConnection();
        con.setRequestProperty("Icy-MetaData", "1");
        con.setRequestProperty("Connection", "close");
        con.setRequestProperty("Accept", null);
        con.connect();
        int metaDataOffset = 0;
        Map<String, List<String>> headers = con.getHeaderFields();
        InputStream stream = con.getInputStream();

        if (headers.containsKey("icy-metaint")) {
            // Headers are sent via HTTP
            metaDataOffset = Integer.parseInt(headers.get("icy-metaint").get(0));
        } else {
            // Headers are sent within a stream
            StringBuilder strHeaders = new StringBuilder();
            char c;
            while ((c = (char) stream.read()) != -1) {
                strHeaders.append(c);
                if (strHeaders.length() > 5 && (strHeaders.substring((strHeaders.length() - 4), strHeaders.length()).equals("\r\n\r\n"))) {
                    // end of headers
                    break;
                }
            }

            // Match headers to get metadata offset within a stream
            Pattern p = Pattern.compile("\\r\\n(icy-metaint):\\s*(.*)\\r\\n");
            Matcher m = p.matcher(strHeaders.toString());
            if (m.find()) {
                metaDataOffset = Integer.parseInt(m.group(2));
            }
        }

        // In case no data was sent
        if (metaDataOffset == 0) {
            return false;
        }

        // Read metadata
        int b;
        int count = 0;
        int metaDataLength = 4080; // 4080 is the max length
        boolean inData = false;
        StringBuilder metaData = new StringBuilder();
        // Stream position should be either at the beginning or right after headers
        while ((b = stream.read()) != -1) {
            count++;

            // Length of the metadata
            if (count == metaDataOffset + 1) {
                metaDataLength = b * 16;
            }

            if (count > metaDataOffset + 1 && count < (metaDataOffset + metaDataLength)) {
                inData = true;
            } else {
                inData = false;
            }
            if (inData) {
                if (b != 0) {
                    metaData.append((char) b);
                }
            }
            if (count > (metaDataOffset + metaDataLength)) {
                break;
            }
        }

        // Set the data
        metadata = IcyStreamMeta.parseMetadata(metaData.toString());

        // Close
        stream.close();
        return true;
    }



    public static Map<String, String> parseMetadata(String metaString) {
        Map<String, String> metadata = new HashMap();
        String[] metaParts = metaString.split(";");
        Pattern p = Pattern.compile("^([a-zA-Z]+)=\\'([^\\']*)\\'$");
        Matcher m;
        for (int i = 0; i < metaParts.length; i++) {
            m = p.matcher(metaParts[i]);
            if (m.find()) {
                metadata.put((String) m.group(1), (String) m.group(2));
            }
        }

        return metadata;
    }

    public static Builder newBuild() {
        return new IcyStreamMeta().new Builder();
    }

    @Override
    public void run() {
        Handler handler = new Handler(Looper.getMainLooper());
        try {
            retreiveMetadata();
            final String title = getTitle();
            final String artist = getArtist();


            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (onStreamMetaListner != null) {
                        onStreamMetaListner.onInfoUpdated(artist, title);
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (onStreamMetaListner != null) {
                        onStreamMetaListner.onInfoFail();
                    }
                }
            });
        }
    }

    public interface OnStreamMetaListner {
        void onInfoUpdated(String artist, String trackname);

        void onInfoFail();
    }

    public class Builder {
        private Builder() {

        }

        public Builder setUrl(URL streamUrl) {
            IcyStreamMeta.this.streamUrl = streamUrl;
            IcyStreamMeta.this.metadata = null;
            return this;
        }

        public Builder setOnStreamMetaListner(OnStreamMetaListner onStreamMetaListner) {
            IcyStreamMeta.this.onStreamMetaListner = onStreamMetaListner;
            return this;
        }

        public IcyStreamMeta build() {
            return IcyStreamMeta.this;
        }
    }
}