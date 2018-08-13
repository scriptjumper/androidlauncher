package com.launcher.rapidLaunch.launcher.appdrawer;

import android.content.Context;
import android.view.View;
import android.widget.TabHost;

public class DummyTabContent implements TabHost.TabContentFactory {
    private final Context mContext;

    public DummyTabContent(Context context) {
        mContext = context;
    }

    @Override
    public View createTabContent(String tag) {
        return new View(mContext);
    }
}
