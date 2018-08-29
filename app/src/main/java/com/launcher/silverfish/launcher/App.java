package com.launcher.rapidLaunch.launcher;

import android.app.Application;

import com.launcher.rapidLaunch.dbmodel.DaoMaster;
import com.launcher.rapidLaunch.dbmodel.DaoSession;
import com.launcher.rapidLaunch.shared.Settings;

import org.greenrobot.greendao.database.Database;

public class
App extends Application {

    private Settings mSettings;
    private DaoSession mDaoSession;

    // HomeScreenFragment will set this so that PackageModifiedReceiver can notify it
    public com.launcher.rapidLaunch.launcher.ShortcutListener shortcutListener;

    @Override
    public void onCreate() {
        super.onCreate();

        mSettings = new Settings(this);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "settings-db", null);
        Database db = helper.getWritableDb();
        mDaoSession = new DaoMaster(db).newSession();
    }

    public Settings getSettings() {
        return mSettings;
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }
}
