package com.shaeen.launcher.ui.adapter.recyclerview;

import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shaeen.launcher.R;
import com.shaeen.launcher.data.loader.ShortcutsLoader;
import com.shaeen.launcher.data.model.IconRow;
import com.shaeen.launcher.ui.activity.LauncherActivity;
import com.shaeen.launcher.util.IconUtil;
import com.shaeen.launcher.util.IntentUtil;
import com.shaeen.launcher.util.SDKUtil;

import java.util.HashMap;
import java.util.List;

public class AllAppsRecyclerViewAdapter extends RecyclerView.Adapter<AllAppsRecyclerViewAdapter.RowOfIconsViewHolder> {

    private final HashMap<String, Bitmap> iconHashMap = new HashMap<>();

    private final LauncherActivity mLauncherActivity;
    private final List<IconRow> mIconRows;

    public AllAppsRecyclerViewAdapter(LauncherActivity launcherActivity, List<IconRow> iconRows) {
        mLauncherActivity = launcherActivity;
        mIconRows = iconRows;
    }

    @Override
    public RowOfIconsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RowOfIconsViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_all_apps_row, parent, false));
    }

    @Override
    public void onBindViewHolder(final RowOfIconsViewHolder holder, int position) {
        final IconRow iconRow = mIconRows.get(position);

        for (int column = 0; column < LauncherActivity.APP_DRAWER_COLUMNS; column++) {
            boolean shouldHideIcon = false;
            ApplicationInfo applicationInfo = null;
            if (iconRow.getApplicationInfoList().size() <= column) {
                shouldHideIcon = true;
            } else {
                applicationInfo = iconRow.getApplicationInfoList().get(column);
            }

            View rootView;
            ImageView imageView;
            TextView textView;
            switch (column + 1) {
                case 1:
                    rootView = holder.firstIconView;
                    imageView = ((ImageView) holder.firstIconView.getChildAt(0));
                    textView = ((TextView) holder.firstIconView.getChildAt(1));
                    break;
                case 2:
                    rootView = holder.secondIconView;
                    imageView = ((ImageView) holder.secondIconView.getChildAt(0));
                    textView = ((TextView) holder.secondIconView.getChildAt(1));
                    break;
                case 3:
                    rootView = holder.thirdIconView;
                    imageView = ((ImageView) holder.thirdIconView.getChildAt(0));
                    textView = ((TextView) holder.thirdIconView.getChildAt(1));
                    break;
                case 4:
                    rootView = holder.fourthIconView;
                    imageView = ((ImageView) holder.fourthIconView.getChildAt(0));
                    textView = ((TextView) holder.fourthIconView.getChildAt(1));
                    break;
                case 5:
                    rootView = holder.fifthIconView;
                    imageView = ((ImageView) holder.fifthIconView.getChildAt(0));
                    textView = ((TextView) holder.fifthIconView.getChildAt(1));
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown column index");
            }

            if (shouldHideIcon || applicationInfo == null) {
                if (column == 0) {
                    mIconRows.remove(position);
                    removeLastEmptyRowAsap();
                    break;
                }

                imageView.setImageDrawable(null);
                textView.setText(null);
                rootView.setOnClickListener(null);

                continue;
            }

            final ApplicationInfo finalApplicationInfo = applicationInfo;
            rootView.setOnClickListener(view -> {
                mLauncherActivity.notifyAppLaunch(finalApplicationInfo);

                IntentUtil.launchApp(view, finalApplicationInfo.packageName);
            });

            rootView.setOnLongClickListener(view -> {
                if (!SDKUtil.AT_LEAST_N_MR1) {
                    return false;
                }

                try {
                    mLauncherActivity.showShortcutsBottomSheet(finalApplicationInfo.packageName,
                            ShortcutsLoader.loadShortcuts(mLauncherActivity, finalApplicationInfo.packageName));
                } catch (Exception e) {
                    Toast.makeText(mLauncherActivity, mLauncherActivity.getString(R.string.app_name)
                            + " is not the default Launcher", Toast.LENGTH_SHORT).show();

                    e.printStackTrace();
                }

                return !mLauncherActivity.isExpandingAppDrawer();
            });

            textView.setText(applicationInfo.loadLabel(holder.itemView.getContext().getPackageManager()));

            if (iconHashMap.containsKey(applicationInfo.packageName)) {
                imageView.setImageBitmap(iconHashMap.get(applicationInfo.packageName));
                continue;
            }

            String packageName = applicationInfo.packageName;
            Bitmap icon = IconUtil.setIconOnImageView(mLauncherActivity, imageView, applicationInfo);

            iconHashMap.put(packageName, icon);
        }
    }

    private void removeLastEmptyRowAsap() {
        Handler handler = new Handler();
        handler.post(() -> {
            if (!mLauncherActivity.getAppListRecyclerView().isComputingLayout()) {
                notifyItemRemoved(getItemCount());
            } else {
                removeLastEmptyRowAsap();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mIconRows.size();
    }

    static class RowOfIconsViewHolder extends RecyclerView.ViewHolder {

        ViewGroup firstIconView;
        ViewGroup secondIconView;
        ViewGroup thirdIconView;
        ViewGroup fourthIconView;
        ViewGroup fifthIconView;

        RowOfIconsViewHolder(View itemView) {
            super(itemView);

            firstIconView = itemView.findViewById(R.id.first_icon_row);
            secondIconView = itemView.findViewById(R.id.second_icon_row);
            thirdIconView = itemView.findViewById(R.id.third_icon_row);
            fourthIconView = itemView.findViewById(R.id.fourth_icon_row);
            fifthIconView = itemView.findViewById(R.id.fifth_icon_row);
        }
    }
}