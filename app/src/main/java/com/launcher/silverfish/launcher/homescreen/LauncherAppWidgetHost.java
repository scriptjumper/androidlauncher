package com.launcher.rapidLaunch.launcher.homescreen;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;

/**
 * Overrides the onCreateView to return our custom AppWidgetHostView.
 */
public class LauncherAppWidgetHost extends AppWidgetHost {

    public LauncherAppWidgetHost(Context context, int hostId) {
        super(context, hostId);
    }

    @Override
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        // pass back our custom AppWidgetHostView
        return new LauncherAppWidgetHostView(context);
    }
}