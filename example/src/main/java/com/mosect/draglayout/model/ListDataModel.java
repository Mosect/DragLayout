package com.mosect.draglayout.model;

import com.mosect.draglayout.entity.ListItem;
import com.mosect.draglayout.entity.PageEntity;

import java.util.ArrayList;
import java.util.Random;

public class ListDataModel {

    private static ListDataModel instance;

    public static ListDataModel getInstance() {
        if (null == instance) {
            instance = new ListDataModel();
        }
        return instance;
    }

    public PageEntity<ListItem> loadPage(int page) {

        PageEntity<ListItem> result = new PageEntity<>();
        result.setTotal(112);
        result.setPageSize(20);
        result.setPageCount(result.getTotal() / result.getPageSize() +
                result.getTotal() % result.getPageSize() == 0 ? 0 : 1);
        result.setPageNumber(page);
        int start = (result.getPageNumber() - 1) * result.getPageSize();
        Random random = new Random();
        for (int i = 0; i < result.getPageSize(); i++) {
            int index = start + i;
            if (index >= result.getTotal()) break;
            if (null == result.getList()) result.setList(new ArrayList<ListItem>());
            ListItem entity = new ListItem();
            entity.setTitle(String.format("DragLayout ITEM %d", index + 1));
            StringBuilder info = new StringBuilder();
            int infoCount = random.nextInt(10) + 1;
            for (int j = 0; j < infoCount; j++) {
                info.append("DragLayout demo by mosect").append("  ");
            }
            entity.setInfo(info.toString());
            result.getList().add(entity);
        }
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
