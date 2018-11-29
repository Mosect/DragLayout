package com.mosect.draglayout.entity;

import java.util.Collection;

public class PageEntity<T> {

    private int pageNumber;
    private int pageSize;
    private int total;
    private int pageCount;
    private Collection<T> list;

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public Collection<T> getList() {
        return list;
    }

    public void setList(Collection<T> list) {
        this.list = list;
    }
}
