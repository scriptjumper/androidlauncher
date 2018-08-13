package com.launcher.rapidLaunch.models;

import android.graphics.drawable.Drawable;

public class AppDetail {
    public CharSequence label;
    public CharSequence packageName;
    public CharSequence activityName;
    public CharSequence intentUri; // for launcher shortcuts created by for example
    // file managers which open a specific directory.
    public Drawable icon;
    public long id;
}
