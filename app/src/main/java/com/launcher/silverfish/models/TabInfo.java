package com.launcher.rapidLaunch.models;

import com.launcher.rapidLaunch.dbmodel.TabTable;

public class TabInfo {
    private final long id;
    private final String tag;
    private String label;

    public TabInfo(TabTable tab) {
        this.id = tab.getId();
        this.label = tab.getLabel();

        // Let the tag simply be the id of the tab
        tag = Long.toString(id);
    }

    public long getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public String getLabel() {
        return label;
    }

    public void rename(String new_name) {
        label = new_name;
    }
}
