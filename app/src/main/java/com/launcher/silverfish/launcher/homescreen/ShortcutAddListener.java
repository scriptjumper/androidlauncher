package com.launcher.rapidLaunch.launcher.homescreen;

import com.launcher.rapidLaunch.dbmodel.AppTable;

/**
 * Used for communication between the launcher activity and the home screen fragment.
 */
public interface ShortcutAddListener {
    void OnShortcutAdd(AppTable appTable);
}
