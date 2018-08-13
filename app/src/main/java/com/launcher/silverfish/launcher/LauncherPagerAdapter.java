package com.launcher.rapidLaunch.launcher;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.launcher.rapidLaunch.R;
import com.launcher.rapidLaunch.launcher.appdrawer.TabbedAppDrawerFragment;
import com.launcher.rapidLaunch.launcher.homescreen.HomeScreenFragment;
import com.launcher.rapidLaunch.launcher.settings.SettingsScreenFragment;

public class LauncherPagerAdapter extends FragmentStatePagerAdapter {

    //region Fields

    // Store a context so we can use getString in later methods
    final private Context _context;

    //endregion

    //region Constructor

    public LauncherPagerAdapter(FragmentManager fm, Context c) {
        super(fm);
        _context = c;
    }

    //endregion

    //region Get items

    @Override
    public Fragment getItem(int pageNumber) {
        // Return the right fragment for given page number
        switch (pageNumber) {
            case 0: // First page is the app drawer
                return new TabbedAppDrawerFragment();

            case 1: // Second page is the 'home screen'
                return new HomeScreenFragment();

            case 2: // Third page is the 'settings activity'
                return new SettingsScreenFragment();

            default: // Any other page (such as last) is an empty fragment
                return new Fragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int pageNumber) {
        switch (pageNumber) {
            case 0:
                return _context.getString(R.string.text_app_drawer);
            case 1:
                return _context.getString(R.string.text_home_screen);
            case 2:
                return _context.getString(R.string.text_settings_screen);
            default:
                return _context.getString(R.string.text_empty);
        }
    }

    //endregion
}
