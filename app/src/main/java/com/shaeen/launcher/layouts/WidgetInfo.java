package com.shaeen.launcher.layouts;

import android.appwidget.AppWidgetHostView;

public class WidgetInfo {

    public AppWidgetHostView hostView;
    private final int appWidgetId;

    public WidgetInfo(int appWidgetId) {
        this.appWidgetId = appWidgetId;
    }

}
