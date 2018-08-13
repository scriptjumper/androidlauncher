package com.launcher.rapidLaunch.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.DragEvent;
import android.view.KeyEvent;

import com.launcher.rapidLaunch.R;
import com.launcher.rapidLaunch.common.Constants;
import com.launcher.rapidLaunch.common.Utils;
import com.launcher.rapidLaunch.dbmodel.AppTable;
import com.launcher.rapidLaunch.launcher.homescreen.HomeScreenFragment;
import com.launcher.rapidLaunch.launcher.homescreen.ShortcutAddListener;
import com.launcher.rapidLaunch.launcher.settings.SettingsScreenFragment;
import com.launcher.rapidLaunch.sqlite.LauncherSQLiteHelper;
import com.launcher.rapidLaunch.utils.PackagesCategories;

import java.util.List;

/**
 * This is the main activity of the launcher
 */
public class LauncherActivity extends FragmentActivity
        implements SettingsScreenFragment.SettingChanged {

    //region Fields

    LauncherPagerAdapter mCollectionPagerAdapter;
    ViewPager mViewPager;

    // Used for telling home screen when a shortcut is added.
    private ShortcutAddListener shortcutAddListener;

    // Used when the intent is created to specify an starting page index
    public static final String START_PAGE = "start_page";

    //endregion

    //region Android lifecycle

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        // Try creating the default tabs. This will have no effect if they already exist.
        if (createDefaultTabs())
            autoSortApplications();

        // Create the pager
        mCollectionPagerAdapter =
                new LauncherPagerAdapter(
                        getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCollectionPagerAdapter);
        mViewPager.setCurrentItem(getIntent().getIntExtra(START_PAGE, 1));
    }

    //endregion

    //region First time setup

    private boolean createDefaultTabs() {
        LauncherSQLiteHelper sql = new LauncherSQLiteHelper((App) getApplication());
        if (sql.hasTabs())
            return false;

        // Load default names for the tab
        String[] defaultTabNames = new String[]{
                getString(R.string.tab_other),
                getString(R.string.tab_phone),
                getString(R.string.tab_games),
                getString(R.string.tab_internet),
                getString(R.string.tab_media),
                getString(R.string.tab_accessories),
                getString(R.string.tab_settings),
        };

        // Create and add the tables to the SQL database
        for (int i = 0; i < 7; i++) {
            String tab_name = defaultTabNames[i];
            sql.addTab(tab_name);
        }

        return true;
    }

    // Auto sorts the applications in their corresponding tabs
    private void autoSortApplications() {

        // Set up both SQL helper and package manager
        LauncherSQLiteHelper sql = new LauncherSQLiteHelper((App) getApplication());
        PackageManager mPacMan = getApplicationContext().getPackageManager();

        // Set MAIN and LAUNCHER filters, so we only get activities with that defined on their manifest
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        // Get all activities that have those filters
        List<ResolveInfo> availableActivities = mPacMan.queryIntentActivities(i, 0);
        // Store here the packages and their categories IDs
        // This will allow us to add all the apps at once instead opening the database over and over
        List<AppTable> apps =
                PackagesCategories.setCategoriesForAppTable(getApplicationContext(), availableActivities);

        // Then add all the apps to their corresponding tabs at once
        sql.addAppsToTab(apps);
    }

    //endregion

    //region Fragment communication

    @Override
    public void onWidgetVisibilityChanged(boolean visible) {
        getHomeScreenFragment().setWidgetVisibility(visible);
    }

    @Override
    public void onWidgetChangeRequested() {
        getHomeScreenFragment().popupSelectWidget();
    }

    @Override
    public void onColorChanged(int drawerBg, int widgetBg, int fontFg) {
        // There is no need to notify the app drawer fragment that the colors changed.
        // This tab is at #0, and the settings tab is #2. Only 2 tabs (#1 and #2) are
        // kept in memory, so as soon as #0 gets created, its colors will get updated.
        getHomeScreenFragment().setWidgetColors(widgetBg, fontFg);
    }

    HomeScreenFragment getHomeScreenFragment() {
        return (HomeScreenFragment) mCollectionPagerAdapter.instantiateItem(mViewPager, 1);
    }

    public boolean addShortcut(AppTable appTable) {
        if (getFragShortcutAddListenerRefreshListener() != null) {
            getFragShortcutAddListenerRefreshListener().OnShortcutAdd(appTable);
            return true;
        } else
            return false;
    }

    public void moveToScreen(int screen) {
        mViewPager.setCurrentItem(screen, true);
    }

    //endregion

    //region Listeners

    public ShortcutAddListener getFragShortcutAddListenerRefreshListener() {
        return shortcutAddListener;
    }

    public void setFragShortcutAddListenerRefreshListener(ShortcutAddListener shortcutAddListener) {
        this.shortcutAddListener = shortcutAddListener;
    }

    //endregion

    //region Events

    private void dropItem(DragEvent dragEvent) {
        if (mViewPager.getCurrentItem() == 1) {
            AppTable appTable = new AppTable();
            appTable.setPackageName(dragEvent.getClipData().getItemAt(0).getText().toString());
            appTable.setActivityName(dragEvent.getClipData().getItemAt(1).getText().toString());
            addShortcut(appTable);
        }
    }

    private void changePage(DragEvent dragEvent) {

        // Get display size
        int width = Utils.getScreenDimensions(this).x;
        int threshold = (int) (Constants.DRAG_THRESHOLD_PERCENT_X * width);

        // Change page
        if (mViewPager.getCurrentItem() == 0 && dragEvent.getX() >= width - threshold) {
            mViewPager.setCurrentItem(1);
        } else if (mViewPager.getCurrentItem() == 1 && dragEvent.getX() <= threshold) {
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Override the functionality of back and home key
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)
                && event.getRepeatCount() == 0) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Override the functionality of back and home key
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) && event.isTracking()
                && !event.isCanceled()) {
            mViewPager.setCurrentItem(1);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    //endregion
}
