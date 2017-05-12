package com.wangx.dggapi;


import android.view.View;

public interface IProxy<T> {
    public void inject(T target, View root);
}
