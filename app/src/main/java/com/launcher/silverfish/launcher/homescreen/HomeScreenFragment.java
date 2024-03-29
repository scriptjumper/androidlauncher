package com.launcher.rapidLaunch.launcher.homescreen;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.launcher.rapidLaunch.R;
import com.launcher.rapidLaunch.common.Constants;
import com.launcher.rapidLaunch.common.Utils;
import com.launcher.rapidLaunch.dbmodel.AppTable;
import com.launcher.rapidLaunch.dbmodel.ShortcutTable;
import com.launcher.rapidLaunch.launcher.App;
import com.launcher.rapidLaunch.launcher.LauncherActivity;
import com.launcher.rapidLaunch.launcher.ShortcutListener;
import com.launcher.rapidLaunch.layouts.SquareGridLayout;
import com.launcher.rapidLaunch.models.AppDetail;
import com.launcher.rapidLaunch.shared.Settings;
import com.launcher.rapidLaunch.sqlite.LauncherSQLiteHelper;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HomeScreenFragment extends Fragment {

    //region Fields

    LauncherSQLiteHelper sqlHelper;
    private Settings settings;

    // Constant variables for communication with AppWidgetManager
    final private int WIDGET_HOST_ID = 1339;
    final private int REQUEST_PICK_APPWIDGET = 1340;
    final private int REQUEST_CREATE_APPWIDGET = 1341;
    final private int REQUEST_BIND_APPWIDGET = 1342;

    private AppWidgetManager mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;

    private PackageManager mPacMan;
    private View rootView;
    private ArrayList<AppDetail> appsList;
    private SquareGridLayout shortcutLayout;

    // Save the last X and Y position on the touch events, for later calculating the speed of change
    private float lastX, lastY;
    private boolean touchConsumed; // Did we consume the touch event yet? This will avoid calling it twice
    private float touchSlop;

    //endregion

    //region Android lifecycle

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sqlHelper = new LauncherSQLiteHelper((App) getActivity().getApplication());
        settings = new Settings(getContext());

        // Initiate global variables
        mAppWidgetManager = AppWidgetManager.getInstance(getActivity().getBaseContext());
        mAppWidgetHost = new LauncherAppWidgetHost(getActivity().getApplicationContext(), WIDGET_HOST_ID);
        mAppWidgetHost.startListening();

        mPacMan = getActivity().getPackageManager();
        appsList = new ArrayList<>();

        rootView = inflater.inflate(R.layout.activity_home, container, false);
        // Set touch slop and listen for touch events, such as swipe
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        rootView.setOnTouchListener(onRootTouchListener);

        shortcutLayout = (SquareGridLayout) rootView.findViewById(R.id.shortcut_area);

        // Start listening for shortcut additions
        ((LauncherActivity) getActivity())
                .setFragShortcutAddListenerRefreshListener(new ShortcutAddListener() {
                    @Override
                    public void OnShortcutAdd(AppTable appTable) {
                        // Insert it into the database and get the row id
                        // TODO: Check if an error has occurred while inserting into database.
                        ShortcutTable shortcutTable = new ShortcutTable(null, appTable.getPackageName(),
                                appTable.getActivityName(), null);
                        if (sqlHelper.canAddShortcut(shortcutTable)) {
                            sqlHelper.addShortcut(shortcutTable);
                            if (addAppToView(shortcutTable)) {
                                updateShortcuts();
                            }
                        }
                    }
                });

        addWidgetOnClickListener();
        setOnDragListener();

        setWidgetColors(settings.getWidgetBgColor(), settings.getFontFgColor());
        setWidgetVisibility(settings.isWidgetVisible());
        loadWidget();

        loadApps();
        updateShortcuts();

        // Listen for application uninstall events to remove the shortcuts if needed
        ((App) getContext().getApplicationContext()).shortcutListener = shortcutListener;

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAppWidgetHost.stopListening();
        ((App) getContext().getApplicationContext()).shortcutListener = null;
    }

    //endregion

    //region Manage apps and shortcuts

    private void loadApps() {
        List<ShortcutTable> shortcuts = sqlHelper.getAllShortcuts();
        for (ShortcutTable shortcut : shortcuts) {
            if (!addAppToView(shortcut)) {
                // If the shortcut could not be added then the user has probably uninstalled it,
                // so we should remove it from the db
                Log.d("HomeFragment", "Removing shortcut " + shortcut.getPackageName() + " from db");
                sqlHelper.removeShortcut(shortcut.getId());
            }
        }
    }

    private boolean addAppToView(ShortcutTable shortcut) {
        try {
            ActivityInfo activityInfo = mPacMan.getActivityInfo(
                    new ComponentName(shortcut.getPackageName(), shortcut.getActivityName()), PackageManager.GET_META_DATA);
            AppDetail appDetail = new AppDetail();
            appDetail.label = activityInfo.loadLabel(mPacMan);
            appDetail.intentUri = shortcut.getIntentUri();
            // load the icon later in an async task
            appDetail.icon = null;

            appDetail.packageName = shortcut.getPackageName();
            appDetail.activityName = shortcut.getActivityName();
            appDetail.id = shortcut.getId();

            appsList.add(appDetail);
            return true;

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private ShortcutListener shortcutListener = new ShortcutListener() {
        @Override
        public void onShortcutRemoved(long appId) {
            for (int i = 0; i < appsList.size(); ++i) {
                if (appsList.get(i).id == appId) {
                    appsList.remove(i);
                    updateShortcuts();
                    break;
                }
            }
        }
    };

    private void removeApp(int app_index, long app_id) {
        sqlHelper.removeShortcut(app_id);
        appsList.remove(app_index);
    }

    @SuppressWarnings("deprecation")
    private void updateShortcuts() {
        int count = appsList.size();
        int size = (int) Math.ceil(Math.sqrt(count));
        shortcutLayout.removeAllViews();

        if (size == 0) {
            size = 1;
        }

        // Redraw the layout
        shortcutLayout.setSize(size);
        shortcutLayout.requestLayout();
        shortcutLayout.invalidate();

        for (int i = 0; i < appsList.size(); i++) {
            final AppDetail app = appsList.get(i);
            View convertView = getActivity().getLayoutInflater().inflate(R.layout.shortcut_item, null);

            // load the app icon in an async task
            ImageView im = (ImageView) convertView.findViewById(R.id.item_app_icon);
            Utils.loadAppIconAsync(mPacMan, app.packageName.toString(), im);

            TextView tv = (TextView) convertView.findViewById(R.id.item_app_label);
            tv.setText(app.label);
            shortcutLayout.addView(convertView);

            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (MotionEventCompat.getActionMasked(event)) {
                        case MotionEvent.ACTION_DOWN:
                            updateTouchDown(event);
                            break;

                        case MotionEvent.ACTION_MOVE:
                            tryConsumeSwipe(event);
                            break;

                        case MotionEvent.ACTION_UP:
                            // We only want to launch the activity if the touch was not consumed yet!
                            if (!touchConsumed) {
                                Intent i = new Intent();
                                if (app.intentUri != null) {
                                    try {
                                        i = Intent.parseUri(app.intentUri.toString(), Intent.URI_INTENT_SCHEME);
                                    } catch (URISyntaxException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    i.setAction(Intent.ACTION_MAIN);
                                    i.setComponent(new ComponentName(app.packageName.toString(), app.activityName.toString()));
                                }
                                if (i != null) {
                                    // Sanity check (application may have been uninstalled)
                                    // TODO Remove it from the database
                                    startActivity(i);
                                } else {
                                    Toast.makeText(getContext(),
                                            R.string.application_not_installed, Toast.LENGTH_SHORT).show();
                                }
                            }
                            break;
                    }

                    return touchConsumed;
                }
            });

            // start a drag when an app has been long clicked
            final long appId = app.id;
            final int appIndex = i;
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    String[] mime_types = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                    ClipData data = new ClipData(Constants.DRAG_SHORTCUT_REMOVAL,
                            mime_types, new ClipData.Item(Long.toString(appId)));

                    data.addItem(new ClipData.Item(Integer.toString(appIndex)));

                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                            view.findViewById(R.id.item_app_icon));

                    // "This method was deprecated in API level 24. Use startDragAndDrop()
                    // for newer platform versions."
                    if (Build.VERSION.SDK_INT < 24) {
                        view.startDrag(data, shadowBuilder, view, 0);
                    } else {
                        view.startDragAndDrop(data, shadowBuilder, view, 0);
                    }

                    // Show removal indicator
                    FrameLayout rem_ind = (FrameLayout) rootView.findViewById(R.id.remove_indicator);
                    rem_ind.setVisibility(View.VISIBLE);
                    AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
                    animation.setDuration(500);
                    rem_ind.startAnimation(animation);
                    return true;

                }
            });
        }
    }

    //endregion

    //region Listeners

    private void setOnDragListener() {
        rootView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // Check that it is a shortcut removal gesture
                        ClipDescription cd = dragEvent.getClipDescription();
                        if (!cd.getLabel().toString().equals(Constants.DRAG_SHORTCUT_REMOVAL)) {
                            return false;
                        }
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        // Don't do anything
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        //Don't do anything
                        break;
                    case DragEvent.ACTION_DROP:
                        // If outside of bound, remove the app
                        if (Utils.onBottomCenterScreenEdge(getActivity(), dragEvent.getX(), dragEvent.getY())) {
                            String appId = dragEvent.getClipData().getItemAt(0).getText().toString();
                            String appIndex = dragEvent.getClipData().getItemAt(1).getText().toString();
                            removeApp(Integer.parseInt(appIndex), Long.parseLong(appId));
                            updateShortcuts();
                        }

                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        // Hide the remove-indicator
                        FrameLayout rem_ind = (FrameLayout) rootView.findViewById(R.id.remove_indicator);
                        rem_ind.setVisibility(View.INVISIBLE);
                        break;

                }
                return true;
            }
        });
    }

    private void addWidgetOnClickListener() {
        // Long click on widget area should start up widget selection
        FrameLayout widget_area = (FrameLayout) rootView.findViewById(R.id.widget_area);
        widget_area.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                popupSelectWidget();
                return true;
            }
        });
    }

    //region Widgets

    //region Widget selection

    public void popupSelectWidget() {
        // Allocate widget id and start widget selection activity
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addEmptyData(pickIntent); // This is needed work around some weird bug.
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    //endregion

    //region Widget creation

    private void createWidget(Intent data) {
        // Get the widget id
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        createWidgetFromId(appWidgetId);
    }

    private void createWidgetFromId(int widget_id) {
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widget_id);

        // Create the host view
        AppWidgetHostView hostView = mAppWidgetHost.createView(getActivity().getBaseContext(), widget_id, appWidgetInfo);
        hostView.setAppWidget(widget_id, appWidgetInfo);

        // And place the widget in widget area and save.
        placeWidget(hostView);
        settings.setWidget(appWidgetInfo.provider.getPackageName(), appWidgetInfo.provider.getClassName());
    }

    //endregion

    //region Widget loading

    private void loadWidget() {
        ComponentName cn = settings.getWidget();

        Log.d("Widget creation", "Loaded from db: " + cn.getClassName() + " - " + cn.getPackageName());
        // Check that there actually is a widget in the database
        if (cn.getPackageName().isEmpty() && cn.getClassName().isEmpty()) {
            Log.d("Widget creation", "DB was empty");
            return;
        }
        Log.d("Widget creation", "DB was not empty");

        final List<AppWidgetProviderInfo> infos = mAppWidgetManager.getInstalledProviders();

        // Get AppWidgetProviderInfo
        AppWidgetProviderInfo appWidgetInfo = null;
        // Just in case you want to see all package and class names of installed widget providers,
        // this code is useful
        for (final AppWidgetProviderInfo info : infos) {
            Log.d("AD3", info.provider.getPackageName() + " / "
                    + info.provider.getClassName());
        }
        // Iterate through all infos, trying to find the desired one
        for (final AppWidgetProviderInfo info : infos) {
            if (info.provider.getClassName().equals(cn.getClassName()) &&
                    info.provider.getPackageName().equals(cn.getPackageName())) {
                // We found it!
                appWidgetInfo = info;
                break;
            }
        }
        if (appWidgetInfo == null) {
            Log.d("Widget creation", "app info was null");
            return; // Stop here
        }

        // Allocate the hosted widget id
        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

        boolean allowed_to_bind = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, cn);

        // Ask the user to allow this app to have access to their widgets
        if (!allowed_to_bind) {
            Log.d("Widget creation", "asking for permission");
            Intent i = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            Bundle args = new Bundle();
            args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            args.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, cn);
            if (Build.VERSION.SDK_INT >= 21) {
                args.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, null);
            }
            i.putExtras(args);
            startActivityForResult(i, REQUEST_BIND_APPWIDGET);
            return;
        } else {

            Log.d("Widget creation", "Allowed to bind");
            Log.d("Widget creation", "creating widget");
            //Intent i = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            //createWidgetFromId(appWidgetId);
        }
        // Create the host view
        AppWidgetHostView hostView = mAppWidgetHost.createView(
                getActivity().getBaseContext(), appWidgetId, appWidgetInfo);

        // Set the desired widget
        hostView.setAppWidget(appWidgetId, appWidgetInfo);

        placeWidget(hostView);
    }

    //endregion

    //region Widget configuration

    private void configureWidget(Intent data) {
        // Get the selected widget information
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            // If the widget wants to be configured then start its configuration activity
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            // Otherwise simply create it
            createWidget(data);
        }
    }

    //endregion

    //region Widget placing

    private void placeWidget(AppWidgetHostView hostView) {
        FrameLayout widget_area = (FrameLayout) rootView.findViewById(R.id.widget_area);

        widget_area.removeAllViews();
        widget_area.addView(hostView);

        // Let the widget host view take control of the long click action.
        hostView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                popupSelectWidget();
                return true;
            }
        });

        hostView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (MotionEventCompat.getActionMasked(event)) {
                    case MotionEvent.ACTION_DOWN:
                        updateTouchDown(event);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        tryConsumeSwipe(event);
                        break;
                }

                return touchConsumed;
            }
        });
    }

    //endregion

    //region Widget bugs workarounds

    private void addEmptyData(Intent pickIntent) {
        // This is needed work around some weird bug.
        // This will simply add some empty data to the intent.
        ArrayList customInfo = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
        ArrayList customExtras = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
    }

    //endregion

    //endregion

    //region Events

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // listen for widget manager response
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            } else if (requestCode == REQUEST_BIND_APPWIDGET) {
                createWidget(data);
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    final View.OnTouchListener onRootTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (MotionEventCompat.getActionMasked(event)) {
                case MotionEvent.ACTION_DOWN:
                    updateTouchDown(event);
                    break;

                case MotionEvent.ACTION_MOVE:
                    tryConsumeSwipe(event);
                    break;
            }

            return true;
        }
    };

    //endregion

    //region UI

    public void setWidgetVisibility(boolean visible) {
        final FrameLayout widgetArea = (FrameLayout) rootView.findViewById(R.id.widget_area);
        widgetArea.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setWidgetColors(int background, int foreground) {
        final FrameLayout widgetArea = (FrameLayout) rootView.findViewById(R.id.widget_area);
        final TextView noWidgetNotice = (TextView) rootView.findViewById(R.id.no_widget_notice);

        widgetArea.setBackgroundColor(background);
        // The no-widget notice will be null if a widget is set
        if (noWidgetNotice != null)
            noWidgetNotice.setTextColor(foreground);
    }

    void updateTouchDown(MotionEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        touchConsumed = false;
    }

    void tryConsumeSwipe(MotionEvent event) {
        if (!touchConsumed) {
            // Also subtract the X: we want to trigger if we scroll down, not to the sides
            float downSpeed = event.getY() - lastY - Math.abs(lastX - event.getX());
            if (downSpeed > touchSlop) {
                // The user swiped down, show the status bar and consume the event
                expandNotificationPanel();
                touchConsumed = true;
            } else {
                updateTouchDown(event);
            }
        }
    }

    void expandNotificationPanel() {
        try {
            //noinspection WrongConstant
            Object service = getActivity().getSystemService("statusbar");
            Class<?> clazz = Class.forName("android.app.StatusBarManager");
            Method expand = Build.VERSION.SDK_INT <= 16 ?
                    clazz.getMethod("expand") :
                    clazz.getMethod("expandNotificationsPanel");

            expand.invoke(service);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    //endregion
}
