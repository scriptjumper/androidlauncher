package com.shaeen.launcher.ui.fragment;

import android.view.View;

import com.shaeen.launcher.ui.fragment.base.DockFragment;

public class MostLaunchedAppsDockFragment extends DockFragment {

    @Override
    public void init(View rootView) {
        super.init(rootView);

        dispatchMostLaunchedAppIconsUpdate();
    }
}
