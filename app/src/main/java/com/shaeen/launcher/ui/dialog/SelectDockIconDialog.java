package com.shaeen.launcher.ui.dialog;

import android.app.Dialog;
import android.content.pm.ApplicationInfo;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.shaeen.launcher.R;
import com.shaeen.launcher.ui.activity.LauncherActivity;
import com.shaeen.launcher.ui.adapter.recyclerview.SelectDockIconRecyclerViewAdapter;

public class SelectDockIconDialog extends Dialog {

    private final LauncherActivity mLauncherActivity;
    private final int mColumn;

    private SelectDockIconDialog(LauncherActivity launcherActivity, int column) {
        super(launcherActivity);

        mLauncherActivity = launcherActivity;
        mColumn = column;

        init();
    }

    public static void show(LauncherActivity launcherActivity, int dockItemColumn) {
        new SelectDockIconDialog(launcherActivity, dockItemColumn).show();
    }

    private void init() {
        setContentView(R.layout.dialog_select_app);

        RecyclerView recyclerView = findViewById(R.id.select_dock_icon_apps_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new SelectDockIconRecyclerViewAdapter(this,
                mLauncherActivity.getApplicationInfoList()));
    }

    public void onAppSelected(ApplicationInfo applicationInfo) {
        dismiss();

        mLauncherActivity.onDockAppSelected(applicationInfo, mColumn);
    }

    public LauncherActivity getLauncherActivity() {
        return mLauncherActivity;
    }
}
