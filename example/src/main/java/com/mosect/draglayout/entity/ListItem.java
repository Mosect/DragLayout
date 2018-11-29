package com.mosect.draglayout.entity;

import java.io.Serializable;

public class ListItem implements Serializable, Cloneable {

    private String title;
    private String info;

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
}
