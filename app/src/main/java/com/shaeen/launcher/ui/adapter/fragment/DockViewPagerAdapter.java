package com.shaeen.launcher.ui.adapter.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.shaeen.launcher.ui.fragment.HomeScreenDockFragment;
import com.shaeen.launcher.ui.fragment.MostLaunchedAppsDockFragment;
import com.shaeen.launcher.ui.fragment.base.DockFragment;

public class DockViewPagerAdapter extends FragmentPagerAdapter {

    private final DockFragment mHomeScreenDockFragment = new HomeScreenDockFragment();
    private final DockFragment mMostLaunchedAppsDockFragment = new MostLaunchedAppsDockFragment();

    private final DockFragment[] DOCK_FRAGMENTS = {
            mHomeScreenDockFragment,
            mMostLaunchedAppsDockFragment
    };

    public DockViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return DOCK_FRAGMENTS.length;
    }

    @Override
    public Fragment getItem(int position) {
        return DOCK_FRAGMENTS[position];
    }

    public HomeScreenDockFragment getHomeScreenDockFragment() {
        return (HomeScreenDockFragment) mHomeScreenDockFragment;
    }

    public MostLaunchedAppsDockFragment getMostLaunchedAppsDockFragment() {
        return (MostLaunchedAppsDockFragment) mMostLaunchedAppsDockFragment;
    }
}
