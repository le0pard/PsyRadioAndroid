package com.catware.psyradio.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.catware.psyradio.manager.SettingManager;

/**
 * Created by officemac on 08.09.16.
 */
public class BaseFragment extends Fragment {
    protected SettingManager settingManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingManager = SettingManager.getInstance();
    }
}
