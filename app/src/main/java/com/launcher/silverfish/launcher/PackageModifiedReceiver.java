package com.launcher.rapidLaunch.launcher;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.launcher.rapidLaunch.sqlite.LauncherSQLiteHelper;

public class PackageModifiedReceiver extends BroadcastReceiver {

    private final static String PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    private final static String PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            boolean added = intent.getAction().equals(PACKAGE_ADDED);
            boolean removed = intent.getAction().equals(PACKAGE_REMOVED);
            if (added || removed) {
                final String pkg = intent.getDataString().split(":")[1];
                final com.launcher.rapidLaunch.launcher.App app = (com.launcher.rapidLaunch.launcher.App) context.getApplicationContext();
                final LauncherSQLiteHelper sql = new LauncherSQLiteHelper(app);

                if (added) {
                    // TODO Determine its category
                } else {
                    // TODO: ShortcutTable entries are no longer uniquely identified by its packageName.
                    // Also take in to account activityName and intentUri
                    final long id = sql.getShortcutId(pkg);
                    sql.removeShortcut(id);
                    if (app.shortcutListener != null)
                        app.shortcutListener.onShortcutRemoved(id);
                }
            }
        } catch (Exception e) {
            // We really don't want to crash when a package is added or remove, ignore everything
            e.printStackTrace();
        }
    }
}
