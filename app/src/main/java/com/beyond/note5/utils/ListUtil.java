package com.beyond.note5.utils;

import com.beyond.note5.bean.Element;

import java.util.List;

public class ListUtil {

    public static <S extends Element> S getById(List<S> list, String id) {
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) {
                index = i;
            }
        }
        if (index == -1) {
            return null;
        }
        return list.get(index);
    }

    public static <S extends Element> int getIndexById(List<S> list, String id) {
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) {
                index = i;
            }
        }
        return index;
    }
}
