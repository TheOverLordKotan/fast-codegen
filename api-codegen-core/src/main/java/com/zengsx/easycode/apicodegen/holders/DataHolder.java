package com.zengsx.easycode.apicodegen.holders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: DataHolder
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-24 21:54
 */
public class DataHolder<T> {

    /**
     * 校验注解
     */
    private final List<T> container;

    public DataHolder() {
        this.container = new ArrayList<>();
    }

    public List<T> get() {
        return container;
    }

    public void addItem(T... items) {
        addItem(Arrays.asList(items));
    }

    public void addItem(List<T> items) {
        container.addAll(items);
    }

}
