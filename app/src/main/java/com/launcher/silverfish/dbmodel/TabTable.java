package com.launcher.rapidLaunch.dbmodel;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

@Entity(indexes = {
        @Index(value = "label")
})
public class TabTable {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String label;

    @Generated(hash = 1581068435)
    public TabTable(Long id, @NotNull String label) {
        this.id = id;
        this.label = label;
    }

    @Generated(hash = 1198679566)
    public TabTable() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
