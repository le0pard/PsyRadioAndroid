package com.catware.psyradio.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.catware.psyradio.constants.PsyConst;
import com.catware.psyradio.constants.StreamUrl;

/**
 * Created by officemac on 08.09.16.
 */
public class SettingManager {

    private static SettingManager settingManager = null;

    private SettingManager() {

    }

    public static SettingManager getInstance() {
        if (settingManager == null) {
            settingManager = new SettingManager();
        }
        return settingManager;
    }

    //region time
    public void saveUserTime(Context context, long timeInSeconds) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(PsyConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PsyConst.TIME_KEY, timeInSeconds);
        editor.apply();
    }

    public long getUserTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PsyConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(PsyConst.TIME_KEY, 0);

    }
    //endregion

    //region stream url
    public void saveStreamUrl(Context context, String streamUrl) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(PsyConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PsyConst.STREAM_URL_KEY, streamUrl);
        editor.apply();
    }

    public String getStreamUrl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PsyConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PsyConst.STREAM_URL_KEY, StreamUrl.STREAM_URL_256.getUrl());

    }
    //endregion


}
