package com.launcher.rapidLaunch.sqlite;

import com.launcher.rapidLaunch.dbmodel.AppTable;
import com.launcher.rapidLaunch.dbmodel.AppTableDao;
import com.launcher.rapidLaunch.dbmodel.DaoSession;
import com.launcher.rapidLaunch.dbmodel.ShortcutTable;
import com.launcher.rapidLaunch.dbmodel.ShortcutTableDao;
import com.launcher.rapidLaunch.dbmodel.TabTable;
import com.launcher.rapidLaunch.dbmodel.TabTableDao;
import com.launcher.rapidLaunch.launcher.App;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

public class LauncherSQLiteHelper {

    private DaoSession mSession;

    public LauncherSQLiteHelper(App app) {
        mSession = app.getDaoSession();
    }

    public boolean hasTabs() {
        return mSession.getTabTableDao().count() > 0;
    }

    public TabTable addTab(String tabName) {
        TabTable newTab = new TabTable(null, tabName);
        mSession.getTabTableDao().insert(newTab);
        return newTab;
    }

    public void removeTab(long tabId) {
        mSession.getTabTableDao().deleteByKey(tabId);
    }

    public String getTabName(long tabId) {
        return mSession.getTabTableDao().queryBuilder()
                .where(TabTableDao.Properties.Id.eq(tabId))
                .uniqueOrThrow().getLabel();
    }

    public long renameTab(long tabId, String newName) {
        return mSession.insertOrReplace(new TabTable(tabId, newName));
    }

    public List<TabTable> getAllTabs() {
        return mSession.getTabTableDao().loadAll();
    }

    public List<AppTable> getAppsForTab(long tabId) {
        return mSession.getAppTableDao().queryBuilder()
                .where(AppTableDao.Properties.TabId.eq(tabId))
                .list();
    }

    /*  Looks like this is unused. Commenting out (YAGNI).
        public List<AppTable> getAllApps() {
            return mSession.getAppTableDao().loadAll();
        }
    */
    public boolean containsApp(String activityName) {
        return mSession.getAppTableDao().queryBuilder()
                .where(AppTableDao.Properties.ActivityName.eq(activityName))
                .unique() != null;
    }

    public void addAppToTab(AppTable appTable) {
        mSession.getAppTableDao().insert(appTable);
    }

    public void addAppsToTab(List<AppTable> apps) {
        mSession.getAppTableDao().insertInTx(apps);
    }

    public void removeAppFromTab(AppTable appTable) {
        QueryBuilder qb = mSession.getAppTableDao().queryBuilder();
        AppTable app = (AppTable) qb.where(qb.and(AppTableDao.Properties.TabId.eq(appTable.getTabId()),
                AppTableDao.Properties.ActivityName.eq(appTable.getActivityName()))).unique();

        if (app != null)
            mSession.getAppTableDao().delete(app);
    }

    public void removeApps(List<AppTable> apps) {
        mSession.getAppTableDao().deleteInTx(apps);
    }

    public boolean canAddShortcut(ShortcutTable shortcutTable) {
        WhereCondition commonCondition = mSession.getShortcutTableDao().queryBuilder().and(
                ShortcutTableDao.Properties.ActivityName.eq(shortcutTable.getActivityName()),
                ShortcutTableDao.Properties.PackageName.eq(shortcutTable.getPackageName())
        );
        if (shortcutTable.getIntentUri() != null) {
            return mSession.getShortcutTableDao().queryBuilder()
                    .where(commonCondition,
                            ShortcutTableDao.Properties.IntentUri.eq(shortcutTable.getIntentUri()))
                    .unique() == null;
        } else {
            return mSession.getShortcutTableDao().queryBuilder()
                    .where(commonCondition,
                            ShortcutTableDao.Properties.IntentUri.isNull())
                    .unique() == null;
        }
    }

    public long addShortcut(ShortcutTable shortcutTable) {
        shortcutTable.setId(null);
        return mSession.getShortcutTableDao().insert(shortcutTable);
    }

    public long getShortcutId(String packageName) {
        try {
            return mSession.getShortcutTableDao().queryBuilder()
                    .where(ShortcutTableDao.Properties.PackageName.eq(packageName))
                    .uniqueOrThrow().getId();
        } catch (DaoException ignored) {
            return -1;
        }
    }

    public void removeShortcut(long id) {
        try {
            mSession.getShortcutTableDao().delete(
                    mSession.getShortcutTableDao().queryBuilder()
                            .where(ShortcutTableDao.Properties.Id.eq(id))
                            .uniqueOrThrow()
            );
        } catch (DaoException ignored) {
        }
    }

    public List<ShortcutTable> getAllShortcuts() {
        return mSession.getShortcutTableDao().loadAll();
    }
}
