package com.mosect.draglayout.entity;

import java.io.Serializable;

public class ListItem implements Serializable, Cloneable {

    private String title;
    private String info;
    private boolean touchScrollable; // 是否可以触摸滑动

    @Override
    public ListItem clone() {
        try {
            return (ListItem) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public boolean isTouchScrollable() {
        return touchScrollable;
    }

    public void setTouchScrollable(boolean touchScrollable) {
        this.touchScrollable = touchScrollable;
    }
}
